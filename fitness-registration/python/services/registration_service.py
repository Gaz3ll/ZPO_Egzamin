from models import SessionLocal, FitnessClass, Registration


def register(user_id: str, class_id: int) -> str:
    """
    Zapisuje użytkownika o danym user_id na zajęcia class_id.
    Jeśli wolne miejsca na liście głównej są wyczerpane, użytkownik jest dodawany na koniec listy rezerwowej (WAITING).
    """
    db = SessionLocal()
    try:
        fitness_class = db.query(FitnessClass).filter(FitnessClass.id == class_id).first()
        if not fitness_class:
            return "Class not found"

        existing = db.query(Registration).filter(
            Registration.user_id == user_id, Registration.class_id == class_id
        ).first()
        if existing:
            return "Already registered"

        # Pobranie liczby osób na liście głównej
        main_count = db.query(Registration).filter(
            Registration.class_id == class_id, Registration.status == "MAIN"
        ).count()

        if main_count < fitness_class.max_capacity:
            # Rejestracja na liście głównej
            db.add(Registration(user_id=user_id, class_id=class_id, status="MAIN", position=None))
            db.commit()
            return "Registered on main list"
        else:
            # Rejestracja na liście rezerwowej
            waiting_count = db.query(Registration).filter(
                Registration.class_id == class_id, Registration.status == "WAITING"
            ).count()
            db.add(Registration(user_id=user_id, class_id=class_id, status="WAITING", position=waiting_count + 1))
            db.commit()
            return f"Registered on waiting list, position {waiting_count + 1}"
    finally:
        db.close()


def unregister(user_id: str, class_id: int) -> str:
    """
    Wypisuje użytkownika z zajęć.
    Jeżeli użytkownik był na liście głównej, następuje promocja pierwszej osoby z listy rezerwowej.
    Jeżeli użytkownik był na liście rezerwowej, kolejka jest przenumerowywana.
    """
    db = SessionLocal()
    try:
        reg = db.query(Registration).filter(
            Registration.user_id == user_id, Registration.class_id == class_id
        ).first()
        if not reg:
            return "Registration not found"

        was_main = (reg.status == "MAIN")
        db.delete(reg)
        db.commit()

        if was_main:
            # Promocja osoby z rezerwy na listę główną
            _promote_from_waiting(db, class_id)
            return "Unregistered from main list"
        else:
            # Przenumerowanie listy rezerwowej
            _reorder_waiting(db, class_id)
            return "Unregistered from waiting list"
    finally:
        db.close()


def _promote_from_waiting(db, class_id: int):
    """
    Promuje pierwszą osobę z listy rezerwowej (WAITING) o najniższej pozycji w kolejce na listę główną (MAIN).
    """
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()

    if waiting:
        promoted = waiting[0]
        promoted.status = "MAIN"
        promoted.position = None
        db.commit()
        # Po promocji, przenumerowujemy pozostałe osoby w kolejce rezerwowej
        _reorder_waiting(db, class_id)


def _reorder_waiting(db, class_id: int):
    """
    Utrzymuje spójność pozycji w kolejce rezerwowej poprzez ponowne przypisanie indeksów 1..N.
    """
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()

    for i, reg in enumerate(waiting):
        reg.position = i + 1
    db.commit()
