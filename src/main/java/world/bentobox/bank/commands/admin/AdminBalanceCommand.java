package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminBalanceCommand extends AbstractAdminBankCommand {

    public AdminBalanceCommand(CompositeCommand parent) {
        super(parent, "balance");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.balance");
        this.setParametersHelp("bank.admin.balance.parameters");
        this.setDescription("bank.admin.balance.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return this.canAbstractExecute(user, args, RequestType.ADMIN_BALANCE);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("bank.balance.island-balance", TextVariables.NUMBER, format(addon
                .getBankManager()
                .getBalance(island)));
        return true;
    }

}
