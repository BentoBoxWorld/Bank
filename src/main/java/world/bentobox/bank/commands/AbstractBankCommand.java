package world.bentobox.bank.commands;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberUtils;

import world.bentobox.bank.Bank;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public abstract class AbstractBankCommand extends CompositeCommand {

    protected AbstractBankCommand(CompositeCommand parent, String string) {
        super(parent, string);
        this.addon = getAddon();
    }

    protected Bank addon;
    protected Island island;
    protected Money value = new Money();
    protected User target;

    protected enum RequestType {
        USER_BALANCE,
        USER_DEPOSIT,
        USER_WITHDRAWAL,
        ADMIN_SET,
        ADMIN_BALANCE,
        ADMIN_GIVE,
        ADMIN_TAKE,
        USER_STATEMENT,
        ADMIN_STATEMENT
    }

    /**
     * A map of the number of args required for each request type
     */
    private static final Map<RequestType, Integer> ARG_SIZE;
    static {
        Map<RequestType, Integer> as = new EnumMap<>(RequestType.class);
        as.put(RequestType.USER_BALANCE, 0);
        as.put(RequestType.USER_DEPOSIT, 1);
        as.put(RequestType.USER_WITHDRAWAL, 1);
        as.put(RequestType.USER_STATEMENT, 0);
        as.put(RequestType.ADMIN_BALANCE, 1);
        as.put(RequestType.ADMIN_GIVE, 2);
        as.put(RequestType.ADMIN_SET, 2);
        as.put(RequestType.ADMIN_STATEMENT, 1);
        as.put(RequestType.ADMIN_TAKE, 2);

        ARG_SIZE = Collections.unmodifiableMap(as);
    }

    /**
     * @param user - user
     * @param args - args
     * @return true if can execute, false if not
     */
    public boolean canAbstractExecute(User user, List<String> args, RequestType type) {
        // Check world
        if (!this.getWorld().equals(Util.getWorld(user.getWorld()))) {
            user.sendMessage("general.errors.wrong-world");
            return false;
        }
        if (!checkArgs(user, args, type)) {
            return false;
        }
        // Check flag
        if (type.name().startsWith("USER") && !island.isAllowed(user, Bank.BANK_ACCESS)) {
            user.sendMessage("bank.errors.no-rank");
            return false;
        }
        return true;
    }

    protected boolean checkArgs(User user, List<String> args, RequestType type) {
        // Check if there's the right number of arguments
        int size = ARG_SIZE.get(type);
        if (args.size() != size) {
            this.showHelp(this, user);
            return false;
        }
        // Get target's island
        boolean isUser = type.name().startsWith("USER");
        if (isUser) {
            island = getIslands().getIsland(getWorld(), user);
        } else {
            target = getAddon().getPlayers().getUser(args.get(0));
            if (target == null) {
                user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
                return false;
            }
            island = getIslands().getIsland(getWorld(), target);
        }
        if (island == null) {
            user.sendMessage("general.errors.no-island");
            return false;
        }
        if (args.isEmpty() || (!isUser && args.size() == 1)) return true;
        // Check value
        String v = args.get(args.size() - 1);
        if (!NumberUtils.isNumber(v)) {
            user.sendMessage("bank.errors.must-be-a-number");
            return false;
        }
        return parseValue(user, v);
    }

    protected boolean parseValue(User user, String arg) {
        try {
            value = Money.parseMoney(arg);
        } catch (Exception e) {
            if (e.getMessage().startsWith("bank")) {
                user.sendMessage(e.getMessage());
            } else {
                user.sendMessage("bank.errors.must-be-a-number");
            }
            return false;
        }
        if (!value.isPositive()) {
            user.sendMessage("bank.errors.value-must-be-positive");
            return false;
        }
        return true;
    }

}
