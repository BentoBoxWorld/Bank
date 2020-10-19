package world.bentobox.bank.commands.user;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;

/**
 * @author tastybento
 *
 */
public class WithdrawCommand extends CompositeCommand {


    private double value;

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
        // Check if there's the right number of arguments
        if (args.size() != 1) {
            this.showHelp(this, user);
            return false;
        }
        // Check world
        if (!this.getWorld().equals(user.getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        // Check value
        if (!NumberUtils.isNumber(args.get(0))) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        // Check flag
        Island island = getIslands().getIsland(getWorld(), user);
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        if (!island.isAllowed(user, Bank.BANK_ACCESS)) {
            user.sendMessage("bank.errors.no-rank");
            return false;
        }
        value = 0D;
        try {
            value = Double.parseDouble(args.get(0));
        } catch (Exception e) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        if (value <= 0) {
            user.sendMessage("bank.errors.value-must-be-positive");
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
