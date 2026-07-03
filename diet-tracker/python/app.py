from fastapi import FastAPI
from models import init_db
from routers import diet

app = FastAPI(
    title="Diet Tracker API",
    version="1.0",
    description="API for calorie logging and Harris-Benedict BMR calculation"
)

app.include_router(diet.router)


@app.on_event("startup")
def startup():
    init_db()
