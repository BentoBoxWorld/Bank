package world.bentobox.bank.commands.user;

import java.util.List;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
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
public class DepositCommand extends AbstractBankCommand {

    public DepositCommand(CompositeCommand parent) {
        super(parent, "deposit");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.deposit");
        this.setParametersHelp("bank.deposit.parameters");
        this.setDescription("bank.deposit.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Check if the player has the balance
        VaultHook vault = ((Bank)this.getAddon()).getVault();
        double balance = vault.getBalance(user, getWorld());
        if (balance < value) {
            user.sendMessage("bank.errors.too-much");
            return false;
        }
        // Success
        EconomyResponse response = vault.withdraw(user, value, getWorld());
        if (response.type == ResponseType.SUCCESS) {
            ((Bank)getAddon()).getBankManager().deposit(user, value, getWorld()).thenAccept(result -> {
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
                case SUCCESS:
                    user.sendMessage("bank.deposit.success", TextVariables.NUMBER, vault.format(((Bank)getAddon()).getBankManager().getBalance(user, getWorld())));
                    break;
                default:
                    break;

                }
            });
            return true;
        }
        user.sendMessage("bank.errors.bank-error");
        return false;
    }

}
