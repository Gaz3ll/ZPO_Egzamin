from pydantic import BaseModel
from typing import Dict


class QuizSubmitRequest(BaseModel):
    answers: Dict[str, str] = {}


class QuizResultResponse(BaseModel):
    score: float
    percentage: float
    passed: bool
    correct: int
    wrong: int
    total: int
