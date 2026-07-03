from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()

USERS = {
    "user1": {"password": "pass", "role": "USER"},
    "user2": {"password": "pass", "role": "USER"},
}


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    if not user or user["password"] != credentials.password:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
    return credentials.username
