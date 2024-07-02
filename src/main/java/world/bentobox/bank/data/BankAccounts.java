package world.bentobox.bank.data;

import java.util.Map;
import java.util.TreeMap;

import com.google.gson.annotations.Expose;

import world.bentobox.bentobox.database.objects.DataObject;
import world.bentobox.bentobox.database.objects.Table;

/**
 * @author tastybento
 *
 */
@Table(name = "BankAccounts")
public class BankAccounts implements DataObject {

    /**
     * Island ID
     */
    @Expose
    private String uniqueId;

    /**
     * This is only used for backward compatibility
     */
    @Expose
    private Double balance;

    /**
     * The balance in {@link Money}
     */
    @Expose
    private Money moneyBalance;

    @Expose
    private final Map<Long, String> history = new TreeMap<>();

    /**
     * Timestamp for when interest was last paid
     */
    @Expose
    private Long interestLastPaid;

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * @return the balance
     */
    public Money getBalance() {
        // Backwards compatibility
        if (balance != null && balance != 0) {
            moneyBalance = new Money(balance);
            balance = null;
        } else if (moneyBalance == null) {
            moneyBalance = new Money();
        }
        return moneyBalance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(Money balance) {
        this.moneyBalance = balance;
    }

    /**
     * @return the history
     */
    public Map<Long, String> getHistory() {
        return history;
    }

    /**
     * Get the timestamp for when interest was last paid
     * @return the interestLastPaid
     */
    public long getInterestLastPaid() {
        if (interestLastPaid == null) {
            interestLastPaid = System.currentTimeMillis();
        }
        return interestLastPaid;
    }

    /**
     * Set when interest was last paid
     * @param interestLastPaid the interestLastPaid to set
     */
    public void setInterestLastPaid(long interestLastPaid) {
        this.interestLastPaid = interestLastPaid;
    }

}
