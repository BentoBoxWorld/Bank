package world.bentobox.bank.commands.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.CommonTestSetup;
import world.bentobox.bank.data.Money;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
class AdminBalanceCommandTest extends CommonTestSetup {

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
    @Mock
    private PlayersManager pm;
    // Class under test
    private AdminBalanceCommand bc;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(ic.getWorld()).thenReturn(world);
        when(ic.getAddon()).thenReturn(addon);
        when(user.getWorld()).thenReturn(world);

        // IWM friendly name
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");

        // Islands
        when(im.getIsland(world, user)).thenReturn(island);

        // Players
        when(addon.getPlayers()).thenReturn(pm);
        when(pm.getUser("tastybento")).thenReturn(user);

        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(addon.getBankManager()).thenReturn(bankManager);
        when(addon.getVault()).thenReturn(vh);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);

        // Util.getWorld returns the world passed in
        mockedUtil.when(() -> Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, org.bukkit.World.class));
        // Default 0 balance for unknown islands
        when(bankManager.getBalance(any())).thenReturn(new Money());

        bc = new AdminBalanceCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertFalse(bc.isOnlyPlayer());
        assertEquals("bank.admin.balance", bc.getPermission());
        assertEquals("bank.admin.balance.parameters", bc.getParameters());
        assertEquals("bank.admin.balance.description", bc.getDescription());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteArgsNoArgs() {
        assertFalse(bc.canExecute(user, "balance", Collections.emptyList()));
        verify(user).sendMessage("commands.help.header", TextVariables.LABEL, "BSkyBlock");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        assertFalse(bc.canExecute(user, "balance", Collections.singletonList("tastybento")));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteUnknownTarget() {
        when(pm.getUser(anyString())).thenReturn(null);
        assertFalse(bc.canExecute(user, "balance", Collections.singletonList("bonne")));
        verify(user).sendMessage("general.errors.unknown-player", TextVariables.NAME, "bonne");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoRank() {
        // Should be ignored because this is an admin command
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertTrue(bc.canExecute(user, "balance", Collections.singletonList("tastybento")));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteSuccess() {
        assertTrue(bc.canExecute(user, "balance", Collections.singletonList("tastybento")));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.admin.AdminBalanceCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        assertTrue(bc.execute(user, "balance", Collections.singletonList("tastybento")));
        verify(user).sendMessage("bank.balance.island-balance", TextVariables.NUMBER, "0.0");
    }

}
