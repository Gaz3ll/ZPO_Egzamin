from sqlalchemy import Column, Integer, String, Float, create_engine
from sqlalchemy.orm import declarative_base, sessionmaker

DATABASE_URL = "sqlite:///./diet.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


class Meal(Base):
    """
    Model posiłku zawierający makroskładniki i kalorie.
    """
    __tablename__ = "meals"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)
    proteins = Column(Float, nullable=False)
    carbs = Column(Float, nullable=False)
    fats = Column(Float, nullable=False)
    calories = Column(Float, nullable=False)


class DailyLog(Base):
    """
    Dziennik spożycia posiłków przez konkretnego użytkownika w danym dniu.
    """
    __tablename__ = "daily_logs"
    id = Column(Integer, primary_key=True, index=True)
    date = Column(String, nullable=False) # YYYY-MM-DD
    meal_id = Column(Integer, nullable=False)
    user_id = Column(String, nullable=False)


class UserProfile(Base):
    """
    Profil użytkownika przechowujący wagę, wzrost, wiek i płeć do BMR.
    """
    __tablename__ = "user_profiles"
    user_id = Column(String, primary_key=True)
    weight = Column(Float, nullable=False)
    height = Column(Float, nullable=False)
    age = Column(Integer, nullable=False)
    gender = Column(String, nullable=False) # M / F


def init_db():
    """
    Inicjalizacja tabel i seedowanie posiłków bez pętli i ifów.
    """
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    
    # Funkcyjne seedowanie
    _seed_meals(db) if db.query(Meal).count() == 0 else None
    db.close()


def _seed_meals(db):
    meals = [
        Meal(name="Jajecznica", proteins=15.0, carbs=1.0, fats=12.0, calories=172.0),
        Meal(name="Pierś z kurczaka z ryżem", proteins=40.0, carbs=60.0, fats=8.0, calories=472.0),
        Meal(name="Odżywka białkowa", proteins=24.0, carbs=3.0, fats=2.0, calories=126.0),
        Meal(name="Sałatka Cezar", proteins=12.0, carbs=10.0, fats=18.0, calories=250.0),
    ]
    db.add_all(meals)
    db.commit()
