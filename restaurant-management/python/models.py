from sqlalchemy import Column, Integer, String, create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

DATABASE_URL = "sqlite:///./restaurant.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


class Table(Base):
    """
    Model stolika restauracyjnego określa liczbę miejsc oraz lokalizację.
    """
    __tablename__ = "tables"
    id = Column(Integer, primary_key=True, index=True)
    seats = Column(Integer, nullable=False)
    location = Column(String, nullable=False) # INDOOR / OUTDOOR


class Reservation(Base):
    """
    Model rezerwacji powiązany z danym stolikiem, czasem, liczbą gości i klientem.
    """
    __tablename__ = "reservations"
    id = Column(Integer, primary_key=True, index=True)
    table_id = Column(Integer, nullable=False)
    time = Column(String, nullable=False) # HH:MM
    guests_count = Column(Integer, nullable=False)
    user_id = Column(String, nullable=False)


def init_db():
    """
    Tworzy tabele i dodaje początkowe stoliki (seed) bez użycia if.
    """
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    
    # Funkcyjne seedowanie bez tradycyjnego if
    _seed_data(db) if db.query(Table).count() == 0 else None
    db.close()


def _seed_data(db):
    tables = [
        Table(seats=2, location="INDOOR"),
        Table(seats=4, location="INDOOR"),
        Table(seats=4, location="OUTDOOR"),
        Table(seats=6, location="OUTDOOR"),
        Table(seats=8, location="INDOOR"),
    ]
    db.add_all(tables)
    db.commit()
