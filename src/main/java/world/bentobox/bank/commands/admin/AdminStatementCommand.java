package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bank.commands.user.tabs.StatementTab;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.TabbedPanelBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminStatementCommand extends AbstractAdminBankCommand {

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
        return canAbstractExecute(user, args, RequestType.ADMIN_STATEMENT);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Close the current panel
        user.closeInventory();
        // Open a new panel
        new TabbedPanelBuilder()
        .user(user)
        .world(getWorld())
        .tab(1, new StatementTab(getAddon(), user, island, true))
        .tab(2, new StatementTab(getAddon(), user, island, false))
        .startingSlot(1)
        .size(54)
        .build().openPanel();
        return true;
    }

}
