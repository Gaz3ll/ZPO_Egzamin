# Parametry punktacji quizu:
# +1.0 pkt za poprawną odpowiedź, -0.5 pkt za złą odpowiedź, 0 pkt za pominiętą.
# Próg zaliczenia wynosi >50%.
CORRECT_POINTS = 1.0
WRONG_PENALTY = -0.5
SKIP_POINTS = 0.0
PASS_THRESHOLD = 0.5


def calculate_score(total_questions: int, correct_count: int, wrong_count: int) -> float:
    """
    Oblicza wynik punktowy za odpowiedzi w quizie wielokrotnego wyboru.
    Wzór: (poprawne * 1.0) + (błędne * -0.5) + (pominięte * 0.0)
    """
    return correct_count * CORRECT_POINTS + wrong_count * WRONG_PENALTY + (total_questions - correct_count - wrong_count) * SKIP_POINTS


def calculate_percentage(score: float, total_questions: int) -> float:
    """
    Przelicza wynik punktowy na wynik procentowy.
    Jeśli wynik punktowy jest ujemny, zwraca 0.0%.
    """
    max_score = total_questions * CORRECT_POINTS
    return 0.0 if max_score == 0 else max(0.0, (score / max_score) * 100.0)


def is_passed(percentage: float) -> bool:
    """
    Sprawdza, czy wynik procentowy przekracza próg zaliczenia (>50%).
    """
    return percentage > PASS_THRESHOLD * 100
