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

    @ConfigComment("This is how many ranks will be registered with the placeholder API.")
    @ConfigComment("There are two placeholders per rank:")
    @ConfigComment("%Bank_[gamemode]_top_name_1% with island level: %Bank_[gamemode]_top_value_1%")
    @ConfigComment("[gamemode] is bskyblock, acidisland, etc.")
    @ConfigEntry(path = "bank.placeholders.number-of-ranks")
    private int ranksNumber = 10;

    @ConfigComment("The annual interest rate for accounts. If zero or less, interest will not be paid.")
    private int interestRate = 10;

    @ConfigComment("Period that interest is compounded in days. Default is 1 day.")
    @ConfigComment("Interest calculations are done when the server starts or when the player logs in.")
    private float compoundPeriod = 1;

    @ConfigComment("Cooldown time for user withdrawl and deposit commands. This should be set long enough")
    @ConfigComment("so that database writes can be made in time. Default is 60 seconds.")
    private int cooldown = 60;

    @ConfigEntry(path = "bank.sendAlert")
    @ConfigComment("Should other members of the island get a message when someone deposits/withdraws")
    @ConfigComment("from the bank?")
    private boolean sendBankAlert = true;

    @ConfigComment("Shorthand units")
    @ConfigEntry(path = "units.kilo")
    private String kilo = "k";
    @ConfigEntry(path = "units.mega")
    private String mega = "M";
    @ConfigEntry(path = "units.giga")
    private String giga = "G";
    @ConfigEntry(path = "units.tera")
    private String tera = "T";

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

    /**
     * @return the ranksNumber
     */
    public int getRanksNumber() {
        return ranksNumber;
    }

    /**
     * @param ranksNumber the ranksNumber to set
     */
    public void setRanksNumber(int ranksNumber) {
        this.ranksNumber = ranksNumber;
    }

    /**
     * Interest rate is a yearly percentage.
     * @return the yearly interestRate
     */
    public int getInterestRate() {
        return interestRate;
    }

    /**
     * @param interestRate the interestRate to set
     */
    public void setInterestRate(int interestRate) {
        this.interestRate = interestRate;
    }

    /**
     * @return the compoundPeriod in ticks
     */
    public long getCompoundPeriodInTicks() {
        // Make the period a minimum of 1 minute long
        return Math.max(1200L, (long) (compoundPeriod * 20 * 24 * 60 * 60));
    }

    /**
     * @return compound period in days
     */
    public float getCompoundPeriod() {
        return compoundPeriod;
    }

    /**
     * @return the compound periods per year
     */
    public long getCompoundPeriodsPerYear() {
        return (long) (compoundPeriod * 365);
    }

    /**
     * @param compoundPeriod the compoundPeriod to set in hours
     */
    public void setCompoundPeriod(float compoundPeriod) {
        this.compoundPeriod = compoundPeriod;
    }

    /**
     * @return the compound period in ms
     */
    public long getCompoundPeriodInMs() {
        return (long) (compoundPeriod * 24 * 60 * 60 * 1000);
    }

    /**
     * @return the sendBankAlert
     */
    public boolean isSendBankAlert() {
        return sendBankAlert;
    }

    /**
     * @param sendBankAlert the sendBankAlert to set
     */
    public void setSendBankAlert(boolean sendBankAlert) {
        this.sendBankAlert = sendBankAlert;
    }

    /**
     * @return the cooldown
     */
    public int getCooldown() {
        return cooldown;
    }

    /**
     * @param cooldown the cooldown to set
     */
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    /**
     * @return the kilo
     */
    public String getKilo() {
        return kilo;
    }

    /**
     * @param kilo the kilo to set
     */
    public void setKilo(String kilo) {
        this.kilo = kilo;
    }

    /**
     * @return the mega
     */
    public String getMega() {
        return mega;
    }

    /**
     * @param mega the mega to set
     */
    public void setMega(String mega) {
        this.mega = mega;
    }

    /**
     * @return the giga
     */
    public String getGiga() {
        return giga;
    }

    /**
     * @param giga the giga to set
     */
    public void setGiga(String giga) {
        this.giga = giga;
    }

    /**
     * @return the tera
     */
    public String getTera() {
        return tera;
    }

    /**
     * @param tera the tera to set
     */
    public void setTera(String tera) {
        this.tera = tera;
    }
}
