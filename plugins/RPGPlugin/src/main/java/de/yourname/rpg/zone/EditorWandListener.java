package de.yourname.rpg.zone;

import de.yourname.rpg.command.RpgAdminCommand;
import de.yourname.rpg.util.PdcKeys;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class EditorWandListener implements Listener {
    private final RpgAdminCommand adminCommand;
    private final PdcKeys keys;

    public EditorWandListener(RpgAdminCommand adminCommand, PdcKeys keys) {
        this.adminCommand = adminCommand;
        this.keys = keys;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getItemMeta() == null) {
            return;
        }
        if (!item.getItemMeta().getPersistentDataContainer().has(keys.editorWand(), PersistentDataType.BYTE)) {
            return;
        }
        Player player = event.getPlayer();
        ZoneSelection selection = adminCommand.getSelection(player);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
            Location location = event.getClickedBlock().getLocation();
            selection.setPos1(new ZonePosition(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            player.sendMessage("§aPos1 gesetzt.");
            event.setCancelled(true);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (player.isSneaking()) {
                if (selection.isComplete()) {
                    player.sendMessage("§6Zone-Auswahl:");
                    player.sendMessage("§ePos1: §7" + format(selection.getPos1()));
                    player.sendMessage("§ePos2: §7" + format(selection.getPos2()));
                } else {
                    player.sendMessage("§7Auswahl unvollständig. Setze Pos1 und Pos2.");
                }
                return;
            }
            Location location = event.getClickedBlock().getLocation();
            selection.setPos2(new ZonePosition(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()));
            player.sendMessage("§aPos2 gesetzt.");
            event.setCancelled(true);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR && player.isSneaking()) {
            selection.reset();
            player.sendMessage("§cSelection zurückgesetzt.");
        }
    }

    private String format(ZonePosition pos) {
        return pos.getWorld() + " (" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
    }
}
