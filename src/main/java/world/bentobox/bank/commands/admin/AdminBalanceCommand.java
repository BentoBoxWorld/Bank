package world.bentobox.bank.commands.admin;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class AdminBalanceCommand extends AdminCommand {

    private @Nullable Island island;

    public AdminBalanceCommand(CompositeCommand parent) {
        super(parent, "balance");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.balance");
        this.setDescription("bank.admin.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Check if there's the right number of arguments
        if (args.size() != 1) {
            this.showHelp(this, user);
            return false;
        }
        // Get target's island
        island = getIslands().getIsland(getWorld(), getAddon().getPlayers().getUser(args.get(0)));
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("bank.balance.island-balance", TextVariables.NUMBER, ((Bank)getAddon())
                .getVault()
                .format(((Bank)getAddon())
                        .getBankManager()
                        .getBalance(island)));
        return true;
    }

}
