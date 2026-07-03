from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()

USERS = {
    "user1": "pass",
    "user2": "pass",
}


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    password = USERS.get(credentials.username)
    return credentials.username if password and password == credentials.password else _unauthorized()


def _unauthorized():
    raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")
