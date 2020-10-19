/**
 *
 */
package world.bentobox.bank.commands;

import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public abstract class AbstractBankCommand extends CompositeCommand {

    protected AbstractBankCommand(Bank bank, CompositeCommand adminCmd, String adminCommand) {
        super(bank, adminCmd, adminCommand);
    }
    protected AbstractBankCommand(CompositeCommand parent, String string) {
        super(parent, string);
    }

    protected Island island;
    protected double value;

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // Check world
        if (!this.getWorld().equals(user.getWorld())) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        if (!checkArgs(user, args, 1)) {
            return false;
        }
        // Check flag
        if (!island.isAllowed(user, Bank.BANK_ACCESS)) {
            user.sendMessage("bank.errors.no-rank");
            return false;
        }
        return true;
    }

    protected boolean checkArgs(User user, List<String> args, int size) {
        // Check if there's the right number of arguments
        if (args.size() != size) {
            this.showHelp(this, user);
            return false;
        }
        // Get target's island
        if (size == 1) {
            island = getIslands().getIsland(getWorld(), user);
        } else {
            island = getIslands().getIsland(getWorld(), getAddon().getPlayers().getUser(args.get(0)));
        }
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        // Check value
        if (!NumberUtils.isNumber(args.get(size - 1))) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        return parseValue(user, args.get(size - 1));
    }

    protected boolean parseValue(User user, String arg) {
        try {
            value = Double.parseDouble(arg);
        } catch (Exception e) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        if (value <= 0) {
            user.sendMessage("bank.errors.value-must-be-positive");
            return false;
        }
        return true;
    }
}
