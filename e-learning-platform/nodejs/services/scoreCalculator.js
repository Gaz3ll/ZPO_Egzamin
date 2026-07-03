const CORRECT_POINTS = 1.0;
const WRONG_PENALTY = -0.5;
const SKIP_POINTS = 0.0;
const PASS_THRESHOLD = 0.5;

function calculateScore(totalQuestions, correctCount, wrongCount) {
  return correctCount * CORRECT_POINTS + wrongCount * WRONG_PENALTY +
    (totalQuestions - correctCount - wrongCount) * SKIP_POINTS;
}

function calculatePercentage(score, totalQuestions) {
  const maxScore = totalQuestions * CORRECT_POINTS;
  return Math.max(0, (score / maxScore) * 100);
}

function isPassed(percentage) {
  return percentage > PASS_THRESHOLD * 100;
}

module.exports = { calculateScore, calculatePercentage, isPassed };
