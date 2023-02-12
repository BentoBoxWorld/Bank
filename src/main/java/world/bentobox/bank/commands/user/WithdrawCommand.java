package world.bentobox.bank.commands.user;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class WithdrawCommand extends AbstractBankCommand {

    public WithdrawCommand(CompositeCommand parent) {
        super(parent, "withdraw");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.withdraw");
        this.setParametersHelp("bank.withdraw.parameters");
        this.setDescription("bank.withdraw.description");

    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!canAbstractExecute(user, args, RequestType.USER_WITHDRAWAL)) {
            return false;
        }
        // Check if the player has the balance
        Money balance = addon.getBankManager().getBalance(user, getWorld());
        if (Money.lessThan(balance, value)) {
            user.sendMessage("bank.errors.low-balance");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {

        // Success
        addon.getBankManager().withdraw(user, value, getWorld()).thenAccept(result -> {
            switch (result) {
                case FAILURE_LOAD_ERROR -> user.sendMessage("bank.errors.bank-error");
                case FAILURE_LOW_BALANCE -> user.sendMessage("bank.errors.low-balance");
                case FAILURE_NO_ISLAND -> user.sendMessage("general.errors.no-island");
                default -> {
                    addon.getVault().deposit(user, value.getValue(), getWorld());
                    user.sendMessage("bank.withdraw.success", TextVariables.NUMBER, addon.getVault().format(addon.getBankManager().getBalance(island).getValue()));
                }
            }
        });
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        String balance = String.valueOf(addon.getBankManager().getBalance(user, getWorld()).getValue());
        return Optional.of(List.of(balance, "all"));
    }

}
