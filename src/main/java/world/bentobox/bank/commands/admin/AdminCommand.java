package world.bentobox.bank.commands.admin;

import java.util.List;
import java.util.Optional;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public class AdminCommand extends CompositeCommand {

    public AdminCommand(Bank bank, CompositeCommand adminCmd, String adminCommand) {
        super(bank, adminCmd, adminCommand);
    }

    public AdminCommand(CompositeCommand parent, String string) {
        super(parent, string);
    }

    @Override
    public void setup() {
        this.setDescription("bank.admin.description");
        new AdminStatementCommand(this);
        new AdminBalanceCommand(this);
        new AdminGiveCommand(this);
        new AdminSetCommand(this);
        new AdminTakeCommand(this);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        return Optional.of(Util.getOnlinePlayerList(user));
    }
}
