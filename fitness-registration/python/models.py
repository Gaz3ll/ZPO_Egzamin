from sqlalchemy import Column, Integer, String, create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "sqlite:///./fitness.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class FitnessClass(Base):
    __tablename__ = "fitness_classes"
    id = Column(Integer, primary_key=True, index=True)
    type = Column(String, nullable=False)
    day_of_week = Column(String, nullable=False)
    time = Column(String, nullable=False)
    max_capacity = Column(Integer, nullable=False)


class Registration(Base):
    __tablename__ = "registrations"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String, nullable=False)
    class_id = Column(Integer, nullable=False)
    status = Column(String, nullable=False)  # MAIN or WAITING
    position = Column(Integer, nullable=True)


def init_db():
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
