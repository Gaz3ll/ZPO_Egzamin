from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()

USERS = {
    "student1": {"password": "pass", "role": "STUDENT"},
    "teacher1": {"password": "pass", "role": "TEACHER"},
}


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    if not user or user["password"] != credentials.password:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    return credentials.username


def get_teacher_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    if not user or user["password"] != credentials.password:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    if user["role"] != "TEACHER":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Teacher access required")
    return credentials.username


def require_student(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    if not user or user["password"] != credentials.password:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    if user["role"] != "STUDENT":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Student access required")
    return credentials.username
