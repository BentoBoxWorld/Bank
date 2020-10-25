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

    @Expose
    private double balance;

    @Expose
    private final Map<Long, String> history = new TreeMap<>();

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
    public double getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }

    /**
     * @return the history
     */
    public Map<Long, String> getHistory() {
        return history;
    }

}
