package world.bentobox.bank.commands.user;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import world.bentobox.bank.commands.AbstractBankCommand;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * @author tastybento
 *
 */
public class WithdrawCommand extends AbstractBankCommand {

    public WithdrawCommand(CompositeCommand parent) {
        super(parent, "withdraw");
    }

    @Override
    public void setup() {
        this.setOnlyPlayer(true);
        this.setPermission("bank.user.withdraw");
        this.setParametersHelp("bank.withdraw.parameters");
        this.setDescription("bank.withdraw.description");

    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        if (!canAbstractExecute(user, args, RequestType.USER_WITHDRAWAL)) {
            return false;
        }
        // Check if the player has the balance
        Money balance = addon.getBankManager().getBalance(user, getWorld());
        if (Money.lessThan(balance, value)) {
            user.sendMessage("bank.errors.low-balance");
            return false;
        }
        return true;
    }

    @Override
    public boolean execute(User user, String label, List<String> args) {

        // Success
        addon.getBankManager().withdraw(user, value, getWorld()).thenAccept(result -> {
            switch (result) {
                case FAILURE_LOAD_ERROR -> user.sendMessage("bank.errors.bank-error");
                case FAILURE_LOW_BALANCE -> user.sendMessage("bank.errors.low-balance");
                case FAILURE_NO_ISLAND -> user.sendMessage("general.errors.no-island");
                default -> {
                    addon.getVault().deposit(user, value.getValue(), getWorld());
                    user.sendMessage("bank.withdraw.success", TextVariables.NUMBER, addon.getVault().format(addon.getBankManager().getBalance(island).getValue()));

                    if(!addon.getSettings().isSendBankAlert()) return;
                    Island island = getPlugin().getIslands().getIsland(getWorld(), user);

                    final Set<UUID> members = island.getMemberSet(RanksManager.MEMBER_RANK);
                    for(UUID member : members) {
                        final Player player = Bukkit.getPlayer(member);

                        if(player == null || user.getUniqueId().equals(member)) continue;

                        User.getInstance(member).sendMessage("bank.withdraw.alert", "[name]", user.getName(), "[number]", String.valueOf(value.getValue()));
                    }
                }
            }
        });
        return true;
    }

    @Override
    public Optional<List<String>> tabComplete(User user, String alias, List<String> args) {
        String balance = String.valueOf(addon.getBankManager().getBalance(user, getWorld()).getValue());
        return Optional.of(Collections.singletonList(balance));
    }

}
