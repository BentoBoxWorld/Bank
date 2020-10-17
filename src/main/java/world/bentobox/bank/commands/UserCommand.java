package world.bentobox.bank.commands;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class UserCommand extends CompositeCommand {

    public UserCommand(Bank addon, CompositeCommand parent, String label) {
        super(addon, parent, label);
    }

    @Override
    public void setup() {
        this.setPermission("bank.user");
        this.setOnlyPlayer(true);
        this.setDescription("bank.user.description");
        // Sub commands
        new StatementCommand(this);
        new DepositCommand(this);
        new WithdrawCommand(this);
        new BalanceCommand(this);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        this.showHelp(this, user);
        return true;
    }

}
