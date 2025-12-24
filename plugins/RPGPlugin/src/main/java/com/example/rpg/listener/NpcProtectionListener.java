package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Schutz: NPCs sollen nicht beschädigt, nicht getargetet und nicht "interaktiv kaputt" gemacht werden.
 * (Ohne externe Plugins.)
 */
public class NpcProtectionListener implements Listener {
    private final RPGPlugin plugin;

    public NpcProtectionListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean isNpc(Entity entity) {
        return entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (isNpc(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && isNpc(event.getTarget())) {
            event.setCancelled(true);
        }
    }

    // Intentionally no PlayerInteractAtEntityEvent cancel to avoid breaking normal right-click.
}
