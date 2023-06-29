package com.food.ordering.system.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Money {
    // It's final (immutable), so I need to set the value for amount in constructor.
    private final BigDecimal amount;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    // Constructor
    public Money(BigDecimal amount) {
        this.amount = amount;
    }

    // Returns true if the amount is greater than 0
    public boolean isGreaterThanZero() {
        return this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Compares a money value with the amount
    public boolean isGreaterThan(Money money) {
        return this.amount != null && this.amount.compareTo(money.getAmount()) > 0;
    }

    // Adds money to the amount
    public Money add(Money money) {
        return new Money(setScale(this.amount.add(money.getAmount())));
    }

    // Subtracts money from the amount
    public Money subract(Money money) {
        return new Money(setScale(this.amount.subtract(money.getAmount())));
    }

    // Utility method to set scale in Big Decimal number
    private BigDecimal setScale(BigDecimal input) {
        return input.setScale(2, RoundingMode.HALF_EVEN);
    }

    // Multiplys the money
    public Money multiply(int multiplier){
        return new Money(setScale(this.amount.multiply(new BigDecimal(multiplier))));
    }

    // Getter
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return amount.equals(money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount);
    }
}
