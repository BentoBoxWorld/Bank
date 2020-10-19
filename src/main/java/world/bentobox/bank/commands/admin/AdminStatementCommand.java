package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bank.commands.user.tabs.StatementTab;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.TabbedPanelBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminStatementCommand extends AdminCommand {

    public AdminStatementCommand(CompositeCommand parent) {
        super(parent, "statement");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.admin.statement");
        this.setParametersHelp("bank.admin.statement.parameters");
        this.setDescription("bank.admin.statement.description");
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
        // Close the current panel
        user.closeInventory();
        // Open a new panel
        new TabbedPanelBuilder()
        .user(user)
        .world(getWorld())
        .tab(1, new StatementTab(((Bank)getAddon()), user, island, true))
        .tab(2, new StatementTab(((Bank)getAddon()), user, island, false))
        .startingSlot(1)
        .size(54)
        .build().openPanel();
        return true;
    }

}
