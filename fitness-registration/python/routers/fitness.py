from fastapi import APIRouter, Depends, Request
from fastapi.responses import HTMLResponse, RedirectResponse
from fastapi.templating import Jinja2Templates
from models import SessionLocal, FitnessClass, Registration
from services.registration_service import register, unregister
from routers.auth import get_current_user

router = APIRouter()
templates = Jinja2Templates(directory="templates")


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@router.get("/fitness", response_class=HTMLResponse)
def show_classes(request: Request, user: str = Depends(get_current_user)):
    db = SessionLocal()
    classes = db.query(FitnessClass).all()
    db.close()
    return templates.TemplateResponse("fitness.html", {"request": request, "classes": classes, "message": None})


@router.post("/fitness/register/{class_id}", response_class=HTMLResponse)
def register_for_class(request: Request, class_id: int, user: str = Depends(get_current_user)):
    message = register(user, class_id)
    db = SessionLocal()
    classes = db.query(FitnessClass).all()
    db.close()
    return templates.TemplateResponse("fitness.html", {"request": request, "classes": classes, "message": message})


@router.post("/fitness/unregister/{class_id}", response_class=HTMLResponse)
def unregister_from_class(request: Request, class_id: int, user: str = Depends(get_current_user)):
    message = unregister(user, class_id)
    db = SessionLocal()
    classes = db.query(FitnessClass).all()
    db.close()
    return templates.TemplateResponse("fitness.html", {"request": request, "classes": classes, "message": message})


@router.post("/api/fitness/register/{class_id}")
def api_register(class_id: int, user: str = Depends(get_current_user)):
    message = register(user, class_id)
    return {"message": message}


@router.post("/api/fitness/unregister/{class_id}")
def api_unregister(class_id: int, user: str = Depends(get_current_user)):
    message = unregister(user, class_id)
    return {"message": message}
