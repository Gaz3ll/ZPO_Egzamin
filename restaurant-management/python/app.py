from fastapi import FastAPI
from models import init_db
from routers import restaurant

app = FastAPI(
    title="Restaurant Management API",
    version="1.0",
    description="API for optimal table matching and reservation management"
)

app.include_router(restaurant.router)


@app.on_event("startup")
def startup():
    init_db()
