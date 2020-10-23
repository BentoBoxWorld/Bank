package world.bentobox.bank.commands.admin;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;

/**
 * @author tastybento
 *
 */
public class AdminGiveCommand extends AbstractAdminBankCommand {

    public AdminGiveCommand(CompositeCommand parent) {
        super(parent, "give");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.give");
        this.setParametersHelp("bank.admin.give.parameters");
        this.setDescription("bank.admin.give.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return checkArgs(user, args, RequestType.ADMIN_GIVE);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Success
        ((Bank)getAddon())
        .getBankManager()
        .deposit(user, island, value, TxType.GIVE)
        .thenAccept(result -> {
            if (result == BankResponse.SUCCESS) {
                VaultHook vault = ((Bank)this.getAddon()).getVault();
                user.sendMessage("bank.deposit.success", TextVariables.NUMBER, vault.format(((Bank)getAddon()).getBankManager().getBalance(user, getWorld())));
            } else {
                user.sendMessage("bank.errors.bank-error");
            }
        });
        return true;
    }

}
