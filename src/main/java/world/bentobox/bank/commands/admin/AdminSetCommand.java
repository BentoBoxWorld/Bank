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
public class AdminSetCommand extends AdminCommand {

    public AdminSetCommand(CompositeCommand parent) {
        super(parent, "set");
    }

    @Override
    public void setup() {
        this.setPermission("bank.admin.set");
        this.setParametersHelp("bank.admin.set.parameters");
        this.setDescription("bank.admin.set.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return checkArgs(user, args, 2);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        // Success
        ((Bank)getAddon()).getBankManager().set(user, island.getUniqueId(), value, TxType.SET).thenAccept(result -> {
            if (result == BankResponse.SUCCESS) {
                VaultHook vault = ((Bank)this.getAddon()).getVault();
                user.sendMessage("bank.admin.set.success", TextVariables.NUMBER, vault.format(((Bank)getAddon()).getBankManager().getBalance(user, getWorld())));
            } else {
                user.sendMessage("bank.errors.bank-error");
            }
        });
        return true;
    }

}
