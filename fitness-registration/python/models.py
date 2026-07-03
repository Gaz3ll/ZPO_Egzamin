from sqlalchemy import Column, Integer, String, create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

# Adres bazy danych SQLite dla systemu rejestracji fitness
DATABASE_URL = "sqlite:///./fitness.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Użycie nowoczesnego importu declarative_base z sqlalchemy.orm zamiast sqlalchemy.ext.declarative
Base = declarative_base()


class FitnessClass(Base):
    """
    Model zajęć fitness określający typ, dzień tygodnia, godzinę i maksymalny limit osób (pojemność).
    """
    __tablename__ = "fitness_classes"
    id = Column(Integer, primary_key=True, index=True)
    type = Column(String, nullable=False)
    day_of_week = Column(String, nullable=False)
    time = Column(String, nullable=False)
    max_capacity = Column(Integer, nullable=False)


class Registration(Base):
    """
    Model zapisów reprezentujący uczestnika zajęć fitness.
    Określa status (lista główna - MAIN, lista rezerwowa - WAITING) i pozycję na liście rezerwowej.
    """
    __tablename__ = "registrations"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String, nullable=False)
    class_id = Column(Integer, nullable=False)
    status = Column(String, nullable=False)  # MAIN lub WAITING
    position = Column(Integer, nullable=True) # Pozycja w kolejce rezerwowej (None dla listy głównej)


def init_db():
    """
    Tworzy tabele w bazie danych i dodaje przykładowy harmonogram zajęć (seed).
    """
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    if db.query(FitnessClass).count() == 0:
        classes = [
            FitnessClass(type="Yoga", day_of_week="Monday", time="08:00", max_capacity=20),
            FitnessClass(type="Pilates", day_of_week="Monday", time="10:00", max_capacity=20),
            FitnessClass(type="Spinning", day_of_week="Tuesday", time="09:00", max_capacity=20),
            FitnessClass(type="Zumba", day_of_week="Wednesday", time="17:00", max_capacity=20),
            FitnessClass(type="CrossFit", day_of_week="Thursday", time="18:00", max_capacity=20),
            FitnessClass(type="Boxing", day_of_week="Friday", time="19:00", max_capacity=20),
        ]
        db.add_all(classes)
        db.commit()
    db.close()
