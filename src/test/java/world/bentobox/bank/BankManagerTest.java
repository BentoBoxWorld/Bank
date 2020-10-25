package world.bentobox.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.IslandPreclearEvent;
import world.bentobox.bentobox.api.events.island.IslandEvent.Reason;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, DatabaseSetup.class, Util.class})
public class BankManagerTest {

    @Mock
    private Bank bank;
    // Class under test
    private BankManager bm;

    private static AbstractDatabaseHandler<Object> h;
    @Mock
    private BentoBox plugin;
    @Mock
    private Settings pluginSettings;
    @Mock
    private User user;
    @Mock
    private World world;

    private Island island;
    @Mock
    private IslandsManager im;
    private String uniqueId;
    @Mock
    private Location location;


    @SuppressWarnings("unchecked")
    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, InvocationTargetException, IntrospectionException {
        // This has to be done beforeClass otherwise the tests will interfere with each other
        h = mock(AbstractDatabaseHandler.class);
        // Database
        PowerMockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        when(DatabaseSetup.getDatabase()).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        when(bank.getPlugin()).thenReturn(plugin);
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);
        // Island manager
        when(bank.getIslands()).thenReturn(im);
        uniqueId = UUID.randomUUID().toString();
        island = new Island();
        island.setUniqueId(uniqueId);
        when(location.getWorld()).thenReturn(world);
        when(location.clone()).thenReturn(location);
        island.setCenter(location);
        when(im.getIsland(eq(world), eq(user))).thenReturn(island);

        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, World.class));

        bm = new BankManager(bank);
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        deleteAll(new File("database"));
        User.clearUsers();
        Mockito.framework().clearInlineMocks();
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#BankManager(world.bentobox.bank.Bank)}.
     */
    @Test
    public void testBankManager() {
        PowerMockito.verifyStatic(Bukkit.class);
        Bukkit.getScheduler();
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#loadBalances()}.
     */
    @Test
    public void testLoadBalances() {
        bm.loadBalances();
        PowerMockito.verifyStatic(Bukkit.class, times(2));
        Bukkit.getScheduler();
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#deposit(world.bentobox.bentobox.api.user.User, double, org.bukkit.World)}.
     */
    @Test
    public void testDepositUserDoubleWorld() {
        bm.deposit(user, 100D, world).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#deposit(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, double, world.bentobox.bank.data.TxType)}.
     */
    @Test
    public void testDepositUserIslandDoubleTxType() {
        for (TxType type : TxType.values()) {
            bm.deposit(user, island, 100D, type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#withdraw(world.bentobox.bentobox.api.user.User, double, org.bukkit.World)}.
     */
    @Test
    public void testWithdrawUserDoubleWorld() {
        bm.withdraw(user, 100D, world).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#withdraw(world.bentobox.bentobox.api.user.User, world.bentobox.bentobox.database.objects.Island, double, world.bentobox.bank.data.TxType)}.
     */
    @Test
    public void testWithdrawUserIslandDoubleTxType() {
        for (TxType type : TxType.values()) {
            bm.withdraw(user, island, 100D, type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalance(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetBalanceIsland() {
        assertEquals(0D, bm.getBalance(island), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalance(world.bentobox.bentobox.api.user.User, org.bukkit.World)}.
     */
    @Test
    public void testGetBalanceUserWorld() {
        assertEquals(0D, bm.getBalance(user, world), 0D);
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getHistory(world.bentobox.bentobox.database.objects.Island)}.
     */
    @Test
    public void testGetHistory() {
        assertTrue(bm.getHistory(island).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#getBalances()}.
     */
    @Test
    public void testGetBalances() {
        assertTrue(bm.getBalances(world).isEmpty());
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#set(world.bentobox.bentobox.api.user.User, java.lang.String, double, double, world.bentobox.bank.data.TxType)}.
     */
    @Test
    public void testSet() {
        String islandID = "";
        for (TxType type : TxType.values()) {
            bm.set(user, islandID, 10D, 100D, type).thenAccept(r -> assertEquals(BankResponse.SUCCESS, r));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.BankManager#onIslandDelete(world.bentobox.bentobox.api.events.island.IslandEvent.IslandPreclearEvent)}.
     */
    @Test
    public void testOnIslandDelete() {
        IslandPreclearEvent e = (IslandPreclearEvent) new IslandEvent.IslandEventBuilder().oldIsland(island).reason(Reason.PRECLEAR).island(island).build();
        bm.onIslandDelete(e);
        verify(h).deleteID(eq(uniqueId));
    }

}
