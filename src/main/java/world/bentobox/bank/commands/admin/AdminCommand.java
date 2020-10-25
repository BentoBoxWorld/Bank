package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminCommand extends CompositeCommand {

    public AdminCommand(Bank bank, CompositeCommand adminCmd, String adminCommand) {
        super(bank, adminCmd, adminCommand);
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin");
        this.setDescription("bank.admin.description");
        new AdminStatementCommand(this);
        new AdminBalanceCommand(this);
        new AdminGiveCommand(this);
        new AdminSetCommand(this);
        new AdminTakeCommand(this);
    }


    @Override
    public boolean execute(User user, String label, List<String> args) {
        this.showHelp(this, user);
        return true;
    }

}
