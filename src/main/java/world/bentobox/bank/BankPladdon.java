package world.bentobox.bank;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 * @author tastybento
 *
 */
public class BankPladdon extends Pladdon {
    private Addon addon;

    @Override
    public Addon getAddon() {
        if (addon == null) {
            addon = new Bank();
        }
        return addon;
    }
}
