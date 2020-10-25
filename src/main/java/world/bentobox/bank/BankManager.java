package world.bentobox.bank;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Enums;

import world.bentobox.bank.data.AccountHistory;
import world.bentobox.bank.data.BankAccounts;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandPreclearEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
public class BankManager implements Listener {
    private static final int MAX_SIZE = 20;
    // Database handler for accounts
    private final Database<BankAccounts> handler;
    private final Bank addon;
    private final Map<String, BankAccounts> cache;
    private final Map<String, Double> balances;

    /**
     * Cached database bank manager for withdrawals, deposits and balance inquiries
     * @param addon - Bank
     */
    public BankManager(Bank addon) {
        cache = new HashMap<>();
        balances = new ConcurrentHashMap<>();
        handler = new Database<>(addon, BankAccounts.class);
        this.addon = addon;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(addon.getPlugin(), () -> {
            if (cache.size() > MAX_SIZE) {
                cache.clear();
            }
        }, 6000L, 6000L);
    }

    public void loadBalances() {
        balances.clear();
        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () ->
        handler.loadObjects().forEach(ba -> balances.put(ba.getUniqueId(), ba.getBalance())));
    }

    /**
     * @param user - depositor and island member
     * @param amount - amount
     * @param world - island's world
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> deposit(User user, double amount, World world) {
        // Get player's account
        Island island = addon.getIslands().getIsland(Util.getWorld(world), user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }
        return deposit(user, island, amount, TxType.DEPOSIT);
    }

    /**
     * @param user - depositor
     * @param island - island
     * @param amount - amount
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> deposit(User user, Island island, double amount, TxType type) {
        try {
            BankAccounts account = getAccount(island.getUniqueId());
            return this.set(user, island.getUniqueId(), amount, (account.getBalance() + amount), type);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR);
        }
    }

    /**
     * Gets account
     * @param uuid - Island UUID
     * @return account
     * @throws IOException - if database value cannot be read
     */
    private BankAccounts getAccount(String uuid) throws IOException {
        if (cache.containsKey(uuid)) return cache.get(uuid);
        BankAccounts account = new BankAccounts();
        if (!handler.objectExists(uuid)) {
            // Create new account
            account.setUniqueId(uuid);
        } else {
            account = handler.loadObject(uuid);
            if (account == null) {
                throw new IOException("Cannot load account from database");
            }
        }
        return account;
    }

    /**
     * @param user - island member
     * @param amount - amount
     * @param world - world
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> withdraw(User user, double amount, World world) {
        // Get player's island
        Island island = addon.getIslands().getIsland(Util.getWorld(world), user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }
        return withdraw(user, island, amount, TxType.WITHDRAW);
    }

    /**
     * @param user - user withdrawing
     * @param island - island
     * @param amount - amount
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> withdraw(User user, Island island, double amount, TxType type) {
        BankAccounts account;
        if (!handler.objectExists(island.getUniqueId())) {
            // No account = no balance
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE);
        } else {
            try {
                account = getAccount(island.getUniqueId());
            } catch (IOException e) {
                return CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR);
            }

        }
        // Check balance
        if (account.getBalance() < amount) {
            // Low balance
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE);
        }
        // Success
        return this.set(user, island.getUniqueId(), amount, (account.getBalance() - amount), type);
    }

    /**
     * Get balance for island
     * @param island - island
     * @return balance. 0 if unknown
     */
    public double getBalance(@Nullable Island island) {
        if (island == null) {
            return 0D;
        }
        return balances.getOrDefault(island.getUniqueId(), 0D);
    }

    /**
     * Get balance for user in world
     * @param user - user
     * @param world - world
     * @return balance. 0 if unknown
     */
    public double getBalance(User user, World world) {
        return getBalance(addon.getIslands().getIsland(Util.getWorld(world), user));
    }

    /**
     * Get history for island
     * @param island - island
     * @return list of {@link AccountHistory}
     */
    public List<AccountHistory> getHistory(Island island) {
        try {
            BankAccounts account = getAccount(island.getUniqueId());
            return account.getHistory().entrySet().stream().map(en -> {
                String[] split = en.getValue().split(":");
                if (split.length == 3) {
                    TxType type = Enums.getIfPresent(TxType.class, split[1]).or(TxType.UNKNOWN);
                    return new AccountHistory(en.getKey(), split[0], Double.valueOf(split[2]), type);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get balances for a world
     * @param world - world
     * @return the balances
     */
    public Map<String, Double> getBalances(World world) {
        return balances.entrySet().stream()
                .filter(en -> addon.getIslands().getIslandById(en.getKey())
                        .map(i -> i.getWorld().equals(Util.getWorld(world))).orElse(false))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Sets an island's account value to an amount
     * @param user - user who is doing the setting
     * @param islandID - island unique id
     * @param amount - amount
     * @param type - type of transaction
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> set(User user, String islandID, double amount, double newBalance, TxType type) {
        try {
            BankAccounts account = getAccount(islandID);
            account.setBalance(newBalance);
            account.getHistory().put(System.currentTimeMillis(), user.getName() + ":" + type + ":" + amount);
            cache.put(islandID, account);
            balances.put(islandID, account.getBalance());
            CompletableFuture<BankResponse> result = new CompletableFuture<>();
            handler.saveObjectAsync(account).thenRun(() -> result.complete(BankResponse.SUCCESS));
            return result;
        } catch (IOException e) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onIslandDelete(IslandPreclearEvent e) {
        String id = e.getIsland().getUniqueId();
        handler.deleteID(id);
        cache.remove(id);
        balances.remove(id);
    }
}
