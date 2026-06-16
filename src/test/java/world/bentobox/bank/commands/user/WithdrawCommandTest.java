package world.bentobox.bank.commands.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.BankResponse;
import world.bentobox.bank.CommonTestSetup;
import world.bentobox.bank.Settings;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
class WithdrawCommandTest extends CommonTestSetup {

    /**
     * Class under test
     */
    private WithdrawCommand wct;
    @Mock
    private CompositeCommand ic;
    @Mock
    private User user;
    @Mock
    private Bank addon;
    @Mock
    private BankManager bankManager;
    @Mock
    private VaultHook vh;
    private Settings settings;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(ic.getWorld()).thenReturn(world);
        when(ic.getAddon()).thenReturn(addon);
        when(user.getWorld()).thenReturn(world);
        when(user.getUniqueId()).thenReturn(UUID.randomUUID());

        // IWM friendly name
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");

        // Islands
        when(im.getIsland(world, user)).thenReturn(island);
        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        // Settings
        settings = new Settings();
        when(addon.getSettings()).thenReturn(settings);

        when(addon.getBankManager()).thenReturn(bankManager);
        when(bankManager.getBalance(any(), any())).thenReturn(new Money());
        when(bankManager.getBalance(island)).thenReturn(new Money(100D));
        when(addon.getVault()).thenReturn(vh);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));

        // Util.getWorld returns the world passed in
        mockedUtil.when(() -> Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, org.bukkit.World.class));

        wct = new WithdrawCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertTrue(wct.isOnlyPlayer());
        assertEquals("bank.user.withdraw", wct.getPermission());
        assertEquals("bank.withdraw.parameters", wct.getParameters());
        assertEquals("bank.withdraw.description", wct.getDescription());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoArgs() {
        assertFalse(wct.canExecute(user, "withdraw", Collections.emptyList()));
        verify(user).sendMessage("commands.help.header", TextVariables.LABEL, "BSkyBlock");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNotANumber() {
        assertFalse(wct.canExecute(user, "withdraw", Collections.singletonList("hello")));
        verify(user).sendMessage("bank.errors.must-be-a-number");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNegativeNumber() {
        assertFalse(wct.canExecute(user, "withdraw", Collections.singletonList("-50")));
        verify(user).sendMessage("bank.errors.value-must-be-positive");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberLowBalance() {
        assertFalse(wct.canExecute(user, "withdraw", Collections.singletonList("123.30")));
        verify(user, never()).sendMessage("bank.errors.must-be-a-number");
        verify(user).sendMessage("bank.errors.low-balance");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteAllArg() {
        when(bankManager.getBalance(user, world)).thenReturn(new Money(555D));
        assertTrue(wct.canExecute(user, "withdraw", Collections.singletonList("all")));
        verify(user, never()).sendMessage("bank.errors.must-be-a-number");
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberNoRank() {
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertFalse(wct.canExecute(user, "withdraw", Collections.singletonList("123.30")));
        verify(user).sendMessage("bank.errors.no-rank");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        assertFalse(wct.canExecute(user, "withdraw", Collections.singletonList("123.30")));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberSuccess() {
        when(bankManager.getBalance(user, world)).thenReturn(new Money(555D));
        assertTrue(wct.canExecute(user, "withdraw", Collections.singletonList("123.30")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringLoadError() {
        when(bankManager.withdraw(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR));
        assertTrue(wct.execute(user, "withdraw", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.bank-error");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringLowBalance() {
        when(bankManager.withdraw(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));
        assertTrue(wct.execute(user, "withdraw", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.low-balance");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringNoIsland() {
        when(bankManager.withdraw(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND));
        assertTrue(wct.execute(user, "withdraw", Collections.emptyList()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringSuccess() {
        testCanExecuteOneArgNumberSuccess();
        when(bankManager.withdraw(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.SUCCESS));
        assertTrue(wct.execute(user, "withdraw", Collections.singletonList("123.30")));
        verify(vh).deposit(user, 123.3D, world);
        verify(user).sendMessage("bank.withdraw.success", TextVariables.NUMBER, "100.0");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.WithdrawCommand#tabComplete(User, String, java.util.List)}.
     */
    @Test
    void testTabComplete() {
        Optional<List<String>> value = wct.tabComplete(user, "", Collections.emptyList());
        assertTrue(value.isPresent());
        assertEquals("0.0", value.get().get(0));
        when(bankManager.getBalance(any(), any())).thenReturn(new Money(12345D));
        value = wct.tabComplete(user, "", Collections.emptyList());
        assertEquals("12345.0", value.get().get(0));
    }

}
