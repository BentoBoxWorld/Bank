package world.bentobox.bank;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Enums;

import world.bentobox.bank.data.AccountHistory;
import world.bentobox.bank.data.BankAccounts;
import world.bentobox.bank.data.Money;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.events.island.IslandPreclearEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.Database;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 */
public class BankManager implements Listener {
    private static final int MAX_SIZE = 20;
    private static final double MINIMUM_INTEREST = 0.01;
    static final long MILLISECONDS_IN_YEAR = (long) 1000 * 60 * 60 * 24 * 365;
    // Database handler for accounts
    private final Database<BankAccounts> handler;
    private final Bank addon;
    private final Map<String, BankAccounts> cache;
    private final Map<String, Money> balances;

    /**
     * Cached database bank manager for withdrawals, deposits and balance inquiries
     *
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

    /**
     * Load the bank balances and calculate any interest due
     *
     * @return completable future that completes when the loading is done
     */
    public CompletableFuture<Void> loadBalances() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        balances.clear();
        Bukkit.getScheduler().runTaskAsynchronously(addon.getPlugin(), () -> {
            handler.loadObjects().forEach(ba -> balances.put(ba.getUniqueId(), getBalancePlusInterest(ba)));
            future.complete(null);
        });
        return future;
    }

    /**
     * Get the new balance plus interest, if any.
     * Interest will only be calculated if the last time it was added is longer than the compound period
     *
     * @param ba - bank account
     * @return Money balance of account
     */
    private Money getBalancePlusInterest(BankAccounts ba) {
        if (System.currentTimeMillis() - ba.getInterestLastPaid() > addon.getSettings().getCompoundPeriodInMs()) {
            return calculateInterest(ba);
        }
        return ba.getBalance();
    }

    Money calculateInterest(BankAccounts ba) {
        double bal = ba.getBalance().getValue();
        // Calculate compound interest over period of time
        // a = P * (1 + r/n)^(n*t)
        /*
         * a = the total value
         * P = the initial deposit
         * r = the annual interest rate
         * n = the number of times that interest is compounded per year
         * t = the number of years the money is saved﻿
         */
        double r = (double) addon.getSettings().getInterestRate() / 100;
        long n = addon.getSettings().getCompoundPeriodsPerYear();
        double t = getYears(System.currentTimeMillis() - ba.getInterestLastPaid());
        double a = bal * Math.pow((1 + r / n), (n * t));
        double interest = a - bal;
        if (interest > MINIMUM_INTEREST) {
            addon.getIslands().getIslandById(ba.getUniqueId()).filter(i -> i.getOwner() != null).ifPresent(island -> {
                // Set the interest payment timestamp
                ba.setInterestLastPaid(System.currentTimeMillis());
                // Put this account into the cache so it will be found immediately by the set method
                cache.put(ba.getUniqueId(), ba);
                // Add the new amount
                this.set(User.getInstance(island.getOwner()), island.getUniqueId(), new Money(interest), new Money(bal + interest), TxType.INTEREST);
            });

        }
        return new Money(a);
    }

    private double getYears(long l) {
        return (double) l / MILLISECONDS_IN_YEAR;
    }

    /**
     * @param user   - depositor and island member
     * @param amount - amount
     * @param world  - island's world
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> deposit(User user, Money amount, World world) {
        // Get player's account
        Island island = addon.getIslands().getIsland(Objects.requireNonNull(Util.getWorld(world)), user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }
        return deposit(user, island, amount, TxType.DEPOSIT);
    }

    /**
     * @param user   - depositor
     * @param island - island
     * @param amount - amount
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> deposit(User user, Island island, Money amount, TxType type) {
        try {
            BankAccounts account = getAccount(island.getUniqueId());
            // Calculate interest
            this.getBalancePlusInterest(account);
            return this.set(user, island.getUniqueId(), amount, Money.add(account.getBalance(), amount), type);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR);
        }
    }

    /**
     * Gets account
     *
     * @param uuid - Island UUID
     * @return account
     * @throws IOException - if database value cannot be read
     */
    public BankAccounts getAccount(UUID uuid) throws IOException {
        return getAccount(uuid.toString());
    }

    /**
     * Gets account
     *
     * @param uuid - Island UUID
     * @return account
     * @throws IOException - if database value cannot be read
     */
    public BankAccounts getAccount(String uuid) throws IOException {
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
     * @param user   - island member
     * @param amount - amount
     * @param world  - world
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> withdraw(User user, Money amount, World world) {
        // Get player's island
        Island island = addon.getIslands().getIsland(Objects.requireNonNull(Util.getWorld(world)), user);
        if (island == null) {
            return CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND);
        }
        return withdraw(user, island, amount, TxType.WITHDRAW);
    }

    /**
     * @param user   - user withdrawing
     * @param island - island
     * @param amount - amount
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> withdraw(User user, Island island, Money amount, TxType type) {
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
        // Calculate interest
        this.getBalancePlusInterest(account);
        // Check balance
        if (Money.lessThan(account.getBalance(), amount)) {
            // Low balance
            return CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE);
        }
        // Success
        return this.set(user, island.getUniqueId(), amount, Money.subtract(account.getBalance(), amount), type);
    }


    /**
     * This method checks and returns if in given world given user bank account is at least given value of money. If
     * bank account has exactly required amount, it returns {@code true}.
     *
     * @param user  the user which account must be checked.
     * @param world the world where island is located.
     * @param value the value that must be checked.
     * @return {@code true} if in islands bank account is at least given value, {@code false} otherwise.
     */
    public boolean has(@NonNull User user, @NonNull World world, double value) {
        return has(addon.getIslands().getIsland(Objects.requireNonNull(Util.getWorld(world)), user), Money.of(value));
    }


    /**
     * This method checks and returns if in given world given user bank account is at least given value of money. If
     * bank account has exactly required amount, it returns {@code true}.
     *
     * @param user  the user which account must be checked.
     * @param world the world where island is located.
     * @param value the value that must be checked.
     * @return {@code true} if in islands bank account is at least given value, {@code false} otherwise.
     */
    public boolean has(@NonNull User user, @NonNull World world, @Nullable Money value) {
        return has(addon.getIslands().getIsland(Objects.requireNonNull(Util.getWorld(world)), user), value);
    }


    /**
     * This method checks and returns if in given island bank account is at least given value of money.
     * If bank account has exactly required amount, it returns {@code true}.
     *
     * @param island the island with bank account.
     * @param value  the value that must be checked.
     * @return {@code true} if in islands bank account is at least given value, {@code false} otherwise.
     */
    public boolean has(@Nullable Island island, double value) {
        return has(island, Money.of(value));
    }


    /**
     * This method checks and returns if in given island bank account is at least given value of money.
     * If bank account has exactly required amount, it returns {@code true}.
     *
     * @param island the island with bank account.
     * @param value  the value that must be checked.
     * @return {@code true} if in islands bank account is at least given value, {@code false} otherwise.
     */
    public boolean has(@Nullable Island island, @Nullable Money value) {
        if (island == null || value == null) {
            return false;
        } else {
            return Money.compare(this.balances.getOrDefault(island.getUniqueId(), new Money()), value) >= 0;
        }
    }


    /**
     * Get balance for island
     *
     * @param island - island
     * @return balance. 0 if unknown
     */
    public Money getBalance(@Nullable Island island) {
        if (island == null) {
            return new Money();
        }
        return balances.getOrDefault(island.getUniqueId(), new Money());
    }

    /**
     * Get balance for island
     *
     * @param island - island
     * @return balance. 0 if unknown
     */
    public Money getBalance(UUID island) {
        return balances.getOrDefault(island.toString(), new Money());
    }

    /**
     * Get balance for user in world
     *
     * @param user  - user
     * @param world - world
     * @return balance. 0 if unknown
     */
    public Money getBalance(User user, World world) {
        return getBalance(addon.getIslands().getIsland(Objects.requireNonNull(Util.getWorld(world)), user));
    }

    /**
     * Get history for island
     *
     * @param island - island
     * @return list of {@link AccountHistory}
     */
    public List<AccountHistory> getHistory(Island island) {
        try {
            BankAccounts account = getAccount(island.getUniqueId());
            // Calculate interest
            this.getBalancePlusInterest(account);
            return account.getHistory().entrySet().stream().map(en -> {
                String[] split = en.getValue().split(":");
                if (split.length == 3) {
                    TxType type = Enums.getIfPresent(TxType.class, split[1]).or(TxType.UNKNOWN);
                    return new AccountHistory(en.getKey(), split[0], Double.parseDouble(split[2]), type);
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Get balances for a world
     *
     * @param world - world
     * @return the balances
     */
    public Map<String, Money> getBalances(World world) {
        return balances.entrySet().stream()
                .filter(en -> addon.getIslands().getIslandById(en.getKey())
                        .map(i -> i.getWorld().equals(Util.getWorld(world))).orElse(false))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Sets an island's account value to an amount
     *
     * @param user       - user who is doing the setting or island owner for interest
     * @param islandID   - island unique id
     * @param amount     - amount being added or removed
     * @param newBalance - the resulting new balance
     * @param type       - type of transaction
     * @return BankResponse
     */
    public CompletableFuture<BankResponse> set(@NonNull User user, @NonNull String islandID, Money amount, Money newBalance, TxType type) {
        try {
            BankAccounts account = getAccount(islandID);
            account.setBalance(newBalance);
            account.getHistory().put(System.currentTimeMillis(), user.getName() + ":" + type + ":" + amount.getValue());
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
