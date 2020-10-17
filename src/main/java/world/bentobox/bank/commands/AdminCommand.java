package world.bentobox.bank.commands;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminCommand extends CompositeCommand {

    public AdminCommand(Bank bank, CompositeCommand adminCmd, String adminCommand, String[] aliases) {
        super(adminCmd, adminCommand, aliases);
    }

    @Override
    public void setup() {
        this.setParametersHelp("bank.admin.parameters");
        this.setDescription("bank.admin.description");
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // TODO Auto-generated method stub
        return false;
    }

}
