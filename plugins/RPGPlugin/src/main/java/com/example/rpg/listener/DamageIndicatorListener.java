package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class DamageIndicatorListener implements Listener {
    private final RPGPlugin plugin;
    private final Random random = new Random();

    public DamageIndicatorListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getFinalDamage() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        NamedTextColor color = isMagicDamage(event.getCause()) ? NamedTextColor.AQUA : NamedTextColor.RED;
        String text = "-" + Math.round(event.getFinalDamage()) + " ❤";
        spawnIndicator(entity.getLocation(), text, color);
    }

    @EventHandler
    public void onRegain(EntityRegainHealthEvent event) {
        if (event.getAmount() <= 0) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof TextDisplay) {
            return;
        }
        String text = "+" + Math.round(event.getAmount()) + " ❤";
        spawnIndicator(entity.getLocation(), text, NamedTextColor.GREEN);
    }

    private boolean isMagicDamage(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case MAGIC, DRAGON_BREATH, WITHER, POISON -> true;
            default -> false;
        };
    }

    private void spawnIndicator(Location base, String text, NamedTextColor color) {
        Location location = base.clone().add(offset(), 1.2 + offset(), offset());
        TextDisplay display = base.getWorld().spawn(location, TextDisplay.class);
        display.text(Component.text(text, color));
        display.setBillboard(Display.Billboard.CENTER);
        display.setSeeThrough(true);
        display.setShadowed(true);

        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (display.isDead()) {
                task.cancel();
                return;
            }
            display.teleport(display.getLocation().add(0, 0.04, 0));
        }, 0L, 1L);

        plugin.getServer().getScheduler().runTaskLater(plugin, display::remove, 20L);
    }

    private double offset() {
        return (random.nextDouble() - 0.5) * 0.6;
    }
}
