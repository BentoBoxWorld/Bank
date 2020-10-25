package world.bentobox.bank;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.World;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;

/**
 * Registers placeholders for the addon
 * @author tastybento
 *
 */
public class PhManager {

    private final BentoBox plugin;
    private final BankManager bankManager;
    private final Bank addon;
    private List<String> names = new ArrayList<>();
    private List<String> balances = new ArrayList<>();
    private long lastSorted = System.currentTimeMillis();
    private static final long CACHETIME = 10000;

    /**
     * Constructor
     * @param addon - addon
     * @param bankManager - bank manager
     */
    public PhManager(Bank addon, BankManager bankManager) {
        this.addon = addon;
        this.plugin = addon.getPlugin();
        this.bankManager = bankManager;
    }

    protected boolean registerPlaceholders(GameModeAddon gm) {
        if (plugin.getPlaceholdersManager() == null) return false;
        // Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_island_balance",
                user -> addon.getVault().format(bankManager.getBalance(user, gm.getOverWorld())));

        // Visited Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_visited_island_balance", user -> getVisitedIslandBalance(gm, user));

        // Register Ranked Placeholders
        for (int i = 1; i <= addon.getSettings().getRanksNumber(); i++) {
            final int rank = i;
            // Name
            plugin.getPlaceholdersManager().registerPlaceholder(addon,
                    gm.getDescription().getName().toLowerCase() + "_top_name_" + i, u -> getRankName(gm.getOverWorld(), rank));
            // Level
            plugin.getPlaceholdersManager().registerPlaceholder(addon,
                    gm.getDescription().getName().toLowerCase() + "_top_value_" + i, u -> getRankBalance(gm.getOverWorld(), rank));
        }
        return true;
    }

    String getVisitedIslandBalance(GameModeAddon gm, User user) {
        if (!gm.inWorld(user.getWorld())) return addon.getVault().format(0D);
        return addon.getIslands().getIslandAt(user.getLocation())
                .map(island -> addon.getVault().format(bankManager.getBalance(island)))
                .orElse(addon.getVault().format(0D));
    }

    /**
     * @param world - world to check
     * @param rank - value from 1 to setting in config.yml
     * @return name of island owner who holds the rank or blank if none
     */
    String getRankName(World world, int rank) {
        rank = checkCache(world, rank);
        return rank < names.size() + 1 ? names.get(rank - 1) : "";
    }

    String getRankBalance(World world, int rank) {
        checkCache(world, rank);
        return rank < balances.size() + 1 ? balances.get(rank - 1) : "";
    }

    int checkCache(World world, int rank) {
        if (rank < 1) rank = 1;
        if (rank > addon.getSettings().getRanksNumber()) rank = addon.getSettings().getRanksNumber();
        if (names.isEmpty() || (System.currentTimeMillis() - lastSorted) > CACHETIME) {
            // Clear the old top
            names.clear();
            balances.clear();
            // Get a new balance map, sort it and save it to two sorted lists
            bankManager.getBalances(world).entrySet()
            .stream().sorted((h1, h2) -> Double.compare(h2.getValue(), h1.getValue()))
            .limit(addon.getSettings().getRanksNumber())
            .forEach(en -> {
                names.add(addon.getIslands().getIslandById(en.getKey())
                        .map(i -> addon.getPlayers().getName(i.getOwner())).orElse(""));
                balances.add(addon.getVault().format(en.getValue()));
            });
            lastSorted = System.currentTimeMillis();
        }
        return rank;
    }

    /**
     * @return the lastSorted
     */
    protected long getLastSorted() {
        return lastSorted;
    }

    /**
     * @param lastSorted the lastSorted to set
     */
    protected void setLastSorted(long lastSorted) {
        this.lastSorted = lastSorted;
    }

    /**
     * @return the names
     */
    protected List<String> getNames() {
        return names;
    }

    /**
     * @return the balances
     */
    protected List<String> getBalances() {
        return balances;
    }

    /**
     * @param names the names to set
     */
    protected void setNames(List<String> names) {
        this.names = names;
    }

}
