// Parametry oceniania quizu
const CORRECT_POINTS = 1.0; // Punkty za poprawną odpowiedź (+1)
const WRONG_PENALTY = -0.5;  // Punkty ujemne za złą odpowiedź (-0.5)
const SKIP_POINTS = 0.0;     // Punkty za pominięcie pytania (0)
const PASS_THRESHOLD = 0.5;  // Próg zaliczenia quizu (>50%)

/**
 * Oblicza łączną punktację za quiz.
 */
function calculateScore(totalQuestions, correctCount, wrongCount) {
  return correctCount * CORRECT_POINTS + wrongCount * WRONG_PENALTY +
    (totalQuestions - correctCount - wrongCount) * SKIP_POINTS;
}

/**
 * Przelicza punkty na wynik procentowy. Zwraca wartość nie mniejszą niż 0.
 */
function calculatePercentage(score, totalQuestions) {
  const maxScore = totalQuestions * CORRECT_POINTS;
  return maxScore === 0 ? 0 : Math.max(0, (score / maxScore) * 100);
}

/**
 * Sprawdza, czy wynik procentowy przekracza próg zaliczenia (>50%).
 */
function isPassed(percentage) {
  return percentage > PASS_THRESHOLD * 100;
}

module.exports = { calculateScore, calculatePercentage, isPassed };
