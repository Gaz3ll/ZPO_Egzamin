from models import SessionLocal, Table, Reservation

def find_optimal_table(guests_count: int, time: str) -> Table:
    """
    Wyszukuje pierwszy pasujący, najmniejszy wolny stolik dla danej liczby gości i godziny (podejście funkcyjne).
    """
    db = SessionLocal()
    try:
        tables = db.query(Table).all()
        reservations = db.query(Reservation).filter(Reservation.time == time).all()
        
        # Sprawdzamy zajętość stolika w podejściu funkcyjnym bez if/for
        is_reserved = lambda table_id: any(map(lambda r: r.table_id == table_id, reservations))
        
        # Filtrujemy wolne stoliki spełniające wymagania pojemności
        available = list(filter(lambda t: t.seats >= guests_count and not is_reserved(t.id), tables))
        
        # Zwracamy najmniejszy dopasowany stolik (Best-Fit) lub None
        return next(iter(sorted(available, key=lambda t: t.seats)), None)
    finally:
        db.close()


def book_table(user_id: str, table_id: int, time: str, guests_count: int) -> str:
    """
    Rezerwuje stolik (podejście funkcyjne bez if i pętli).
    """
    db = SessionLocal()
    try:
        table = db.query(Table).filter(Table.id == table_id).first()
        existing_table = db.query(Reservation).filter(Reservation.table_id == table_id, Reservation.time == time).first()
        existing_user = db.query(Reservation).filter(Reservation.user_id == user_id, Reservation.time == time).first()
        
        return "Table not found" if not table else \
               ("Table already reserved" if existing_table else \
                ("User already has a reservation at this time" if existing_user else \
                 ("Not enough seats" if table.seats < guests_count else \
                  _create_booking(db, user_id, table_id, time, guests_count))))
    finally:
        db.close()


def _create_booking(db, user_id, table_id, time, guests_count):
    db.add(Reservation(user_id=user_id, table_id=table_id, time=time, guests_count=guests_count))
    db.commit()
    return "Reservation successful"
