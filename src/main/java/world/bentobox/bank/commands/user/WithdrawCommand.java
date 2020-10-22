package world.bentobox.bank.commands.user;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;

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
        if (!canAbstractExecute(user, label, args, 1)) {
            return false;
        }
        // Check if the player has the balance
        double balance = ((Bank)getAddon()).getBankManager().getBalance(user, getWorld());
        if (balance < value) {
            user.sendMessage("bank.errors.low-balance");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        VaultHook vault = ((Bank)this.getAddon()).getVault();
        // Success
        ((Bank)getAddon()).getBankManager().withdraw(user, value, getWorld()).thenAccept(result -> {
            switch (result) {
            case FAILURE_LOAD_ERROR:
                user.sendMessage("bank.errors.bank-error");
                break;
            case FAILURE_LOW_BALANCE:
                user.sendMessage("bank.errors.low-balance");
                break;
            case FAILURE_NO_ISLAND:
                user.sendMessage("general.errors.no-island");
                break;
            default:
                vault.deposit(user, value, getWorld());
                user.sendMessage("bank.withdraw.success", TextVariables.NUMBER, vault.format(((Bank)getAddon()).getBankManager().getBalance(user, getWorld())));
                break;

            }
        });
        return true;
    }


}
