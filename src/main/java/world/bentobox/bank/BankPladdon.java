package world.bentobox.bank;


import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.api.addons.Pladdon;

/**
 * @author tastybento
 *
 */
public class BankPladdon extends Pladdon {

    @Override
    public Addon getAddon() {
        return new Bank();
    }

}
