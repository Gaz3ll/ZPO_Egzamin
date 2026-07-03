from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from models import init_db
from routers import quiz, teacher

app = FastAPI(title="E-Learning API", version="1.0", description="API for quiz submission and grading")

app.mount("/static", StaticFiles(directory="static"), name="static")

app.include_router(quiz.router)
app.include_router(teacher.router)


@app.on_event("startup")
def startup():
    init_db()
