package world.bentobox.bank.commands;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;

/**
 * @author tastybento
 *
 */
public class DepositCommand extends CompositeCommand {

    public DepositCommand(UserCommand parent) {
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
    public boolean canExecute(User user, String label, List<String> args) {
        // Check world
        if (!this.getWorld().equals(user.getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        // Check if there's the right number of arguments
        if (args.size() != 1) {
            this.showHelp(this, user);
            return false;
        }
        // Check value
        if (!NumberUtils.isDigits(args.get(0))) {
            user.sendMessage("bank.error.must-be-a-number");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        VaultHook vault = ((Bank)this.getAddon()).getVault();
        double value = 0;
        try {
            value = Double.parseDouble(args.get(0));
        } catch (Exception e) {
            user.sendMessage("bank.error.must-be-a-number");
            return false;
        }
        if (value <= 0) {
            user.sendMessage("bank.error.value-must-be-positive");
            return false;
        }
        // Check if the player has the balance
        double balance = vault.getBalance(user, getWorld());
        if (balance < value) {
            user.sendMessage("bank.error.too-much");
            return false;
        }
        // Success
        EconomyResponse response = vault.withdraw(user, value, getWorld());
        if (response.type == ResponseType.SUCCESS) {
            ((Bank)getAddon()).getBankManager().deposit(user, value, getWorld()).thenAccept(result -> {
                switch (result) {
                case FAILURE_LOAD_ERROR:
                    user.sendMessage("bank.error.bank-error");
                    break;
                case FAILURE_LOW_BALANCE:
                    user.sendMessage("bank.error.low-balance");
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
        }
        return true;
    }

}
