package world.bentobox.bank.data;

/**
 * Account history entry class
 * @author tastybento
 *
 */
public class AccountHistory {
    private final long timestamp;
    private final String name;
    private final double amount;
    private final TxType type;
    /**
     * Account history entry
     * @param timestamp - time stamp
     * @param name - name of user making the change
     * @param amount - amount of change
     * @param type - type of change
     */
    public AccountHistory(long timestamp, String name, double amount, TxType type) {
        super();
        this.timestamp = timestamp;
        this.name = name;
        this.amount = amount;
        this.type = type;
    }
    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }
    /**
     * @return the type
     */
    public TxType getType() {
        return type;
    }

}