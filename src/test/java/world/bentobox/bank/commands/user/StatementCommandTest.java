package world.bentobox.bank.commands.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import world.bentobox.bank.Bank;
import world.bentobox.bank.BankManager;
import world.bentobox.bank.CommonTestSetup;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.util.Util;

/**
 * @author tastybento
 *
 */
class StatementCommandTest extends CommonTestSetup {

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
    private Player player;
    // Class under test
    private StatementCommand sc;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        when(ic.getWorld()).thenReturn(world);
        when(ic.getAddon()).thenReturn(addon);
        when(user.getWorld()).thenReturn(world);
        when(user.getPlayer()).thenReturn(player);

        // IWM friendly name
        when(iwm.getFriendlyName(any())).thenReturn("BSkyBlock");

        // Islands
        when(im.getIsland(world, user)).thenReturn(island);
        // Island flag allowed
        when(island.isAllowed(eq(user), any())).thenReturn(true);

        when(addon.getBankManager()).thenReturn(bankManager);
        when(addon.getVault()).thenReturn(vh);
        when(vh.format(anyDouble())).thenAnswer(i -> String.valueOf(i.getArgument(0, Double.class)));
        EconomyResponse er = new EconomyResponse(0, 0, ResponseType.SUCCESS, "");
        when(vh.withdraw(eq(user), anyDouble(), eq(world))).thenReturn(er);

        // Util.getWorld returns the world passed in
        mockedUtil.when(() -> Util.getWorld(any())).thenAnswer(arg -> arg.getArgument(0, org.bukkit.World.class));

        sc = new StatementCommand(ic);
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#setup()}.
     */
    @Test
    void testSetup() {
        assertTrue(sc.isOnlyPlayer());
        assertEquals("bank.user.statement", sc.getPermission());
        assertEquals("bank.statement.description", sc.getDescription());
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteArgs() {
        assertFalse(sc.canExecute(user, "statement", Collections.singletonList("fff")));
        verify(user).sendMessage("commands.help.header", TextVariables.LABEL, "BSkyBlock");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoIsland() {
        when(im.getIsland(world, user)).thenReturn(null);
        assertFalse(sc.canExecute(user, "statement", Collections.emptyList()));
        verify(user).sendMessage("general.errors.no-island");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteNoRank() {
        when(island.isAllowed(eq(user), any())).thenReturn(false);
        assertFalse(sc.canExecute(user, "statement", Collections.emptyList()));
        verify(user).sendMessage("bank.errors.no-rank");
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#canExecute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testCanExecuteSuccess() {
        assertTrue(sc.canExecute(user, "statement", Collections.emptyList()));
    }

    /**
     * Test method for {@link world.bentobox.bank.commands.user.StatementCommand#execute(world.bentobox.bentobox.api.user.User, java.lang.String, java.util.List)}.
     */
    @Test
    void testExecuteUserStringListOfString() {
        assertTrue(sc.execute(user, "statement", Collections.emptyList()));
        verify(user).closeInventory();
        verify(player).openInventory(any(Inventory.class));
    }
}
