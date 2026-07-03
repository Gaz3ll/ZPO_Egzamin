from fastapi import APIRouter, Depends, Request, Form, Query
from fastapi.responses import HTMLResponse
from fastapi.templating import Jinja2Templates
from sqlalchemy.orm import Session
from models import SessionLocal, Table, Reservation
from services.restaurant_service import find_optimal_table, book_table
from routers.auth import get_current_user, require_role, USERS

router = APIRouter()
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/restaurant", response_class=HTMLResponse)
def show_restaurant(request: Request, db: Session = Depends(get_db), user: str = Depends(get_current_user)):
    is_waiter = (USERS.get(user, {}).get("role") == "WAITER")
    reservations = db.query(Reservation).all() if is_waiter else db.query(Reservation).filter(Reservation.user_id == user).all()
    return templates.TemplateResponse("restaurant.html", {
        "request": request,
        "reservations": reservations,
        "user": user,
        "is_waiter": is_waiter,
        "search_result": None,
        "time": "",
        "guests_count": "",
        "message": None
    })


@router.post("/restaurant/search", response_class=HTMLResponse)
def search_table(request: Request, time: str = Form(...), guests_count: int = Form(...), db: Session = Depends(get_db), user: str = Depends(get_current_user)):
    optimal_table = find_optimal_table(guests_count, time)
    is_waiter = (USERS.get(user, {}).get("role") == "WAITER")
    reservations = db.query(Reservation).all() if is_waiter else db.query(Reservation).filter(Reservation.user_id == user).all()
    msg = None if optimal_table else "Brak wolnych stolików o podanej godzinie dla tylu osób."
    return templates.TemplateResponse("restaurant.html", {
        "request": request,
        "reservations": reservations,
        "user": user,
        "is_waiter": is_waiter,
        "search_result": optimal_table,
        "time": time,
        "guests_count": guests_count,
        "message": msg
    })


@router.post("/restaurant/book", response_class=HTMLResponse)
def book_table_ui(request: Request, table_id: int = Form(...), time: str = Form(...), guests_count: int = Form(...), db: Session = Depends(get_db), user: str = Depends(require_role("GUEST"))):
    message = book_table(user, table_id, time, guests_count)
    reservations = db.query(Reservation).filter(Reservation.user_id == user).all()
    return templates.TemplateResponse("restaurant.html", {
        "request": request,
        "reservations": reservations,
        "user": user,
        "is_waiter": False,
        "search_result": None,
        "time": "",
        "guests_count": "",
        "message": message
    })


@router.get("/api/restaurant/tables/search")
def api_search_table(
    guests_count: int = Query(..., description="Liczba gości"),
    time: str = Query(..., description="Godzina rezerwacji (np. 18:00)"),
    user: str = Depends(get_current_user)
):
    """
    Wyszukuje pierwszy pasujący, najmniejszy wolny stolik spełniający podane kryteria (Best-Fit).
    """
    table = find_optimal_table(guests_count, time)
    return {"table": table}
