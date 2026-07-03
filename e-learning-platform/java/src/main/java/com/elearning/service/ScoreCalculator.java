package com.elearning.service;

import org.springframework.stereotype.Component;

/**
 * Kalkulator punktacji i oceny końcowej dla quizów wielokrotnego wyboru.
 * Uwzględnia punkty ujemne za błędne odpowiedzi.
 */
@Component
public class ScoreCalculator {

    // Konfiguracja punktacji zgodna z wymaganiami
    private static final double CORRECT_POINTS = 1.0; // +1 pkt za poprawną odpowiedź
    private static final double WRONG_PENALTY = -0.5;  // -0.5 pkt za błędną odpowiedź
    private static final double SKIP_POINTS = 0.0;     // 0 pkt za brak odpowiedzi
    private static final double PASS_THRESHOLD = 0.5;  // Próg zaliczenia: >50% punktów maksymalnych

    /**
     * Oblicza końcowy wynik punktowy na podstawie liczby poprawnych i błędnych odpowiedzi.
     */
    public double calculateScore(int totalQuestions, int correctCount, int wrongCount) {
        return correctCount * CORRECT_POINTS + wrongCount * WRONG_PENALTY + (totalQuestions - correctCount - wrongCount) * SKIP_POINTS;
    }

    /**
     * Przelicza zdobyte punkty na wynik procentowy w odniesieniu do maksymalnego możliwego wyniku.
     * Zwraca wynik nie mniejszy niż 0%.
     */
    public double calculatePercentage(double score, int totalQuestions) {
        double maxScore = totalQuestions * CORRECT_POINTS;
        return maxScore == 0 ? 0.0 : Math.max(0, (score / maxScore) * 100.0);
    }

    /**
     * Określa, czy uzyskany wynik procentowy pozwala na zaliczenie quizu (próg > 50%).
     */
    public boolean isPassed(double percentage) {
        return percentage > PASS_THRESHOLD * 100;
    }
}
