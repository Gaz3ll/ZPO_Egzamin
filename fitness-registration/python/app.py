from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from models import init_db
from routers import fitness

app = FastAPI(title="Fitness Registration API", version="1.0", description="API for fitness class registration with waiting list management")

app.mount("/static", StaticFiles(directory="static"), name="static")

app.include_router(fitness.router)


@app.on_event("startup")
def startup():
    init_db()
