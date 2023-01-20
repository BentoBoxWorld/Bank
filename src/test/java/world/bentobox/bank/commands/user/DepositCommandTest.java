package world.bentobox.bank.commands.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.bukkit.World;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.BentoBox;
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
@PrepareForTest({BentoBox.class, Util.class})
public class DepositCommandTest {

    /**
     * Class under test
     */
    private DepositCommand dct;
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

        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(ic.getAddon()).thenReturn(addon);
        when(addon.getBankManager()).thenReturn(bankManager);
        when(bankManager.getBalance(eq(island))).thenReturn(new Money(100D));
        when(addon.getVault()).thenReturn(vh);
        when(vh.getBalance(eq(user), eq(world))).thenReturn(1000D);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);
        PowerMockito.mockStatic(Util.class);
        when(Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, World.class));

        dct = new DepositCommand(ic);

    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#setup()}.
     */
    @Test
    public void testSetup() {
        assertTrue(dct.isOnlyPlayer());
        assertEquals("bank.user.deposit", dct.getPermission());
        assertEquals("bank.deposit.parameters", dct.getParameters());
        assertEquals("bank.deposit.description", dct.getDescription());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteNoArgs() {
        assertFalse(dct.canExecute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage(eq("commands.help.header"), eq(TextVariables.LABEL), eq("BSkyBlock"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNotANumber() {
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("hello")));
        verify(user).sendMessage(eq("bank.errors.must-be-a-number"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNegativeNumber() {
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("-50")));
        verify(user).sendMessage(eq("bank.errors.value-must-be-positive"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNumberNoRank() {
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage(eq("bank.errors.no-rank"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNumberNoIsland() {
        when(im.getIsland(eq(world), eq(user))).thenReturn(null);
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage(eq("general.errors.no-island"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteAllSuccess() {
        when(bankManager.getBalance(eq(user), eq(world))).thenReturn(new Money(555D));
        assertTrue(dct.canExecute(user, "deposit", Collections.singletonList("all")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNumberSuccess() {
        when(bankManager.getBalance(eq(user), eq(world))).thenReturn(new Money(555D));
        assertTrue(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringLoadError() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR));

        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.bank-error");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringVaultError() {
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.FAILURE, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);
        assertFalse(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.bank-error");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringLowBalance() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));

        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.low-balance");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringNoIsland() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND));
        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testExecuteUserStringListOfStringSuccess() {
        testCanExecuteOneArgNumberSuccess();
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.SUCCESS));
        assertTrue(dct.execute(user, "deposit", Collections.singletonList("123.30")));
        verify(vh).withdraw(eq(user), eq(123.3D), eq(world));
        verify(user).sendMessage(eq("bank.deposit.success"), eq(TextVariables.NUMBER), eq("100.0"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    public void testCanExecuteOneArgNumberLowBalance() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));
        assertTrue(dct.execute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage(eq("bank.errors.low-balance"));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#tabComplete(User, String, java.util.List)
     */
    @Test
    public void testTabComplete() {
        Optional<List<String>> value = dct.tabComplete(user, "", Collections.emptyList());
        assertTrue(value.isPresent());
        assertEquals("1000.0", value.get().get(0));
    }

}
