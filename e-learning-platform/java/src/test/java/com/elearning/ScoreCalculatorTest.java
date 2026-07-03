package com.elearning;

import com.elearning.service.ScoreCalculator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void testAllCorrect() {
        double score = calculator.calculateScore(5, 5, 0);
        assertEquals(5.0, score, 0.001);
        assertEquals(100.0, calculator.calculatePercentage(score, 5), 0.001);
        assertTrue(calculator.isPassed(100.0));
    }

    @Test
    void testAllWrong() {
        double score = calculator.calculateScore(5, 0, 5);
        assertEquals(-2.5, score, 0.001);
        assertEquals(0.0, calculator.calculatePercentage(score, 5), 0.001);
        assertFalse(calculator.isPassed(0.0));
    }

    @Test
    void testMixedAnswers() {
        double score = calculator.calculateScore(4, 2, 1);
        assertEquals(1.5, score, 0.001);
        double pct = calculator.calculatePercentage(score, 4);
        assertEquals(37.5, pct, 0.001);
        assertFalse(calculator.isPassed(pct));
    }

    @Test
    void testPassThreshold() {
        double score = calculator.calculateScore(4, 3, 1);
        assertEquals(2.5, score, 0.001);
        double pct = calculator.calculatePercentage(score, 4);
        assertEquals(62.5, pct, 0.001);
        assertTrue(calculator.isPassed(pct));
    }

    @Test
    void testBelowThreshold() {
        double score = calculator.calculateScore(4, 2, 2);
        assertEquals(1.0, score, 0.001);
        double pct = calculator.calculatePercentage(score, 4);
        assertEquals(25.0, pct, 0.001);
        assertFalse(calculator.isPassed(pct));
    }

    @Test
    void testAllSkipped() {
        double score = calculator.calculateScore(5, 0, 0);
        assertEquals(0.0, score, 0.001);
        assertEquals(0.0, calculator.calculatePercentage(score, 5), 0.001);
        assertFalse(calculator.isPassed(0.0));
    }

    @Test
    void testExactlyFiftyPercent() {
        double score = calculator.calculateScore(4, 2, 0);
        assertEquals(2.0, score, 0.001);
        double pct = calculator.calculatePercentage(score, 4);
        assertEquals(50.0, pct, 0.001);
        assertFalse(calculator.isPassed(pct));
    }

    @Test
    void testNegativeScore() {
        double score = calculator.calculateScore(2, 0, 2);
        assertEquals(-1.0, score, 0.001);
        double pct = calculator.calculatePercentage(score, 2);
        assertEquals(0.0, pct, 0.001);
        assertFalse(calculator.isPassed(pct));
    }
}
