package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class AdminTakeCommand extends AbstractAdminBankCommand {

    public AdminTakeCommand(CompositeCommand parent) {
        super(parent, "take");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.take");
        this.setParametersHelp("bank.admin.take.parameters");
        this.setDescription("bank.admin.take.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return checkArgs(user, args, RequestType.ADMIN_TAKE);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Success
        addon
        .getBankManager()
        .withdraw(user, island, value, TxType.TAKE)
        .thenAccept(result -> {
            switch (result) {
            case FAILURE_LOW_BALANCE:
                user.sendMessage("bank.errors.too-low");
                break;
            case SUCCESS:
                user.sendMessage("bank.admin.give.success",
                        TextVariables.NAME, this.target.getName(),
                        TextVariables.NUMBER, addon.getVault().format(addon.getBankManager().getBalance(island).getValue()));
                break;
            default:
                user.sendMessage("bank.errors.bank-error");
                break;

            }
        });
        return true;
    }

}
