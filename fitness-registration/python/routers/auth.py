import hashlib
import secrets
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


USERS = {
    "user1": {"password_hash": hash_password("pass"), "role": "USER"},
    "user2": {"password_hash": hash_password("pass"), "role": "USER"},
}


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    user = USERS.get(credentials.username)
    password_ok = user is not None and secrets.compare_digest(user["password_hash"], hash_password(credentials.password))
    return credentials.username if password_ok else _unauthorized()


def _unauthorized():
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid credentials",
        headers={"WWW-Authenticate": "Basic"},
    )
