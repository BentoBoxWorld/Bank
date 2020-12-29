package world.bentobox.bank;

import org.bukkit.Material;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.commands.admin.AdminCommand;
import world.bentobox.bank.commands.user.UserCommand;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.flags.Flag;
import world.bentobox.bentobox.api.flags.Flag.Mode;
import world.bentobox.bentobox.api.flags.Flag.Type;
import world.bentobox.bentobox.api.flags.clicklisteners.CycleClick;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.managers.RanksManager;

/**
 * Main class of the Bank addon.
 * @author tastybento
 */
public class Bank extends Addon {

    private final Config<Settings> config = new Config<>(this, Settings.class);
    private @Nullable Settings settings;
    private VaultHook vault;
    private BankManager bankManager;
    public static final Flag BANK_ACCESS = new Flag.Builder("BANK_ACCESS", Material.GOLD_INGOT)
            .mode(Mode.BASIC)
            .type(Type.PROTECTION)
            .clickHandler(new CycleClick("BANK_ACCESS", RanksManager.MEMBER_RANK, RanksManager.OWNER_RANK))
            .build();

    @Override
    public void onEnable() {
        // Register flag
        this.registerFlag(BANK_ACCESS);
        // Vault hook
        if (!getPlugin().getVault().isPresent()) {
            // Vault is required
            logError("Vault is required - disabling Bank - please install the Vault plugin");
            this.setState(State.DISABLED);
            return;
        } else {
            // Get economy
            vault = getPlugin().getVault().get();
        }
        saveDefaultConfig();
        settings = config.loadConfigObject();
        if (settings == null) {
            // Settings did not load correctly. Disable.
            logError("Settings did not load correctly - disabling Bank - please check config.yml");
            this.setState(State.DISABLED);
            return;
        }
        config.saveConfigObject(settings);
        // Bank Manager
        bankManager = new BankManager(this);
        bankManager.loadBalances().thenRun(() -> bankManager.startInterest());
        PhManager placeholderManager = new PhManager(this, bankManager);
        // Register commands with GameModes
        getPlugin().getAddonsManager().getGameModeAddons().stream()
        .filter(gm -> settings.getGameModes().stream().anyMatch(gm.getDescription().getName()::equalsIgnoreCase))
        .forEach(gm ->  {
            // Register command
            gm.getPlayerCommand().ifPresent(playerCmd -> new UserCommand(this, playerCmd, settings.getUserCommand()));
            gm.getAdminCommand().ifPresent(adminCmd -> new AdminCommand(this, adminCmd, settings.getAdminCommand()));
            // Register placeholders
            if (!placeholderManager.registerPlaceholders(gm)) {
                this.logError("Could not register placeholders because there is no PlaceholderManager");
            }
            // Log
            this.log("Hooking into " + gm.getDescription().getName());
        });
    }

    @Override
    public void onDisable() {
        // Do nothing
    }

    /**
     * @return the settings
     */
    public @Nullable Settings getSettings() {
        return settings;
    }

    /**
     * @return the vault
     */
    public VaultHook getVault() {
        return vault;
    }

    /**
     * @return the bankManager
     */
    public BankManager getBankManager() {
        return bankManager;
    }

}
