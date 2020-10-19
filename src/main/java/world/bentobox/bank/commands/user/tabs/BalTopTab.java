package world.bentobox.bank.commands.user.tabs;

import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.Bank;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.panels.PanelItem;
import world.bentobox.bentobox.api.panels.Tab;
import world.bentobox.bentobox.api.panels.builders.PanelItemBuilder;
import world.bentobox.bentobox.api.user.User;

/**
 * @author tastybento
 *
 */
public class BalTopTab implements Tab {

    private final User user;
    private final boolean sort;
    private final Bank addon;
    private static final Comparator<Entry<String, Double>> comparator = (h1, h2) -> Double.compare(h1.getValue(), h2.getValue());


    public BalTopTab(Bank addon, User user, boolean sort) {
        this.addon = addon;
        this.user = user;
        this.sort = sort;
    }

    @Override
    public PanelItem getIcon() {
        return sort ? new PanelItemBuilder().icon(Material.DIAMOND).name(user.getTranslation("bank.baltop.highest")).build()
                : new PanelItemBuilder().icon(Material.STICK).name(user.getTranslation("bank.baltop.lowest")).build();
    }

    @Override
    public String getName() {
        return user.getTranslation("bank.baltop.title");
    }

    @Override
    public List<@Nullable PanelItem> getPanelItems() {
        return addon.getBankManager().getBalances().entrySet().stream()
                .sorted(sort ? comparator.reversed() : comparator)
                .limit(50)
                .map(ah -> addon.getIslands().getIslandById(ah.getKey())
                        .filter(i -> i.getOwner() != null)
                        .map(island -> new PanelItemBuilder()
                                .icon(addon.getPlayers().getName(island.getOwner()))
                                .name(user.getTranslation("bank.baltop.name-syntax", TextVariables.NAME, addon.getPlayers().getName(island.getOwner())))
                                .description(user.getTranslation("bank.baltop.description-syntax", TextVariables.NUMBER, addon.getVault().format(ah.getValue())))
                                .build()).orElse(null)
                        ).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String getPermission() {
        return "";
    }

}
