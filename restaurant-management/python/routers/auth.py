import hashlib
import secrets
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBasic, HTTPBasicCredentials

security = HTTPBasic()


def hash_password(password: str) -> str:
    return hashlib.sha256(password.encode()).hexdigest()


USERS = {
    "guest1": {"password_hash": hash_password("pass"), "role": "GUEST"},
    "guest2": {"password_hash": hash_password("pass"), "role": "GUEST"},
    "waiter1": {"password_hash": hash_password("pass"), "role": "WAITER"},
    "waiter2": {"password_hash": hash_password("pass"), "role": "WAITER"},
}


def verify_user(credentials: HTTPBasicCredentials) -> str:
    """
    Weryfikuje dane logowania użytkownika.
    Używa secrets.compare_digest zamiast == dla bezpiecznego porównywania (timing-safe).
    """
    user = USERS.get(credentials.username)
    password_ok = (
        user is not None and
        secrets.compare_digest(user["password_hash"], hash_password(credentials.password))
    )
    return credentials.username if password_ok else _unauthorized()


def get_current_user(credentials: HTTPBasicCredentials = Depends(security)) -> str:
    return verify_user(credentials)


def require_role(role: str):
    def dependency(credentials: HTTPBasicCredentials = Depends(security)) -> str:
        username = verify_user(credentials)
        user = USERS.get(username)
        return username if user and user["role"] == role else _forbidden()
    return dependency


def _unauthorized():
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Invalid credentials",
        headers={"WWW-Authenticate": "Basic"},
    )


def _forbidden():
    raise HTTPException(
        status_code=status.HTTP_403_FORBIDDEN,
        detail="Access forbidden for this role",
    )
