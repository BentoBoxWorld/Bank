package world.bentobox.bank.commands;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bank.data.AccountHistory;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.Tab;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class StatementTab implements Tab {

    private final User user;
    private final @Nullable Island island;
    private final boolean sort;
    private final Bank addon;
    private static final Comparator<AccountHistory> comparator = (h1, h2) -> Long.compare(h1.getTimestamp(), h2.getTimestamp());


    public StatementTab(Bank addon, User user, @Nullable Island island, boolean sort) {
        this.addon = addon;
        this.user = user;
        this.island = island;
        this.sort = sort;
    }

    @Override
    public PanelItem getIcon() {
        return sort ? new PanelItemBuilder().icon(Material.GOLD_INGOT).name(user.getTranslation("bank.statement.latest")).build()
                : new PanelItemBuilder().icon(Material.IRON_INGOT).name(user.getTranslation("bank.statement.oldest")).build();
    }

    @Override
    public String getName() {
        return user.getTranslation("bank.statement.title");
    }

    @Override
    public List<@Nullable PanelItem> getPanelItems() {
        return addon.getBankManager().getHistory(island).stream()
                .sorted(sort ? comparator.reversed() : comparator)
                .map(ah -> {
                    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, user.getLocale());
                    String formattedDate = df.format(ah.getTimestamp());
                    PanelItemBuilder pi = new PanelItemBuilder()
                            .description(user.getTranslation("bank.statement.syntax",
                                    "[date-time]",
                                    formattedDate,
                                    TextVariables.NAME, ah.getName(),
                                    TextVariables.NUMBER, addon.getVault().format(ah.getAmount())));
                    return ah.getAmount() > 0 ? pi.icon(Material.GOLD_INGOT).name(user.getTranslation("bank.statement.deposit")).build()
                            : pi.icon(Material.IRON_INGOT).name(user.getTranslation("bank.statement.withdrawal")).build();
                }).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return "";
    }

}
