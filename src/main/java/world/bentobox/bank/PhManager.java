package world.bentobox.bank;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;

/**
 * Registers placeholders for the addon
 * @author tastybento
 *
 */
public class PhManager {
    private static final BigInteger THOUSAND = BigInteger.valueOf(1000);
    private static final TreeMap<BigInteger, String> LEVELS;
    static {
        LEVELS = new TreeMap<>();

        LEVELS.put(THOUSAND, "k");
        LEVELS.put(THOUSAND.pow(2), "M");
        LEVELS.put(THOUSAND.pow(3), "G");
        LEVELS.put(THOUSAND.pow(4), "T");
    }

    private final BentoBox plugin;
    private final BankManager bankManager;
    private final Bank addon;
    private List<String> names = new ArrayList<>();
    private final List<String> balances = new ArrayList<>();
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
        // Island Balance Number
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_island_balance_number",
                user -> String.valueOf(bankManager.getBalance(user, gm.getOverWorld())));

        // Visited Island Balance Number
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_visited_island_balance_number", user -> getVisitedIslandBalance(gm, user, false, true));

        // Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_island_balance",
                user -> addon.getVault().format(bankManager.getBalance(user, gm.getOverWorld())));

        // Visited Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_visited_island_balance", user -> getVisitedIslandBalance(gm, user, false, false));

        // Formatted Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_island_balance_formatted",
                user -> format(bankManager.getBalance(user, gm.getOverWorld())));

        // Formatted Visited Island Balance
        plugin.getPlaceholdersManager().registerPlaceholder(addon,
                gm.getDescription().getName().toLowerCase() + "_visited_island_balance_formatted", user -> getVisitedIslandBalance(gm, user, true, false));

        // Register Ranked Placeholders
        for (int i = 1; i <= Objects.requireNonNull(addon.getSettings()).getRanksNumber(); i++) {
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

    /**
     * Get the visited island balance
     * @param gm - game mode
     * @param user - user
     * @param formatted - format with k, M, etc.
     * @param plain - provides the raw double balance. If true, formatted parameter is ignored
     * @return string of balance
     */
    String getVisitedIslandBalance(GameModeAddon gm, User user, boolean formatted, boolean plain) {
        if (user == null || user.getLocation() == null) return "";
        double balance = gm.inWorld(user.getWorld()) ? addon.getIslands().getIslandAt(user.getLocation()).map(i -> bankManager.getBalance(i)).orElse(0D) : 0D;
        if (plain) {
            return String.valueOf(balance);
        }
        return formatted ? format(balance) : addon.getVault().format(balance);
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
        if (rank > Objects.requireNonNull(addon.getSettings()).getRanksNumber()) rank = addon.getSettings().getRanksNumber();
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

    /**
     * Get the string representation of money. May be converted to shorthand notation, e.g., 104556 = 10.5k
     * @param lvl - value to represent
     * @return string of the value.
     */
    private String format(@Nullable double value) {
        String level = addon.getVault().format(value);
        BigInteger levelValue = BigInteger.valueOf((long)value);
        Map.Entry<BigInteger, String> stage = LEVELS.floorEntry(levelValue);
        if (stage != null) {
            level = new DecimalFormat("#.#").format(levelValue.divide(stage.getKey().divide(THOUSAND)).doubleValue()/1000.0) + stage.getValue();
        }
        return level;
    }
}
