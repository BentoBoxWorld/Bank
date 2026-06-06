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

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
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
class DepositCommandTest extends CommonTestSetup {

    /**
     * Class under test
     */
    private DepositCommand dct;
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
        when(bankManager.getBalance(island)).thenReturn(new Money(100D));
        when(addon.getVault()).thenReturn(vh);
        when(vh.getBalance(user, world)).thenReturn(1000D);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);

        // Util.getWorld returns the world passed in
        mockedUtil.when(() -> Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, org.bukkit.World.class));

        dct = new DepositCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertTrue(dct.isOnlyPlayer());
        assertEquals("bank.user.deposit", dct.getPermission());
        assertEquals("bank.deposit.parameters", dct.getParameters());
        assertEquals("bank.deposit.description", dct.getDescription());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoArgs() {
        assertFalse(dct.canExecute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("commands.help.header", TextVariables.LABEL, "BSkyBlock");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNotANumber() {
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("hello")));
        verify(user).sendMessage("bank.errors.must-be-a-number");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNegativeNumber() {
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("-50")));
        verify(user).sendMessage("bank.errors.value-must-be-positive");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberNoRank() {
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage("bank.errors.no-rank");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        assertFalse(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteAllSuccess() {
        when(bankManager.getBalance(user, world)).thenReturn(new Money(555D));
        assertTrue(dct.canExecute(user, "deposit", Collections.singletonList("all")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberSuccess() {
        when(bankManager.getBalance(user, world)).thenReturn(new Money(555D));
        assertTrue(dct.canExecute(user, "deposit", Collections.singletonList("123.30")));
        verify(user, never()).sendMessage(any());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringLoadError() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOAD_ERROR));

        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.bank-error");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringVaultError() {
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.FAILURE, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);
        assertFalse(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.bank-error");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringLowBalance() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));

        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.low-balance");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringNoIsland() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_NO_ISLAND));
        assertTrue(dct.execute(user, "deposit", Collections.emptyList()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfStringSuccess() {
        testCanExecuteOneArgNumberSuccess();
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.SUCCESS));
        assertTrue(dct.execute(user, "deposit", Collections.singletonList("123.30")));
        verify(vh).withdraw(user, 123.3D, world);
        verify(user).sendMessage("bank.deposit.success", TextVariables.NUMBER, "100.0");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteOneArgNumberLowBalance() {
        when(bankManager.deposit(eq(user), any(), eq(world))).thenReturn(CompletableFuture.completedFuture(BankResponse.FAILURE_LOW_BALANCE));
        assertTrue(dct.execute(user, "deposit", Collections.singletonList("123.30")));
        verify(user).sendMessage("bank.errors.low-balance");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.DepositCommand#tabComplete(User, String, java.util.List)}.
     */
    @Test
    void testTabComplete() {
        Optional<List<String>> value = dct.tabComplete(user, "", Collections.emptyList());
        assertTrue(value.isPresent());
        assertEquals("1000.0", value.get().get(0));
    }

}
