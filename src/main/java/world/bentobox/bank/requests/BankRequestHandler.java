package world.bentobox.bank.requests;

import net.milkbowl.vault.economy.EconomyResponse;
import world.bentobox.bank.Bank;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.addons.request.AddonRequestHandler;
import world.bentobox.bentobox.api.user.User;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * This Request Handler allows other plugins manage island banks.
 * Handler returns requested values by action
 *
 * @see RequestAction
 */
public class BankRequestHandler extends AddonRequestHandler {

    private final Bank bank;
    private final Object PLAYER = "player";
    private final Object ISLAND = "island-id";
    private final Object ACTION = "action";
    private final Object AMOUNT = "amount";

    public BankRequestHandler(Bank addon) {
        super("island-bank");
        bank = addon;
    }

    /* (non-Javadoc)
     * @see world.bentobox.bentobox.api.addons.request.AddonRequestHandler#handle(java.util.Map)
     * @param metaData Required meta data.
     * @return depends to action, see more on @RequestAction
     */
    @Override
    public Object handle(Map<String, Object> map) {
        try {
            RequestAction action = RequestAction.valueOf(((String) map.get(ACTION)).toUpperCase(Locale.ENGLISH));
            Money account = bank.getBankManager().getBalance((UUID) map.get(ISLAND));
            double currentBalance = account.getValue();

            if (action.equals(RequestAction.RESET)) {
                account.setValue(0);
                return BigDecimal.valueOf(currentBalance);
            } else if (action.equals(RequestAction.GET_BALANCE)) {
                return BigDecimal.valueOf(account.getValue());
            }

            double amount = (Double) map.get(AMOUNT);

            if (action.equals(RequestAction.ADD_BALANCE)) {
                account.setValue(currentBalance + amount);
                return account.getValue();
            } else if (action.equals(RequestAction.REMOVE_BALANCE)) {
                account.setValue(Math.max(currentBalance - amount, 0));
                return account.getValue();
            } else if (action.equals(RequestAction.WITHDRAW)) {
                User user = User.getInstance((UUID) map.get(PLAYER));
                if (amount > currentBalance) return action.defaultValue;
                account.setValue(currentBalance - amount);
                return bank.getVault().deposit(user, amount).type.equals(EconomyResponse.ResponseType.SUCCESS);
            } else {
                User user = User.getInstance((UUID) map.get(PLAYER));
                boolean hasBalance = bank.getVault().has(user, amount);
                if (hasBalance) {
                    bank.getVault().withdraw(user, amount);
                    account.setValue(currentBalance + amount);
                }
                return hasBalance;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
