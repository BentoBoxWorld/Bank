package world.bentobox.bank.data;

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
public class MoneyTest {

    Money m;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        m = new Money();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#hashCode()}.
     */
    @Test
    public void testHashCode() {
        assertEquals(0, m.hashCode());
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#Money(double)}.
     */
    @Test
    public void testMoneyDouble() {
        m = new Money(123.45678D);
        assertEquals(123.46D, m.getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#Money()}.
     */
    @Test
    public void testMoney() {
        assertEquals(0D, m.getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#Money(java.math.BigDecimal)}.
     */
    @Test
    public void testMoneyBigDecimal() {
        m = new Money(BigDecimal.valueOf(123.45678));
        assertEquals(123.46D, m.getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#setValue(double)}.
     */
    @Test
    public void testSetValue() {
        m.setValue(12345.6789D);
        assertEquals(12345.68D, m.getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#add(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testAdd() {
        assertEquals(new Money(579), Money.add(new Money(123), new Money(456)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#subtract(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testSubtract() {
        assertEquals(new Money(-333), Money.subtract(new Money(123), new Money(456)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#multiply(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testMultiply() {
        assertEquals(new Money(18), Money.multiply(new Money(3), new Money(6)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#divide(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testDivide() {
        assertEquals(3.71, Money.divide(new Money(456), new Money(123)).getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#lessThan(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testLessThanMoneyMoney() {
        assertFalse(Money.lessThan(new Money(456), new Money(123)));
        assertTrue(Money.lessThan(new Money(456), new Money(1235)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#lessThan(double, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testLessThanDoubleMoney() {
        assertFalse(Money.lessThan(456, new Money(123)));
        assertTrue(Money.lessThan(456, new Money(1235)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#lessThan(world.bentobox.bank.data.Money, double)}.
     */
    @Test
    public void testLessThanMoneyDouble() {
        assertFalse(Money.lessThan(new Money(456), 123));
        assertTrue(Money.lessThan(new Money(456), 1235));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#greaterThan(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testGreaterThan() {
        assertTrue(Money.greaterThan(new Money(456), new Money(123)));
        assertFalse(Money.greaterThan(new Money(456), new Money(1235)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#compareTo(world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testCompareTo() {
        m = new Money(456);
        assertTrue(m.compareTo(new Money(123)) > 0);
        assertEquals(0, m.compareTo(new Money(456)));
        assertTrue(m.compareTo(new Money(1230)) < 0);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#compare(world.bentobox.bank.data.Money, world.bentobox.bank.data.Money)}.
     */
    @Test
    public void testCompare() {
        assertTrue(Money.compare(new Money(456), new Money(123)) > 0);
        assertEquals(0, Money.compare(new Money(456), new Money(456)));
        assertTrue(Money.compare(new Money(456), new Money(1230)) < 0);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#isPositive()}.
     */
    @Test
    public void testIsPositive() {
        assertFalse(m.isPositive());
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#parseMoney(java.lang.String)}.
     */
    @Test(expected = NullPointerException.class)
    public void testParseMoneyNPE() {
        Money.parseMoney(null);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#parseMoney(java.lang.String)}.
     */
    @Test(expected = NumberFormatException.class)
    public void testParseMoneyNFE() {
        Money.parseMoney("tastybento");
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#parseMoney(java.lang.String)}.
     */
    @Test
    public void testParseMoney() {
        assertEquals(123.45, Money.parseMoney("123.45").getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#equals(java.lang.Object)}.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testEqualsObject() {
        assertTrue(m.equals(m));
        assertFalse(m.equals("string"));
        assertFalse(m.equals(null));
        assertFalse(m.equals(new Money(123)));
        m = new Money(345);
        assertTrue(m.equals(new Money(345)));
    }

    /**
     * Test method for {@link world.bentobox.bank.data.Money#toString()}.
     */
    @Test
    public void testToString() {
        m = new Money(1234.56789);
        assertEquals("1234.57", m.toString());
    }

}
