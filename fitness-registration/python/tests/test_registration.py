from models import init_db, SessionLocal, FitnessClass, Registration
from services.registration_service import register, unregister


def setup_module():
    init_db()
    db = SessionLocal()
    db.query(Registration).delete()
    db.query(FitnessClass).delete()
    db.commit()
    fc = FitnessClass(type="Yoga", day_of_week="Monday", time="08:00", max_capacity=2)
    db.add(fc)
    db.commit()
    db.close()


def test_register_main_list():
    msg = register("user1", 1)
    assert msg == "Registered on main list"


def test_register_waiting_list():
    register("user2", 1)
    msg = register("user3", 1)
    assert msg == "Registered on waiting list, position 1"


def test_promotion_from_waiting():
    register("user4", 1)
    db = SessionLocal()
    assert db.query(Registration).filter(Registration.status == "MAIN").count() == 2
    assert db.query(Registration).filter(Registration.status == "WAITING").count() == 2
    db.close()

    msg = unregister("user1", 1)
    assert msg == "Unregistered from main list"

    db = SessionLocal()
    main_list = db.query(Registration).filter(Registration.status == "MAIN").order_by(Registration.id).all()
    assert len(main_list) == 2
    assert main_list[1].user_id == "user3"

    waiting_list = db.query(Registration).filter(Registration.status == "WAITING").order_by(Registration.position).all()
    assert len(waiting_list) == 1
    assert waiting_list[0].user_id == "user4"
    assert waiting_list[0].position == 1
    db.close()


def test_unregister_from_waiting():
    msg = unregister("user4", 1)
    assert msg == "Unregistered from waiting list"

    db = SessionLocal()
    waiting_list = db.query(Registration).filter(Registration.status == "WAITING").order_by(Registration.position).all()
    assert len(waiting_list) == 0
    db.close()
