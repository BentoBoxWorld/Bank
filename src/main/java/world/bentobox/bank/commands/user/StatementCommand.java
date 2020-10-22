package world.bentobox.bank.commands.user;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bank.commands.user.tabs.StatementTab;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.TabbedPanelBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class StatementCommand extends AbstractBankCommand {


    private @Nullable Island island;

    public StatementCommand(CompositeCommand parent) {
        super(parent, "statement");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.statement");
        this.setDescription("bank.statement.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return canAbstractExecute(user, label, args, 0);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Close the current panel
        user.closeInventory();
        // Open a new panel
        new TabbedPanelBuilder()
        .user(user)
        .world(user.getWorld())
        .tab(1, new StatementTab(((Bank)getAddon()), user, island, true))
        .tab(2, new StatementTab(((Bank)getAddon()), user, island, false))
        .startingSlot(1)
        .size(54)
        .build().openPanel();
        return true;
    }

}
