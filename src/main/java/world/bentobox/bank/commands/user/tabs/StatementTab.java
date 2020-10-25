package world.bentobox.bank.commands.user.tabs;

import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bank.data.AccountHistory;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.Tab;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

/**
 * Displays a paginated statement of account transactions
 * @author tastybento
 *
 */
public class StatementTab implements Tab {

    private final User user;
    private final @Nullable Island island;
    private final boolean sort;
    private final Bank addon;
    private static final Comparator<AccountHistory> comparator = Comparator.comparingLong(AccountHistory::getTimestamp);

    private static final Map<TxType, MaterialText> ICON_TEXT;
    static {
        Map<TxType, MaterialText> ic = new EnumMap<>(TxType.class);
        ic.put(TxType.DEPOSIT, new MaterialText(Material.GOLD_NUGGET, "deposit"));
        ic.put(TxType.WITHDRAW, new MaterialText(Material.WOODEN_PICKAXE, "withdrawal"));
        ic.put(TxType.SET, new MaterialText(Material.BIRCH_SIGN, "set"));
        ic.put(TxType.GIVE, new MaterialText(Material.GOLDEN_HOE, "give"));
        ic.put(TxType.TAKE, new MaterialText(Material.DARK_OAK_SIGN, "take"));
        ic.put(TxType.UNKNOWN, new MaterialText(Material.SEA_PICKLE, "unknown"));
        ICON_TEXT = Collections.unmodifiableMap(ic);
    }


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
        if (island == null) return Collections.emptyList();
        return addon.getBankManager().getHistory(island).stream()
                .sorted(sort ? comparator.reversed() : comparator)
                .map(ah -> {
                    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, user.getLocale());
                    String formattedDate = df.format(ah.getTimestamp());
                    df = DateFormat.getTimeInstance(DateFormat.SHORT, user.getLocale());
                    String formattedTime = df.format(ah.getTimestamp());
                    PanelItemBuilder pi = new PanelItemBuilder()
                            .description(user.getTranslation("bank.statement.syntax",
                                    "[date]",
                                    formattedDate,
                                    "[time]",
                                    formattedTime,
                                    TextVariables.NAME, ah.getName(),
                                    TextVariables.NUMBER, addon.getVault().format(ah.getAmount())));
                    return pi.icon(ICON_TEXT.get(ah.getType()).material).name(user.getTranslation("bank.statement." + ICON_TEXT.get(ah.getType()).text)).build();
                }).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return "";
    }

}
