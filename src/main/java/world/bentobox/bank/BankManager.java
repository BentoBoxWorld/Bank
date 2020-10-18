package world.bentobox.bank;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;

import world.bentobox.bank.data.AccountHistory;
import world.bentobox.bank.data.BankAccounts;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;

/**
 * @author tastybento
 *
 */
public class BankManager {
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

    public CompletableFuture<BankResponse> deposit(User user, double amount, World world) {
        // Get player's account
        Island island = addon.getIslands().getIsland(world, user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }

        try {
            BankAccounts account = getAccount(island.getUniqueId());
            account.setBalance(account.getBalance() + amount);
            account.getHistory().put(System.currentTimeMillis(), user.getName() + ":" + amount);
            cache.put(island.getUniqueId(), account);
            balances.put(island.getUniqueId(), account.getBalance());
            CompletableFuture<BankResponse> result = new CompletableFuture<>();
            handler.saveObjectAsync(account).thenRun(() -> result.complete(BankResponse.SUCCESS));
            return result;
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

    public CompletableFuture<BankResponse> withdraw(User user, double amount, World world) {
        // Get player's island
        Island island = addon.getIslands().getIsland(world, user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }

        BankAccounts account = new BankAccounts();
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
        account.setBalance(account.getBalance() - amount);
        account.getHistory().put(System.currentTimeMillis(), user.getName() + ":-" + amount);
        cache.put(island.getUniqueId(), account);
        balances.put(island.getUniqueId(), account.getBalance());
        CompletableFuture<BankResponse> result = new CompletableFuture<>();
        handler.saveObjectAsync(account).thenRun(() -> result.complete(BankResponse.SUCCESS));
        return result;
    }

    public double getBalance(User user, World world) {
        // Get player's island
        Island island = addon.getIslands().getIsland(world, user);
        if (island == null) {
            return 0D;
        }
        return balances.getOrDefault(island.getUniqueId(), 0D);
    }

    public List<AccountHistory> getHistory(Island island) {
        try {
            BankAccounts account = getAccount(island.getUniqueId());
            return account.getHistory().entrySet().stream().map(en -> {
                String[] split = en.getValue().split(":");
                if (split.length == 2) {
                    return new AccountHistory(en.getKey(), split[0], Double.valueOf(split[1]));
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * @return the balances
     */
    public Map<String, Double> getBalances() {
        return balances;
    }
}
