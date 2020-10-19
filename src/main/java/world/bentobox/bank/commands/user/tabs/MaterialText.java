package world.bentobox.bank.commands.user.tabs;

import org.bukkit.Material;

public class MaterialText {
    Material material;
    String text;
    /**
     * @param material - material
     * @param text - string
     */
    public MaterialText(Material m, String t) {
        super();
        this.material = m;
        this.text = t;
    }
}