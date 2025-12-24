package de.yourname.rpg.util;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;

public class PdcKeys {
    private final Plugin plugin;

    public PdcKeys(Plugin plugin) {
        this.plugin = plugin;
    }

    public NamespacedKey npcId() {
        return new NamespacedKey(plugin, "rpg_npc_id");
    }

    public NamespacedKey itemStats() {
        return new NamespacedKey(plugin, "rpg_item_stats");
    }

    public NamespacedKey editorWand() {
        return new NamespacedKey(plugin, "rpg_editor_wand");
    }
}
