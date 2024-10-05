package world.bentobox.bank;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.Settings;
import world.bentobox.bentobox.api.addons.AddonDescription;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.DatabaseSetup.DatabaseType;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;


@SuppressWarnings("deprecation")
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Bukkit.class, BentoBox.class, User.class, IslandsManager.class })
public class BankTest {

    private static File jFile;
    @Mock
    private User user;
    @Mock
    private IslandsManager im;
    @Mock
    private Island island;
    @Mock
    private BentoBox plugin;
    @Mock
    private FlagsManager fm;
    @Mock
    private GameModeAddon gameMode;
    @Mock
    private AddonsManager am;
    @Mock
    private BukkitScheduler scheduler;

    @Mock
    private Settings pluginSettings;

    private Bank addon;

    @Mock
    private Logger logger;
    @Mock
    private PlaceholdersManager phm;
    @Mock
    private CompositeCommand cmd;
    @Mock
    private CompositeCommand adminCmd;
    @Mock
    private World world;
    private UUID uuid;

    @Mock
    private PluginManager pim;
    @Mock
    private VaultHook vh;

    @BeforeClass
    public static void beforeClass() throws IOException {
        // Make the addon jar
        jFile = new File("addon.jar");
        // Copy over config file from src folder
        Path fromPath = Paths.get("src/main/resources/config.yml");
        Path path = Paths.get("config.yml");
        Files.copy(fromPath, path);
        try (JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(jFile))) {
            //Added the new files to the jar.
            try (FileInputStream fis = new FileInputStream(path.toFile())) {
                byte[] buffer = new byte[1024];
                int bytesRead = 0;
                JarEntry entry = new JarEntry(path.toString());
                tempJarOutputStream.putNextEntry(entry);
                while((bytesRead = fis.read(buffer)) != -1) {
                    tempJarOutputStream.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(Bukkit.class, Mockito.RETURNS_MOCKS);
        PowerMockito.mockStatic(IslandsManager.class, Mockito.RETURNS_MOCKS);
        // Set up plugin
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // The database type has to be created one line before the thenReturn() to work!
        DatabaseType value = DatabaseType.JSON;
        when(plugin.getSettings()).thenReturn(pluginSettings);
        when(pluginSettings.getDatabaseType()).thenReturn(value);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);

        // Player has island to begin with
        when(im.getIsland(any(), any(UUID.class))).thenReturn(island);
        when(plugin.getIslands()).thenReturn(im);

        // Locales
        // Return the reference (USE THIS IN THE FUTURE)
        when(user.getTranslation(anyString())).thenAnswer((Answer<String>) invocation -> invocation.getArgument(0, String.class));

        // Server
        Server server = mock(Server.class);
        when(Bukkit.getServer()).thenReturn(server);
        when(Bukkit.getLogger()).thenReturn(Logger.getAnonymousLogger());
        when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

        // Addon
        addon = new Bank();
        File dataFolder = new File("addons/Bank");
        addon.setDataFolder(dataFolder);
        addon.setFile(jFile);
        AddonDescription desc = new AddonDescription.Builder("bentobox", "Bank", "1.3").description("test").authors("tastybento").build();
        addon.setDescription(desc);
        // Addons manager
        when(plugin.getAddonsManager()).thenReturn(am);
        // One game mode
        when(am.getGameModeAddons()).thenReturn(Collections.singletonList(gameMode));
        AddonDescription desc2 = new AddonDescription.Builder("bentobox", "BSkyBlock", "1.3").description("test").authors("tasty").build();
        when(gameMode.getDescription()).thenReturn(desc2);
        when(gameMode.getOverWorld()).thenReturn(world);

        // Player command
        @NonNull
        Optional<CompositeCommand> opCmd = Optional.of(cmd);
        when(gameMode.getPlayerCommand()).thenReturn(opCmd);
        // Admin command
        Optional<CompositeCommand> opAdminCmd = Optional.of(adminCmd);
        when(gameMode.getAdminCommand()).thenReturn(opAdminCmd);

        // Flags manager
        when(plugin.getFlagsManager()).thenReturn(fm);
        when(fm.getFlags()).thenReturn(Collections.emptyList());


        // Bukkit
        when(Bukkit.getScheduler()).thenReturn(scheduler);
        ItemMeta meta = mock(ItemMeta.class);
        ItemFactory itemFactory = mock(ItemFactory.class);
        when(itemFactory.getItemMeta(any())).thenReturn(meta);
        when(Bukkit.getItemFactory()).thenReturn(itemFactory);
        UnsafeValues unsafe = mock(UnsafeValues.class);
        when(unsafe.getDataVersion()).thenReturn(777);
        when(Bukkit.getUnsafe()).thenReturn(unsafe);
        when(Bukkit.getPluginManager()).thenReturn(pim);

        // placeholders
        when(plugin.getPlaceholdersManager()).thenReturn(phm);

        // World
        when(world.getName()).thenReturn("bskyblock-world");
        // Island
        when(island.getWorld()).thenReturn(world);
        when(island.getOwner()).thenReturn(uuid);

        // Vault
        when(plugin.getVault()).thenReturn(Optional.of(vh));
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        deleteAll(new File("database"));
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        new File("addon.jar").delete();
        new File("config.yml").delete();
        deleteAll(new File("addons"));
    }

    private static void deleteAll(File file) throws IOException {
        if (file.exists()) {
            Files.walk(file.toPath())
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    @Test
    public void testOnEnableNoVault() {
        when(plugin.getVault()).thenReturn(Optional.empty());
        addon.onEnable();
        verify(plugin).logError("[Bank] Vault is required - disabling Bank - please install the Vault plugin");
    }

    @Test
    public void testOnEnableNoPlaceholderManager() {
        when(plugin.getPlaceholdersManager()).thenReturn(null);
        addon.onEnable();
        verify(plugin).log("[Bank] Hooking into BSkyBlock");
        verify(plugin).logError("[Bank] Could not register placeholders because there is no PlaceholderManager");
    }

    @Test
    public void testOnEnable() {
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

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testGetSettings() {
        exceptionRule.expect(NullPointerException.class);
        exceptionRule.expectMessage("Settings not initialized?");
        addon.getSettings();
    }

    @Test
    public void testGetVault() {
        assertNull(addon.getVault());
    }

    @Test
    public void testGetBankManager() {
        assertNull(addon.getBankManager());
    }

}
