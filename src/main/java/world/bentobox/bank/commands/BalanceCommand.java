package world.bentobox.bank.commands;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class BalanceCommand extends CompositeCommand {

    public BalanceCommand(UserCommand parent) {
        super(parent, "balance");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.balance");
        this.setDescription("bank.balance.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Check if there's the right number of arguments
        if (!args.isEmpty()) {
            this.showHelp(this, user);
            return false;
        }
        // Check world
        if (!this.getWorld().equals(user.getWorld())) {
            user.sendMessage("general.errors.wrong-world");
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
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("bank.balance.your-balance", TextVariables.NUMBER, ((Bank)getAddon())
                .getVault()
                .format(((Bank)getAddon())
                        .getBankManager()
                        .getBalance(user, getWorld())));
        return true;
    }

}
