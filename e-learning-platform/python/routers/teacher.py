from fastapi import APIRouter, Depends, Request
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from models import SessionLocal, QuizResult
from routers.auth import get_current_user, get_teacher_user

router = APIRouter()
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
    db.close()


@router.get("/teacher/results", response_class=HTMLResponse)
def view_results(request: Request, db: Session = Depends(get_db), user: str = Depends(get_teacher_user)):
    results = db.query(QuizResult).all()
    return templates.TemplateResponse("results.html", {"request": request, "results": results})


@router.get("/teacher/api/results")
def api_get_results(db: Session = Depends(get_db), user: str = Depends(get_teacher_user)):
    results = db.query(QuizResult).all()
    return results
