package world.bentobox.bank;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.database.AbstractDatabaseHandler;
import world.bentobox.bentobox.database.DatabaseSetup;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;

/**
 * Tests for {@link Bank}.
 */
class BankTest extends CommonTestSetup {

    @Mock
    private GameModeAddon gameMode;
    @Mock
    private AddonsManager am;
    @Mock
    private CompositeCommand cmd;
    @Mock
    private CompositeCommand adminCmd;
    @Mock
    private VaultHook vh;

    private Bank addon;
    private File jFile;
    private MockedStatic<DatabaseSetup> mockDb;

    @SuppressWarnings("unchecked")
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Database mock
        AbstractDatabaseHandler<Object> h = mock(AbstractDatabaseHandler.class);
        mockDb = Mockito.mockStatic(DatabaseSetup.class);
        DatabaseSetup dbSetup = mock(DatabaseSetup.class);
        mockDb.when(DatabaseSetup::getDatabase).thenReturn(dbSetup);
        when(dbSetup.getHandler(any())).thenReturn(h);
        when(h.saveObject(any())).thenReturn(CompletableFuture.completedFuture(true));

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Addons manager - one game mode (BSkyBlock)
        when(plugin.getAddonsManager()).thenReturn(am);
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test")
                .authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);
        when(gameMode.getOverWorld()).thenReturn(world);
        when(gameMode.getPlayerCommand()).thenReturn(Optional.of(cmd));
        when(gameMode.getAdminCommand()).thenReturn(Optional.of(adminCmd));

        // Flags manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());

        // Placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // World
        when(world.getName()).thenReturn("bskyblock-world");

        // Vault
        when(plugin.getVault()).thenReturn(Optional.of(vh));

        // Build the addon jar containing the real config.yml
        jFile = new File("addon.jar");
        String configYml = new String(Files.readAllBytes(Paths.get("src/main/resources/config.yml")),
                StandardCharsets.UTF_8);
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jFile))) {
            addJarEntry(jos, "config.yml", configYml);
        }

        addon = new Bank();
        addon.setDataFolder(new File("addons/Bank"));
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "Bank", "1.3").description("test")
                .authors("tastybento").build();
        addon.setDescription(desc);
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        if (mockDb != null) {
            mockDb.closeOnDemand();
        }
        super.tearDown();
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    private static void addJarEntry(JarOutputStream jos, String name, String content) throws Exception {
        JarEntry entry = new JarEntry(name);
        jos.putNextEntry(entry);
        jos.write(content.getBytes(StandardCharsets.UTF_8));
        jos.closeEntry();
    }

    @Test
    void testOnEnableNoVault() {
        when(plugin.getVault()).thenReturn(Optional.empty());
        addon.onEnable();
        verify(plugin).logError("[Bank] Vault is required - disabling Bank - please install the Vault plugin");
    }

    @Test
    void testOnEnableNoPlaceholderManager() {
        when(plugin.getPlaceholdersManager()).thenReturn(null);
        addon.onEnable();
        verify(plugin).log("[Bank] Hooking into BSkyBlock");
        verify(plugin).logError("[Bank] Could not register placeholders because there is no PlaceholderManager");
    }

    @Test
    void testOnEnable() {
        addon.onEnable();
        verify(plugin).log("[Bank] Hooking into BSkyBlock");
        // Placeholders
        verify(phm).registerPlaceholder(eq(addon), eq("bskyblock_island_balance"), any());
        verify(phm).registerPlaceholder(eq(addon), eq("bskyblock_visited_island_balance"), any());
        for (int i = 1; i < 11; i++) {
            verify(phm).registerPlaceholder(eq(addon), eq("bskyblock_top_name_" + i), any());
            verify(phm).registerPlaceholder(eq(addon), eq("bskyblock_top_value_" + i), any());
        }
    }

    @Test
    void testGetSettings() {
        NullPointerException ex = assertThrows(NullPointerException.class, () -> addon.getSettings());
        org.junit.jupiter.api.Assertions.assertEquals("Settings not initialized?", ex.getMessage());
    }

    @Test
    void testGetVault() {
        assertNull(addon.getVault());
    }

    @Test
    void testGetBankManager() {
        assertNull(addon.getBankManager());
    }

}
