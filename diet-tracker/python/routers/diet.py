from fastapi import APIRouter, Depends, Request, Form, Query
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from datetime import date as date_class
from models import SessionLocal, Meal, DailyLog, UserProfile
from services.diet_service import get_daily_summary
from routers.auth import get_current_user

router = APIRouter()
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/diet", response_class=HTMLResponse)
def show_diet(request: Request, date: str = None, db: Session = Depends(get_db), user: str = Depends(get_current_user)):
    target_date = date or str(date_class.today())
    profile = db.query(UserProfile).filter(UserProfile.user_id == user).first() or UserProfile(
        user_id=user, weight=70.0, height=175.0, age=25, gender="M"
    )
    
    meals = db.query(Meal).all()
    
    logs = db.query(DailyLog).filter(DailyLog.user_id == user, DailyLog.date == target_date).all()
    meal_ids = list(map(lambda l: l.meal_id, logs))
    logged_meals = db.query(Meal).filter(Meal.id.in_(meal_ids)).all() if meal_ids else []

    summary = get_daily_summary(user, target_date)

    return templates.TemplateResponse("diet.html", {
        "request": request,
        "user": user,
        "profile": profile,
        "meals": meals,
        "loggedMeals": logged_meals,
        "date": target_date,
        "summary": summary,
        "message": None
    })


@router.post("/diet/meal/add")
def add_meal_ui(date: str = Form(...), meal_id: int = Form(...), db: Session = Depends(get_db), user: str = Depends(get_current_user)):
    db.add(DailyLog(date=date, meal_id=meal_id, user_id=user))
    db.commit()
    return RedirectResponse(url=f"/diet?date={date}", status_code=303)


@router.post("/diet/profile/update")
def update_profile_ui(
    weight: float = Form(...),
    height: float = Form(...),
    age: int = Form(...),
    gender: str = Form(...),
    date: str = Form(...),
    db: Session = Depends(get_db),
    user: str = Depends(get_current_user)
):
    profile = db.query(UserProfile).filter(UserProfile.user_id == user).first()
    
    # Wykorzystujemy zaktualizowanie lub wstawienie nowego profilu
    db.merge(UserProfile(user_id=user, weight=weight, height=height, age=age, gender=gender))
    db.commit()
    
    return RedirectResponse(url=f"/diet?date={date}", status_code=303)


@router.get("/api/diet/logs/summary")
def api_get_summary(
    date: str = Query(None, description="Data w formacie YYYY-MM-DD"),
    user: str = Depends(get_current_user)
):
    """
    Pobiera podsumowanie makroskładników oraz realizację zapotrzebowania BMR dla danego dnia.
    """
    target_date = date or str(date_class.today())
    return get_daily_summary(user, target_date)
