package world.bentobox.bank.commands.user;

import java.util.List;

import world.bentobox.bank.Bank;
import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;

/**
 * Provides the player's island bank balance
 * @author tastybento
 *
 */
public class BalanceCommand extends AbstractBankCommand {

    public BalanceCommand(CompositeCommand parent) {
        super(parent, "balance");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.balance");
        this.setDescription("bank.balance.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        return canAbstractExecute(user, args, RequestType.USER_BALANCE);
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {
        user.sendMessage("bank.balance.island-balance", TextVariables.NUMBER, ((Bank)getAddon())
                .getVault()
                .format(((Bank)getAddon())
                        .getBankManager()
                        .getBalance(island)));
        return true;
    }

}
