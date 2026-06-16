package world.bentobox.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bank.data.BankAccounts;
import world.bentobox.bank.data.Money;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.api.events.IslandBaseEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;
import world.bentobox.bentobox.api.events.island.IslandPreclearEvent;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
class BankManagerTest extends CommonTestSetup {

    @Mock
    private Bank addon;
    // Class under test
    private BankManager bm;

    @SuppressWarnings("rawtypes")
    private AbstractDatabaseHandler h;
    @Mock
    private User user;

    private Island island;
    private String uniqueId;
    private world.bentobox.bank.Settings settings;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        when(addon.getPlugin()).thenReturn(plugin);

        // Island manager
        when(addon.getIslands()).thenReturn(im);
        uniqueId = UUID.randomUUID().toString();
        island = new Island();
        island.setUniqueId(uniqueId);
        island.setCenter(location);
        when(im.getIsland(world, user)).thenReturn(island);

        // Util.getWorld returns the world passed in
        mockedUtil.when(() -> Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, org.bukkit.World.class));

        // Addon settings
        settings = new world.bentobox.bank.Settings();
        when(addon.getSettings()).thenReturn(settings);

        bm = new BankManager(addon);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#BankManager(world.bentobox.bank.Bank)}.
     */
    @Test
    void testBankManager() {
        mockedBukkit.verify(Bukkit::getScheduler);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#loadBalances()}.
     */
    @Test
    void testLoadBalances() {
        bm.loadBalances();
        mockedBukkit.verify(Bukkit::getScheduler, times(2));
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#deposit(world.bentobox.bentobox.api.user.User, world.bentobox.bank.data.Money, org.bukkit.World)}.
     */
    @Test
    void testDepositUserDoubleWorld() {
        bm.deposit(user, new Money(100), world).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#deposit(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.bank.data.Money, world.bentobox.bank.data.TxType)}.
     */
    @Test
    void testDepositUserIslandDoubleTxType() {
        for (TxType type : TxType.values()) {
            bm.deposit(user, island, new Money(100), type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#withdraw(world.bentobox.bentobox.api.user.User, world.bentobox.bank.data.Money, org.bukkit.World)}.
     */
    @Test
    void testWithdrawUserDoubleWorld() {
        bm.withdraw(user, new Money(100), world).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#withdraw(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, world.bentobox.bank.data.Money, world.bentobox.bank.data.TxType)}.
     */
    @Test
    void testWithdrawUserIslandDoubleTxType() {
        for (TxType type : TxType.values()) {
            bm.withdraw(user, island, new Money(100), type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalance(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetBalanceIsland() {
        assertEquals(0D, bm.getBalance(island).getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalance(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    void testGetBalanceUserWorld() {
        assertEquals(0D, bm.getBalance(user, world).getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getHistory(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    void testGetHistory() {
        assertTrue(bm.getHistory(island).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalances(org.bukkit.World)}.
     */
    @Test
    void testGetBalances() {
        assertTrue(bm.getBalances(world).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#set(world.bentobox.bentobox.api.user.User, java.lang.String, world.bentobox.bank.data.Money, world.bentobox.bank.data.Money, world.bentobox.bank.data.TxType)}.
     */
    @Test
    void testSet() {
        String islandID = "";
        for (TxType type : TxType.values()) {
            bm.set(user, islandID, new Money(10), new Money(100), type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#onIslandDelete(world.bentobox.bentobox.api.events.island.IslandEvent.IslandPreclearEvent)}.
     */
    @Test
    void testOnIslandDelete() {
        IslandBaseEvent e = new IslandEvent.IslandEventBuilder().oldIsland(island).reason(Reason.PRECLEAR).island(island).build();
        if (e.getNewEvent().isPresent()) {
            e = e.getNewEvent().get();
        }
        bm.onIslandDelete((IslandPreclearEvent) e);
        verify(h).deleteID(uniqueId);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#calculateInterest(world.bentobox.bank.data.BankAccounts)}.
     */
    @Test
    void testOnCalculateInterestZeroBalance() {
        BankAccounts ba = new BankAccounts();
        assertEquals(0D, bm.calculateInterest(ba).getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#calculateInterest(world.bentobox.bank.data.BankAccounts)}.
     */
    @Test
    void testOnCalculateInterestOneYear10() {
        calculate(10, 11051.56D, BankManager.MILLISECONDS_IN_YEAR);
        BankAccounts ba = new BankAccounts();
        settings.setInterestRate(10); // 10%
        ba.setBalance(new Money(10000));
        ba.setInterestLastPaid(System.currentTimeMillis() - BankManager.MILLISECONDS_IN_YEAR);
        assertEquals(11051.56D, bm.calculateInterest(ba).getValue(), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#calculateInterest(world.bentobox.bank.data.BankAccounts)}.
     */
    @Test
    void testOnCalculateInterestOneDay10() {
        calculate(10, 10002.74D, (long) 24 * 60 * 60 * 1000);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#calculateInterest(world.bentobox.bank.data.BankAccounts)}.
     */
    @Test
    void testOnCalculateInterestOneYear25() {
        calculate(25, 12839.16D, BankManager.MILLISECONDS_IN_YEAR);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#calculateInterest(world.bentobox.bank.data.BankAccounts)}.
     */
    @Test
    void testOnCalculateInterestOneYear0() {
        calculate(0, 10000D, BankManager.MILLISECONDS_IN_YEAR);
    }

    private void calculate(int i, double d, long milliseconds) {
        BankAccounts ba = new BankAccounts();
        settings.setInterestRate(i);
        ba.setBalance(new Money(10000));
        ba.setInterestLastPaid(System.currentTimeMillis() - milliseconds);
        assertEquals(d, bm.calculateInterest(ba).getValue(), 0D);
    }

}
