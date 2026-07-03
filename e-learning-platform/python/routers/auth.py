import hashlib
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


USERS = {
    "student1": {"password_hash": hash_password("pass"), "role": "STUDENT"},
    "teacher1": {"password_hash": hash_password("pass"), "role": "TEACHER"},
}


def verify_user(credentials: HTTPBasicCredentials) -> str:
    user = USERS.get(credentials.username)
    return credentials.username if user and user["password_hash"] == hash_password(credentials.password) else _unauthorized()


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    return verify_user(credentials)


def get_teacher_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    username = verify_user(credentials)
    user = USERS.get(username)
    return username if user["role"] == "TEACHER" else _forbidden("Teacher access required")


def require_student(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    username = verify_user(credentials)
    user = USERS.get(username)
    return username if user["role"] == "STUDENT" else _forbidden("Student access required")


def _unauthorized():
    raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")


def _forbidden(msg: str):
    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=msg)
