package world.bentobox.bank.commands.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import world.bentobox.bank.Bank;
import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public class AdminCommand extends AbstractBankCommand {

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
        String lastArg = !args.isEmpty() ? args.get(args.size()-1) : "";
        if (args.isEmpty()) {
            // Don't show every player on the server. Require at least the first letter
            return Optional.empty();
        }
        List<String> options = new ArrayList<>(Util.getOnlinePlayerList(user));
        return Optional.of(Util.tabLimit(options, lastArg));
    }

}
