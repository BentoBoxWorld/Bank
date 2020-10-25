package world.bentobox.bank.commands.user;

import java.util.List;

import world.bentobox.bank.commands.user.tabs.BalTopTab;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.panels.builders.TabbedPanelBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class BalTopCommand extends CompositeCommand {

    public BalTopCommand(CompositeCommand parent) {
        super(parent, "baltop");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.baltop");
        this.setDescription("bank.baltop.description");
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
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Close the current panel
        user.closeInventory();
        // Open a new panel
        new TabbedPanelBuilder()
        .user(user)
        .world(user.getWorld())
        .tab(1, new BalTopTab(getAddon(), getWorld(), user, true))
        .tab(2, new BalTopTab(getAddon(), getWorld(), user, false))
        .startingSlot(1)
        .size(54)
        .build().openPanel();
        return true;
    }

}
