package world.bentobox.bank.data;

public class AccountHistory {
    private final long timestamp;
    private final String name;
    private final double amount;
    private final TxType type;
    /**
     * @param timestamp
     * @param name
     * @param amount
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