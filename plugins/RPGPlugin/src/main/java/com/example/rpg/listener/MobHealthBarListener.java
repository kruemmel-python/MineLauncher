package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Display;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.persistence.PersistentDataType;

public class MobHealthBarListener implements Listener {
    private final RPGPlugin plugin;
    private final Map<UUID, TextDisplay> healthBars = new HashMap<>();

    public MobHealthBarListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!(living instanceof Monster)) {
            return;
        }
        if (isCustomMob(living)) {
            return;
        }
        updateHealthBar(living, Math.max(0, living.getHealth() - event.getFinalDamage()));
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!(living instanceof Monster)) {
            return;
        }
        if (isCustomMob(living)) {
            return;
        }
        updateHealthBar(living, living.getHealth() + event.getAmount());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        removeHealthBar(living);
    }

    private boolean isCustomMob(LivingEntity living) {
        return living.getPersistentDataContainer()
            .has(plugin.customMobListener().mobKey(), PersistentDataType.STRING);
    }

    private void updateHealthBar(LivingEntity living, double health) {
        TextDisplay display = healthBars.get(living.getUniqueId());
        if (display == null || display.isDead()) {
            attachHealthBar(living, health);
            return;
        }
        display.text(Component.text(buildHealthText(living, health)));
    }

    private void attachHealthBar(LivingEntity living, double health) {
        removeHealthBar(living);
        TextDisplay display = living.getWorld().spawn(living.getLocation().add(0, 1.6, 0), TextDisplay.class);
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);
        display.text(Component.text(buildHealthText(living, health)));
        living.addPassenger(display);
        healthBars.put(living.getUniqueId(), display);
    }

    private void removeHealthBar(LivingEntity living) {
        TextDisplay display = healthBars.remove(living.getUniqueId());
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    private String buildHealthText(LivingEntity living, double health) {
        double maxHealth = 20.0;
        if (living.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
            maxHealth = living.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        }
        maxHealth = Math.max(1, maxHealth);
        int bars = 10;
        int filled = (int) Math.round((health / maxHealth) * bars);
        filled = Math.min(bars, Math.max(0, filled));
        int empty = bars - filled;
        StringBuilder bar = new StringBuilder();
        bar.append("§7[§a");
        bar.append("|".repeat(filled));
        bar.append("§c");
        bar.append("|".repeat(empty));
        bar.append("§7]");
        String name = living.getName();
        return name + " " + bar + " §f" + Math.round(health) + "/" + Math.round(maxHealth) + " HP";
    }
}
