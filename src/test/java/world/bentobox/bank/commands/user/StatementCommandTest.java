package world.bentobox.bank.commands.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, Util.class})
public class StatementCommandTest {
    @Mock
    private CompositeCommand ic;
    @Mock
    private User user;
    @Mock
    private World world;
    @Mock
    private BentoBox plugin;
    @Mock
    private IslandsManager im;
    @Mock
    private @Nullable Island island;
    @Mock
    private Bank addon;
    @Mock
    private BankManager bankManager;
    @Mock
    private VaultHook vh;
    // Class under test
    private StatementCommand sc;
    @Mock
    private Player player;

    /**
     */
    @Before
    public void setUp() {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        when(ic.getWorld()).thenReturn(world);
        when(user.getWorld()).thenReturn(world);
        when(user.getPlayer()).thenReturn(player);

        // IWM friendly name
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.inWorld(any(World.class))).thenReturn(true);

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(eq(world), eq(user))).thenReturn(island);

        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(ic.getAddon()).thenReturn(addon);
        when(addon.getBankManager()).thenReturn(bankManager);
        when(addon.getVault()).thenReturn(vh);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);

        // Settings
        when(plugin.getSettings()).thenReturn(new Settings());

        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, World.class));

        sc = new StatementCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertTrue(sc.isOnlyPlayer());
        assertEquals("bank.user.statement", sc.getPermission());
        assertEquals("bank.statement.description", sc.getDescription());

    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgs() {
        assertFalse(sc.canExecute(user, "statement", Collections.singletonList("fff")));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        when(im.getIsland(eq(world), eq(user))).thenReturn(null);
        assertFalse(sc.canExecute(user, "statement", Collections.emptyList()));
        verify(user).sendMessage(eq("general.errors.no-island"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoRank() {
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertFalse(sc.canExecute(user, "statement", Collections.emptyList()));
        verify(user).sendMessage(eq("bank.errors.no-rank"));

    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteSuccess() {
        assertTrue(sc.canExecute(user, "statement", Collections.emptyList()));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        assertTrue(sc.execute(user, "statement", Collections.emptyList()));
        verify(user).closeInventory();
        verify(player).openInventory(any(Inventory.class));
    }
}
