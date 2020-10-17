/**
 *
 */
package world.bentobox.bank;

import java.util.ArrayList;
import java.util.List;

import world.bentobox.bentobox.api.configuration.ConfigComment;
import world.bentobox.bentobox.api.configuration.ConfigEntry;
import world.bentobox.bentobox.api.configuration.ConfigObject;
import world.bentobox.bentobox.api.configuration.StoreAt;

/**
 * @author tastybento
 *
 */
@StoreAt(filename="config.yml", path="addons/Bank") // Explicitly call out what name this should have.
@ConfigComment("Bank Configuration [version]")
@ConfigComment("")
public class Settings implements ConfigObject {
    // General
    @ConfigComment("BentoBox GameModes that can use Bank")
    @ConfigEntry(path = "bank.game-modes")
    private List<String> gameModes = new ArrayList<>();

    @ConfigComment("User command")
    @ConfigEntry(path = "bank.commands.user")
    private String userCommand = "bank";

    @ConfigComment("Admin command")
    @ConfigEntry(path = "bank.commands.admin")
    private String adminCommand = "bank";

    /**
     * @return the gameModes
     */
    public List<String> getGameModes() {
        return gameModes;
    }

    /**
     * @param gameModes the gameModes to set
     */
    public void setGameModes(List<String> gameModes) {
        this.gameModes = gameModes;
    }

    /**
     * @return the userCommand
     */
    public String getUserCommand() {
        return userCommand;
    }

    /**
     * @param userCommand the userCommand to set
     */
    public void setUserCommand(String userCommand) {
        this.userCommand = userCommand;
    }

    /**
     * @return the adminCommand
     */
    public String getAdminCommand() {
        return adminCommand;
    }

    /**
     * @param adminCommand the adminCommand to set
     */
    public void setAdminCommand(String adminCommand) {
        this.adminCommand = adminCommand;
    }


}
