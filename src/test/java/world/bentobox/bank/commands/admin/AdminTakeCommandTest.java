package world.bentobox.bank.commands.admin;

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

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.data.Money;
import world.bentobox.bank.data.TxType;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlayersManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BentoBox.class)
public class AdminTakeCommandTest {

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
    private AdminTakeCommand bc;
    @Mock
    private PlayersManager pm;
    /**
     */
    @Before
    public void setUp() {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        when(ic.getWorld()).thenReturn(world);
        when(user.getWorld()).thenReturn(world);

        // IWM friendly name
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");
        when(plugin.getIWM()).thenReturn(iwm);
        when(iwm.inWorld(any(World.class))).thenReturn(true);

        // Islands
        when(plugin.getIslands()).thenReturn(im);
        when(im.getIsland(eq(world), eq(user))).thenReturn(island);

        // Players
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getUser(eq("tastybento"))).thenReturn(user);
        when(user.getName()).thenReturn("tastybento");

        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(ic.getAddon()).thenReturn(addon);
        when(addon.getBankManager()).thenReturn(bankManager);
        when(addon.getVault()).thenReturn(vh);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);
        // Always successful taking
        when(bankManager.withdraw(eq(user), any(), any(), eq(TxType.TAKE))).thenReturn(CompletableFuture.completedFuture(BankResponse.SUCCESS));
        // set default balance to 0 for unknown island
        when(bankManager.getBalance(any())).thenReturn(new Money());

        bc = new AdminTakeCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertFalse(bc.isOnlyPlayer());
        assertEquals("bank.admin.take", bc.getPermission());
        assertEquals("bank.admin.take.parameters", bc.getParameters());
        assertEquals("bank.admin.take.description", bc.getDescription());

    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgsNoArgs() {
        assertFalse(bc.canExecute(user, "take", Collections.emptyList()));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteArgsOneArg() {
        assertFalse(bc.canExecute(user, "take", Collections.singletonList("tastybento")));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoIsland() {
        when(im.getIsland(eq(world), eq(user))).thenReturn(null);
        assertFalse(bc.canExecute(user, "take", Arrays.asList("tastybento", "100")));
        verify(user).sendMessage(eq("general.errors.no-island"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteUnknownTarget() {
        when(pm.getUser(anyString())).thenReturn(null);
        assertFalse(bc.canExecute(user, "take", Arrays.asList("bonne", "100")));
        verify(user).sendMessage(eq("general.errors.unknown-player"), eq(TextVariables.NAME), eq("bonne"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNotANumber() {
        assertFalse(bc.canExecute(user, "take", Arrays.asList("tastybento", "xxx")));
        verify(user).sendMessage(eq("bank.errors.must-be-a-number"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNegativeNumber() {
        assertFalse(bc.canExecute(user, "take", Arrays.asList("tastybento", "-99")));
        verify(user).sendMessage(eq("bank.errors.value-must-be-positive"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoRank() {
        // Should be ignored because this is an admin command
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertTrue(bc.canExecute(user, "take", Arrays.asList("tastybento", "100")));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteSuccess() {
        assertTrue(bc.canExecute(user, "take", Arrays.asList("tastybento", "100")));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfString() {
        testCanExecuteSuccess();
        assertTrue(bc.execute(user, "take", Arrays.asList("tastybento", "100")));
        verify(user).sendMessage(eq("bank.admin.give.success"),
                eq(TextVariables.NAME), eq("tastybento"),
                eq(TextVariables.NUMBER), eq("0.0"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringLowBalance() {
        testCanExecuteSuccess();
        when(bankManager.withdraw(eq(user), any(), any(), eq(TxType.TAKE))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));
        assertTrue(bc.execute(user, "take", Arrays.asList("tastybento", "100")));
        verify(user).sendMessage(eq("bank.errors.too-low"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminTakeCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringError() {
        when(bankManager.withdraw(eq(user), any(), any(), eq(TxType.TAKE))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR));
        assertTrue(bc.execute(user, "take", Arrays.asList("tastybento", "100")));
        verify(user).sendMessage(eq("bank.errors.bank-error"));
    }


}
