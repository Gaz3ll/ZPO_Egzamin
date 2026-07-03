import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from models import init_db, SessionLocal, Table, Reservation
from services.restaurant_service import find_optimal_table, book_table


def setup_module():
    init_db()
    db = SessionLocal()
    db.query(Reservation).delete()
    db.query(Table).delete()
    db.commit()
    
    # Seeding tables
    db.add_all([
        Table(id=1, seats=2, location="INDOOR"),
        Table(id=2, seats=4, location="INDOOR"),
        Table(id=3, seats=4, location="OUTDOOR"),
        Table(id=4, seats=8, location="INDOOR"),
    ])
    db.commit()
    db.close()


def test_optimal_table_best_fit():
    # Szukamy dla 3 osób o 18:00
    # Powinien dobrać stolik 4-osobowy (id: 2 lub 3), a nie 8-osobowy lub za mały 2-osobowy
    optimal = find_optimal_table(3, "18:00")
    assert optimal is not None
    assert optimal.seats == 4
    assert optimal.id in [2, 3]


def test_reserved_table_skip():
    # Zarezerwujmy stoliki 4-osobowe o 19:00
    db = SessionLocal()
    db.add(Reservation(table_id=2, time="19:00", guests_count=3, user_id="guest1"))
    db.add(Reservation(table_id=3, time="19:00", guests_count=4, user_id="guest2"))
    db.commit()
    db.close()

    # Szukamy dla 3 osób o 19:00. Skoro stoliki 4-osobowe są zajęte, optymalny musi być stolik 8-osobowy (id: 4)
    optimal = find_optimal_table(3, "19:00")
    assert optimal is not None
    assert optimal.id == 4


def test_no_matching_table():
    # Szukamy stolika dla 10 osób
    optimal = find_optimal_table(10, "20:00")
    assert optimal is None


def test_book_table_prevent_duplicates():
    # Pierwsza rezerwacja powinna przejść
    res1 = book_table("guest1", 1, "20:00", 2)
    assert res1 == "Reservation successful"

    # Druga na ten sam czas i stolik powinna zostać odrzucona
    res2 = book_table("guest2", 1, "20:00", 2)
    assert res2 == "Table already reserved"
