const { calculateScore, calculatePercentage, isPassed } = require('../services/scoreCalculator');

test('all correct', () => {
  const score = calculateScore(5, 5, 0);
  expect(score).toBe(5.0);
  expect(calculatePercentage(score, 5)).toBe(100.0);
  expect(isPassed(100.0)).toBe(true);
});

test('all wrong', () => {
  const score = calculateScore(5, 0, 5);
  expect(score).toBe(-2.5);
  expect(calculatePercentage(score, 5)).toBe(0.0);
  expect(isPassed(0.0)).toBe(false);
});

test('mixed answers', () => {
  const score = calculateScore(4, 2, 1);
  expect(score).toBe(1.5);
  const pct = calculatePercentage(score, 4);
  expect(pct).toBe(37.5);
  expect(isPassed(pct)).toBe(false);
});

test('pass threshold', () => {
  const score = calculateScore(4, 3, 1);
  expect(score).toBe(2.5);
  const pct = calculatePercentage(score, 4);
  expect(pct).toBe(62.5);
  expect(isPassed(pct)).toBe(true);
});

test('below threshold', () => {
  const score = calculateScore(4, 2, 2);
  expect(score).toBe(1.0);
  const pct = calculatePercentage(score, 4);
  expect(pct).toBe(25.0);
  expect(isPassed(pct)).toBe(false);
});

test('all skipped', () => {
  const score = calculateScore(5, 0, 0);
  expect(score).toBe(0.0);
  expect(calculatePercentage(score, 5)).toBe(0.0);
  expect(isPassed(0.0)).toBe(false);
});

test('exactly fifty percent', () => {
  const score = calculateScore(4, 2, 0);
  expect(score).toBe(2.0);
  const pct = calculatePercentage(score, 4);
  expect(pct).toBe(50.0);
  expect(isPassed(pct)).toBe(false);
});

test('negative score', () => {
  const score = calculateScore(2, 0, 2);
  expect(score).toBe(-1.0);
  const pct = calculatePercentage(score, 2);
  expect(pct).toBe(0.0);
  expect(isPassed(pct)).toBe(false);
});
