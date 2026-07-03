from services.score_calculator import calculate_score, calculate_percentage, is_passed


def test_all_correct():
    score = calculate_score(5, 5, 0)
    assert score == 5.0
    assert calculate_percentage(score, 5) == 100.0
    assert is_passed(100.0) is True


def test_all_wrong():
    score = calculate_score(5, 0, 5)
    assert score == -2.5
    assert calculate_percentage(score, 5) == 0.0
    assert is_passed(0.0) is False


def test_mixed():
    score = calculate_score(4, 2, 1)
    assert score == 1.5
    pct = calculate_percentage(score, 4)
    assert pct == 37.5
    assert is_passed(pct) is False


def test_pass_threshold():
    score = calculate_score(4, 3, 1)
    assert score == 2.5
    pct = calculate_percentage(score, 4)
    assert pct == 62.5
    assert is_passed(pct) is True


def test_below_threshold():
    score = calculate_score(4, 2, 2)
    assert score == 1.0
    pct = calculate_percentage(score, 4)
    assert pct == 25.0
    assert is_passed(pct) is False


def test_all_skipped():
    score = calculate_score(5, 0, 0)
    assert score == 0.0
    assert calculate_percentage(score, 5) == 0.0
    assert is_passed(0.0) is False


def test_exactly_fifty_percent():
    score = calculate_score(4, 2, 0)
    assert score == 2.0
    pct = calculate_percentage(score, 4)
    assert pct == 50.0
    assert is_passed(pct) is False


def test_negative_score():
    score = calculate_score(2, 0, 2)
    assert score == -1.0
    pct = calculate_percentage(score, 2)
    assert pct == 0.0
    assert is_passed(pct) is False
