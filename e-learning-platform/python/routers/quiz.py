from fastapi import APIRouter, Depends, Request, Form
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from models import SessionLocal, Question, QuizResult
from schemas import QuizSubmitRequest, QuizResultResponse
from services.score_calculator import calculate_score, calculate_percentage, is_passed
from routers.auth import get_current_user, require_student

router = APIRouter()
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/quiz", response_class=HTMLResponse)
def show_quiz(request: Request, db: Session = Depends(get_db), user: str = Depends(get_current_user)):
    questions = db.query(Question).all()
    return templates.TemplateResponse("quiz.html", {"request": request, "questions": questions})


@router.post("/quiz/submit", response_class=HTMLResponse)
async def submit_quiz(request: Request, db: Session = Depends(get_db), user: str = Depends(require_student)):
    form = await request.form()
    questions = db.query(Question).all()
    form_dict = dict(form)
    answered = list(filter(lambda q: str(q.id) in form_dict and form_dict[str(q.id)], questions))
    correct = sum(map(lambda q: int(form_dict[str(q.id)] == q.correct_answer), answered))
    wrong = len(answered) - correct
    score = calculate_score(len(questions), correct, wrong)
    percentage = calculate_percentage(score, len(questions))
    passed = is_passed(percentage)
    result = QuizResult(user_id=user, score=score, percentage=percentage, passed=passed)
    db.add(result)
    db.commit()
    resp = QuizResultResponse(score=score, percentage=percentage, passed=passed, correct=correct, wrong=wrong, total=len(questions))
    return templates.TemplateResponse("summary.html", {"request": request, "result": resp})


@router.post("/api/quiz/submit")
def api_submit_answers(req: QuizSubmitRequest, db: Session = Depends(get_db), user: str = Depends(require_student)):
    questions = db.query(Question).all()
    answers_dict = req.answers or {}
    answered = list(filter(lambda q: str(q.id) in answers_dict and answers_dict[str(q.id)], questions))
    correct = sum(map(lambda q: int(answers_dict[str(q.id)] == q.correct_answer), answered))
    wrong = len(answered) - correct
    score = calculate_score(len(questions), correct, wrong)
    percentage = calculate_percentage(score, len(questions))
    passed = is_passed(percentage)
    result = QuizResult(user_id=user, score=score, percentage=percentage, passed=passed)
    db.add(result)
    db.commit()
    return QuizResultResponse(score=score, percentage=percentage, passed=passed, correct=correct, wrong=wrong, total=len(questions))
