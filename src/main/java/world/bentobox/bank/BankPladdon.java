package world.bentobox.bank;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 * @author tastybento
 *
 */
@Plugin(name="Bank", version="1.0")
@ApiVersion(ApiVersion.Target.v1_16)
public class BankPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new Bank();
    }

}
