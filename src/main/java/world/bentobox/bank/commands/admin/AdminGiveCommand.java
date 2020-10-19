package world.bentobox.bank.commands.admin;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;

/**
 * @author tastybento
 *
 */
public class AdminGiveCommand extends AdminCommand {

    private @Nullable Island island;

    public AdminGiveCommand(CompositeCommand parent) {
        super(parent, "give");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.give");
        this.setParametersHelp("bank.admin.give.parameters");
        this.setDescription("bank.admin.give.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Check if there's the right number of arguments
        if (args.size() != 2) {
            this.showHelp(this, user);
            return false;
        }
        // Get target's island
        island = getIslands().getIsland(getWorld(), getAddon().getPlayers().getUser(args.get(0)));
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        // Check value
        if (!NumberUtils.isDigits(args.get(1))) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        double value = 0;
        try {
            value = Double.parseDouble(args.get(1));
        } catch (Exception e) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        if (value <= 0) {
            user.sendMessage("bank.errors.value-must-be-positive");
            return false;
        }

        // Success
        ((Bank)getAddon()).getBankManager().deposit(user, island, value, TxType.GIVE).thenAccept(result -> {
            switch (result) {
            case SUCCESS:
                VaultHook vault = ((Bank)this.getAddon()).getVault();
                user.sendMessage("bank.deposit.success", TextVariables.NUMBER, vault.format(((Bank)getAddon()).getBankManager().getBalance(user, getWorld())));
                break;
            default:
                user.sendMessage("bank.errors.bank-error");
                break;

            }
        });
        return true;
    }

}
