package de.yourname.rpg.zone;

import de.yourname.rpg.util.PdcKeys;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class EditorWandItem {
    private final PdcKeys keys;

    public EditorWandItem(PdcKeys keys) {
        this.keys = keys;
    }

    public ItemStack create() {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6RPG-Editor-Wand");
            meta.getPersistentDataContainer().set(keys.editorWand(), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }
}
