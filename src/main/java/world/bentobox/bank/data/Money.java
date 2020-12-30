package world.bentobox.bank.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

import com.google.gson.annotations.Expose;

/**
 * Represents money to two decimal places
 * @author tastybento
 *
 */
public class Money implements Comparable<Money> {

    @Expose
    private BigDecimal value;

    public Money(double value) {
        setValue(value);
    }

    public Money() {
        value = BigDecimal.ZERO;
    }

    public Money(BigDecimal bigDecimal) {
        value = bigDecimal.setScale(2, RoundingMode.HALF_DOWN);
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value.doubleValue();
    }

    /**
     * @param d the value to set
     */
    public void setValue(double d) {
        this.value = new BigDecimal(d).setScale(2, RoundingMode.HALF_DOWN);
    }

    public static Money add(@NonNull Money value1, @NonNull Money value2) {
        return new Money(value1.getValue() + value2.getValue());
    }

    public static Money subtract(@NonNull Money value1, @NonNull Money value2) {
        return new Money(value1.getValue() - value2.getValue());
    }

    public static Money multiply(@NonNull Money value1, @NonNull Money value2) {
        return new Money(value1.getValue() * value2.getValue());
    }

    public static Money divide(@NonNull Money value1, @NonNull Money value2) {
        return new Money(value1.getValue() / value2.getValue());
    }

    public static boolean lessThan(@NonNull Money value1, @NonNull Money value2) {
        Objects.requireNonNull(value1);
        Objects.requireNonNull(value2);
        return value1.getValue() < value2.getValue();
    }

    public static boolean lessThan(double value1, @NonNull Money value2) {
        return value1 < value2.getValue();
    }

    public static boolean lessThan(@NonNull Money value1, double value2) {
        return value1.getValue() < value2;
    }

    public static boolean greaterThan(@NonNull Money value1, @NonNull Money value2) {
        return value1.getValue() > value2.getValue();
    }


    @Override
    public int compareTo(Money o) {
        return Money.compare(this, o);
    }

    /**
     * Compares the two specified {@code Money} values.
     *
     * @param   m1        the first {@code Money} to compare
     * @param   m2        the second {@code Money} to compare
     * @return  the value {@code 0} if {@code m1} is
     *          numerically equal to {@code m2}; a value less than
     *          {@code 0} if {@code d1} is numerically less than
     *          {@code m2}; and a value greater than {@code 0}
     *          if {@code m1} is numerically greater than
     *          {@code m2}.
     */
    public static int compare(Money m1, Money m2) {
        return m1.value.compareTo(m2.value);
    }

    /**
     * @return {@code true} if the value is > 0
     */
    public boolean isPositive() {
        return value.compareTo(BigDecimal.ZERO) == 1;
    }

    /**
     * Returns a new {@code Money} initialized to the value
     * represented by the specified {@code String}.
     *
     * @param  arg   the string to be parsed.
     * @return the {@code Money} value represented by the string
     *         argument.
     * @throws NullPointerException  if the string is null
     * @throws NumberFormatException if the string does not contain
     *         a parsable {@code Money}.
     */
    public static Money parseMoney(String arg) {
        return new Money(new BigDecimal(arg));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money)) {
            return false;
        }
        Money other = (Money) obj;
        return value.equals(other.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
