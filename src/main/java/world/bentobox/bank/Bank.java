package world.bentobox.bank;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bank.commands.UserCommand;
import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.hooks.VaultHook;

/**
 * Main class of the Bank addon.
 * @author tastybento
 */
public class Bank extends Addon {

    private final Config<Settings> config;
    private @Nullable Settings settings;
    private final @NonNull List<GameModeAddon> activeGms;
    private VaultHook vault;
    private BankManager bankManager;

    public Bank() {
        config = new Config<>(this, Settings.class);
        activeGms = new ArrayList<>();
    }

    @Override
    public void onEnable() {
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
            // Settings did no load correctly. Disable.
            logError("Settings did not load correctly - disabling Bank - please check config.yml");
            this.setState(State.DISABLED);
            return;
        }
        config.saveConfigObject(settings);
        // Register commands with GameModes
        activeGms.clear();
        getPlugin().getAddonsManager().getGameModeAddons().stream()
        .filter(gm -> settings.getGameModes().stream().anyMatch(gm.getDescription().getName()::equalsIgnoreCase))
        .forEach(gm ->  {
            // Register command
            gm.getPlayerCommand().ifPresent(playerCmd -> new UserCommand(this, playerCmd, settings.getUserCommand(), settings.getUserAliases().split(" ")));
            //gm.getAdminCommand().ifPresent(adminCmd -> new AdminCommand(this, adminCmd, settings.getAdminCommand(), settings.getAdminAliases().split(" ")));
            // Log
            this.log("Hooking into " + gm.getDescription().getName());
            // Store active world
            activeGms.add(gm);
        });
        // Bank Manager
        bankManager = new BankManager(this);
    }

    @Override
    public void onDisable() {

    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
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
