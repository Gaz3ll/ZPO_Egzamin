from models import SessionLocal, FitnessClass, Registration


def register(user_id: str, class_id: int) -> str:
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

        main_count = db.query(Registration).filter(
            Registration.class_id == class_id, Registration.status == "MAIN"
        ).count()

        if main_count < fitness_class.max_capacity:
            db.add(Registration(user_id=user_id, class_id=class_id, status="MAIN", position=None))
            db.commit()
            return "Registered on main list"
        else:
            waiting_count = db.query(Registration).filter(
                Registration.class_id == class_id, Registration.status == "WAITING"
            ).count()
            db.add(Registration(user_id=user_id, class_id=class_id, status="WAITING", position=waiting_count + 1))
            db.commit()
            return f"Registered on waiting list, position {waiting_count + 1}"
    finally:
        db.close()


def unregister(user_id: str, class_id: int) -> str:
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
            _promote_from_waiting(db, class_id)
            return "Unregistered from main list"
        else:
            _reorder_waiting(db, class_id)
            return "Unregistered from waiting list"
    finally:
        db.close()


def _promote_from_waiting(db, class_id: int):
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()

    if waiting:
        promoted = waiting[0]
        promoted.status = "MAIN"
        promoted.position = None
        db.commit()
        _reorder_waiting(db, class_id)


def _reorder_waiting(db, class_id: int):
    waiting = db.query(Registration).filter(
        Registration.class_id == class_id, Registration.status == "WAITING"
    ).order_by(Registration.position).all()

    for i, reg in enumerate(waiting):
        reg.position = i + 1
    db.commit()
