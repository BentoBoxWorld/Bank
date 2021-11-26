package world.bentobox.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.PlayersManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class})
public class PhManagerTest {

    // Class under test
    private PhManager pm;
    @Mock
    private Bank addon;
    @Mock
    private BankManager bm;
    @Mock
    private BentoBox plugin;
    @Mock
    private GameModeAddon gm;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private @Nullable Location location;
    private Map<String, Money> map;
    @Mock
    private PlayersManager plm;


    /**
     */
    @Before
    public void setUp() {

        AddonDescription desc = new AddonDescription.Builder("main", "AcidIsland", "1.0.2").build();
        when(gm.getDescription()).thenReturn(desc);
        when(addon.getPlugin()).thenReturn(plugin);
        when(plugin.getPlaceholdersManager()).thenReturn(phm);
        when(addon.getSettings()).thenReturn(new Settings());
        when(user.getWorld()).thenReturn(world);
        when(gm.inWorld(eq(world))).thenReturn(true);
        VaultHook vh = mock(VaultHook.class);
        when(vh.format(anyDouble())).thenAnswer(args -> "$" + args.getArgument(0, Double.class));
        when(addon.getVault()).thenReturn(vh);
        when(addon.getIslands()).thenReturn(im);
        when(user.getLocation()).thenReturn(location);
        when(im.getIslandAt(eq(location))).thenReturn(Optional.of(island));
        when(bm.getBalance(eq(island))).thenReturn(new Money(1234.56D));
        map = new LinkedHashMap<>();
        when(bm.getBalances(any())).thenReturn(map);
        when(addon.getPlayers()).thenReturn(plm);
        when(im.getIslandById(anyString())).thenAnswer(arg -> {
            String id = arg.getArgument(0);
            Island i = new Island();
            i.setUniqueId(id);
            i.setOwner(UUID.fromString(id));
            return Optional.of(i);
        });
        when(plm.getName(any())).thenAnswer(arg -> arg.getArgument(0, UUID.class).toString());
        when(user.isPlayer()).thenReturn(true);
        pm = new PhManager(addon, bm);
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#registerPlaceholders(world.bentobox.bentobox.api.addons.GameModeAddon)}.
     */
    @Test
    public void testRegisterPlaceholdersNoPHM() {
        when(plugin.getPlaceholdersManager()).thenReturn(null);
        assertFalse(pm.registerPlaceholders(gm));

    }
    /**
     * Test method for {@link world.bentobox.bank.PhManager#registerPlaceholders(world.bentobox.bentobox.api.addons.GameModeAddon)}.
     */
    @Test
    public void testRegisterPlaceholders() {
        assertTrue(pm.registerPlaceholders(gm));
        verify(phm).registerPlaceholder(eq(addon), eq("acidisland_island_balance"), any());
        for (int i = 1; i < 11; i++) {
            verify(phm).registerPlaceholder(eq(addon), eq("acidisland_top_name_" + i), any());
            verify(phm).registerPlaceholder(eq(addon), eq("acidisland_top_value_" + i), any());
        }
        verify(phm).registerPlaceholder(eq(addon), eq("acidisland_visited_island_balance"), any());
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getVisitedIslandBalance(world.bentobox.bentobox.api.addons.GameModeAddon, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetVisitedIslandBalanceWrongWorld() {
        when(gm.inWorld(eq(world))).thenReturn(false);
        assertEquals("$0.0", pm.getVisitedIslandBalance(gm, user, false, false));
        assertEquals("$0.0", pm.getVisitedIslandBalance(gm, user, true, false));
        assertEquals("0.0", pm.getVisitedIslandBalance(gm, user, false, true));
        assertEquals("0.0", pm.getVisitedIslandBalance(gm, user, true, true));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getVisitedIslandBalance(world.bentobox.bentobox.api.addons.GameModeAddon, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetVisitedIslandBalanceNoIsland() {
        when(im.getIslandAt(eq(location))).thenReturn(Optional.empty());
        assertEquals("$0.0", pm.getVisitedIslandBalance(gm, user, false, false));
        assertEquals("$0.0", pm.getVisitedIslandBalance(gm, user, true, false));
        assertEquals("0.0", pm.getVisitedIslandBalance(gm, user, false, true));
        assertEquals("0.0", pm.getVisitedIslandBalance(gm, user, true, true));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getVisitedIslandBalance(world.bentobox.bentobox.api.addons.GameModeAddon, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetVisitedIslandBalance() {
        assertEquals("$1234.56", pm.getVisitedIslandBalance(gm, user, false, false));
        assertEquals("1.2k", pm.getVisitedIslandBalance(gm, user, true, false));
        assertEquals("1234.56", pm.getVisitedIslandBalance(gm, user, false, true));
        assertEquals("1234.56", pm.getVisitedIslandBalance(gm, user, true, true));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getVisitedIslandBalance(world.bentobox.bentobox.api.addons.GameModeAddon, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetVisitedIslandBalanceLargest() {
        when(bm.getBalance(eq(island))).thenReturn(new Money(Double.MAX_VALUE));
        assertEquals("9223372T", pm.getVisitedIslandBalance(gm, user, true, false));
        assertEquals("1.7976931348623157E308", pm.getVisitedIslandBalance(gm, user, true, true));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getVisitedIslandBalance(world.bentobox.bentobox.api.addons.GameModeAddon, world.bentobox.bentobox.api.user.User)}.
     */
    @Test
    public void testGetVisitedIslandBalanceBig() {
        when(bm.getBalance(eq(island))).thenReturn(new Money(123456789D));
        assertEquals("123.5M", pm.getVisitedIslandBalance(gm, user, true, false));
        assertEquals("1.23456789E8", pm.getVisitedIslandBalance(gm, user, true, true));
    }


    /**
     * Test method for {@link world.bentobox.bank.PhManager#getRankName(org.bukkit.World, int)}.
     */
    @Test
    public void testGetRankNameSub0() {
        assertEquals("",pm.getRankName(world, -1));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getRankName(org.bukkit.World, int)}.
     */
    @Test
    public void testGetRankNameOverMax() {
        assertEquals("", pm.getRankName(world, 100));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getRankName(org.bukkit.World, int)}.
     */
    @Test
    public void testGetRankNameNone() {
        for (int i = 1; i < 11; i++) {
            assertEquals("", pm.getRankName(world, i));
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getRankName(org.bukkit.World, int)}.
     */
    @Test
    public void testGetRankName() {
        assertEquals("", pm.getRankName(world, 5));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#getRankBalance(org.bukkit.World, int)}.
     */
    @Test
    public void testGetRankBalance() {
        assertEquals("", pm.getRankBalance(world, 5));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#checkCache(World, int)}.
     */
    @Test
    public void testCheckCache() {
        assertEquals(5, pm.checkCache(world, 5));
        verify(bm).getBalances(world);
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#checkCache(World, int)}.
     */
    @Test
    public void testCheckCacheWithBalances() {
        map.put(UUID.randomUUID().toString(), new Money(123.45));
        map.put(UUID.randomUUID().toString(), new Money(1230.45));
        map.put(UUID.randomUUID().toString(), new Money(12300.45));
        map.put(UUID.randomUUID().toString(), new Money(12.45));
        map.put(UUID.randomUUID().toString(), new Money(12343.45));
        map.put(UUID.randomUUID().toString(), new Money(1.45));
        map.put(UUID.randomUUID().toString(), new Money(1345.45));
        map.put(UUID.randomUUID().toString(), new Money(1345.45));
        map.put(UUID.randomUUID().toString(), new Money(1345.45));
        map.put(UUID.randomUUID().toString(), new Money(100.45));
        map.put(UUID.randomUUID().toString(), new Money(100.4556786));
        map.put(UUID.randomUUID().toString(), new Money(10000.45));
        map.put(UUID.randomUUID().toString(), new Money(1000000.45));
        when(bm.getBalances(world)).thenReturn(map);
        for (int i = 1; i < 11; i++) {
            pm.checkCache(world, i);
            assertEquals(pm.getBalances().get(i-1), "$" + map.get(pm.getNames().get(i - 1)).getValue());
        }
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#checkCache(World, int)}.
     */
    @Test
    public void testCheckCacheNoNamesChange() {
        pm.setLastSorted(System.currentTimeMillis() + 10000);
        assertEquals(5, pm.checkCache(world, 5));
        verify(bm).getBalances(eq(world));
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#checkCache(World, int)}.
     */
    @Test
    public void testCheckCacheNoChange() {
        // Cache should not refresh because there is a name and the time is in the future
        pm.setNames(Collections.singletonList("tastybento"));
        pm.setLastSorted(System.currentTimeMillis() + 10000);
        long ls = pm.getLastSorted();
        assertEquals(5, pm.checkCache(world, 5));
        assertEquals(ls, pm.getLastSorted());
    }

    /**
     * Test method for {@link world.bentobox.bank.PhManager#checkCache(World, int)}.
     */
    @Test
    public void testCheckCacheOutOfBounds() {
        assertEquals(1, pm.checkCache(world, 0));
        assertEquals(10, pm.checkCache(world, 100));
    }

}
