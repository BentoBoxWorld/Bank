package world.bentobox.bank.data;

public class AccountHistory {
    private final long timestamp;
    private final String name;
    private final double amount;
    /**
     * @param timestamp
     * @param name
     * @param amount
     */
    public AccountHistory(long timestamp, String name, double amount) {
        super();
        this.timestamp = timestamp;
        this.name = name;
        this.amount = amount;
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

}