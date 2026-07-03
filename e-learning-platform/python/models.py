from sqlalchemy import Column, Integer, String, Float, Boolean, create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

DATABASE_URL = "sqlite:///./elearning.db"
engine = create_engine(DATABASE_URL, connect_args={"check_same_thread": False})
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()


class Question(Base):
    __tablename__ = "questions"
    id = Column(Integer, primary_key=True, index=True)
    content = Column(String, nullable=False)
    correct_answer = Column(String, nullable=False)
    points = Column(Integer, default=1)
    option_a = Column(String, nullable=False)
    option_b = Column(String, nullable=False)
    option_c = Column(String, nullable=False)
    option_d = Column(String, nullable=False)


class QuizResult(Base):
    __tablename__ = "quiz_results"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(String, nullable=False)
    score = Column(Float, nullable=False)
    percentage = Column(Float, nullable=False)
    passed = Column(Boolean, nullable=False)


def init_db():
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    if db.query(Question).count() == 0:
        questions = [
            Question(content="What is the capital of France?", correct_answer="B", points=1,
                     option_a="London", option_b="Paris", option_c="Berlin", option_d="Madrid"),
            Question(content="What is 2 + 2?", correct_answer="A", points=1,
                     option_a="4", option_b="5", option_c="3", option_d="6"),
            Question(content="Which planet is known as the Red Planet?", correct_answer="C", points=1,
                     option_a="Venus", option_b="Jupiter", option_c="Mars", option_d="Saturn"),
            Question(content="What is the largest mammal?", correct_answer="D", points=1,
                     option_a="Elephant", option_b="Giraffe", option_c="Shark", option_d="Blue Whale"),
            Question(content="Which gas do plants absorb?", correct_answer="B", points=1,
                     option_a="Oxygen", option_b="Carbon Dioxide", option_c="Nitrogen", option_d="Hydrogen"),
        ]
        db.add_all(questions)
        db.commit()
    db.close()
