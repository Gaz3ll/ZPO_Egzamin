from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()

USERS = {
    "guest1": {"password": "pass", "role": "GUEST"},
    "guest2": {"password": "pass", "role": "GUEST"},
    "waiter1": {"password": "pass", "role": "WAITER"},
}


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    return credentials.username if user and user["password"] == credentials.password else _unauthorized()


def require_role(role: str):
    def dependency(credentials: HTTPBasicCredentials = Depends(security)) -> str:
        user = USERS.get(credentials.username)
        return credentials.username if user and user["password"] == credentials.password and user["role"] == role else _forbidden()
    return dependency


def _unauthorized():
    raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid credentials")


def _forbidden():
    raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access forbidden for this role")
