package pl.zpo.app.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable monetary value object (amount + ISO currency code). Used by the domain
 * algorithm and breakdown to compute values with correct rounding, independent of how
 * the amount is stored in the database (a plain {@code BigDecimal} column).
 *
 * @param amount   monetary amount, always scaled to 2 decimals (HALF_UP)
 * @param currency ISO-4217 currency code, e.g. {@code "PLN"}
 */
public record Money(BigDecimal amount, String currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, String currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(amount.multiply(factor), currency);
    }

    public Money multiply(long factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: %s vs %s".formatted(currency, other.currency));
        }
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency;
    }
}
