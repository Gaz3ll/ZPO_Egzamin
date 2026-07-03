from models import SessionLocal, FitnessClass, Registration


def register(user_id: str, class_id: int) -> str:
    """
    Zapisuje użytkownika na zajęcia fitness (podejście funkcyjne bez instrukcji if).
    """
    db = SessionLocal()
    try:
        fitness_class = db.query(FitnessClass).filter(FitnessClass.id == class_id).first()
        # Wyrażenie trójargumentowe zamiast tradycyjnego if
        return "Class not found" if not fitness_class else \
               ("Already registered" if db.query(Registration).filter(Registration.user_id == user_id, Registration.class_id == class_id).first() else \
                _do_register(db, user_id, class_id, fitness_class))
    finally:
        db.close()


def _do_register(db, user_id, class_id, fitness_class):
    main_count = db.query(Registration).filter(Registration.class_id == class_id, Registration.status == "MAIN").count()
    is_main = main_count < fitness_class.max_capacity
    status = "MAIN" if is_main else "WAITING"
    waiting_count = db.query(Registration).filter(Registration.class_id == class_id, Registration.status == "WAITING").count()
    pos = None if is_main else waiting_count + 1
    
    db.add(Registration(user_id=user_id, class_id=class_id, status=status, position=pos))
    db.commit()
    
    return "Registered on main list" if is_main else f"Registered on waiting list, position {waiting_count + 1}"


def unregister(user_id: str, class_id: int) -> str:
    """
    Wypisuje użytkownika z zajęć (podejście funkcyjne).
    """
    db = SessionLocal()
    try:
        reg = db.query(Registration).filter(Registration.user_id == user_id, Registration.class_id == class_id).first()
        return "Registration not found" if not reg else _do_unregister(db, reg, class_id)
    finally:
        db.close()


def _do_unregister(db, reg, class_id):
    was_main = (reg.status == "MAIN")
    db.delete(reg)
    db.commit()
    # Uruchomienie promocji lub przenumerowania w wyrażeniu trójargumentowym
    _promote_from_waiting(db, class_id) if was_main else _reorder_waiting(db, class_id)
    return "Unregistered from main list" if was_main else "Unregistered from waiting list"


def _promote_from_waiting(db, class_id: int):
    """
    Promuje pierwszą osobę z kolejki (podejście funkcyjne).
    """
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()
    
    # Pobranie pierwszego elementu za pomocą iteratora (zwraca None jeśli lista jest pusta)
    promoted = next(iter(waiting), None)
    # Wykorzystanie operatora logicznego 'and' jako skróconego warunku uruchomienia promocji (zamiast if)
    promoted and _do_promote(db, promoted, class_id)


def _do_promote(db, promoted, class_id):
    promoted.status = "MAIN"
    promoted.position = None
    db.commit()
    _reorder_waiting(db, class_id)


def _reorder_waiting(db, class_id: int):
    """
    Reindeksuje pozycje na liście oczekujących (podejście funkcyjne bez pętli for).
    """
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()
    
    # Brak pętli for - używamy map do zmiany pozycji w kolejce oczekujących
    list(map(lambda pair: _update_position(pair[1], pair[0]), enumerate(waiting)))
    db.commit()


def _update_position(reg, index):
    reg.position = index + 1
