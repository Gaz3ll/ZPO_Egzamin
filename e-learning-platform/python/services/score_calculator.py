CORRECT_POINTS = 1.0
WRONG_PENALTY = -0.5
SKIP_POINTS = 0.0
PASS_THRESHOLD = 0.5


def calculate_score(total_questions: int, correct_count: int, wrong_count: int) -> float:
    return correct_count * CORRECT_POINTS + wrong_count * WRONG_PENALTY + (total_questions - correct_count - wrong_count) * SKIP_POINTS


def calculate_percentage(score: float, total_questions: int) -> float:
    max_score = total_questions * CORRECT_POINTS
    return max(0.0, (score / max_score) * 100.0)


def is_passed(percentage: float) -> bool:
    return percentage > PASS_THRESHOLD * 100
