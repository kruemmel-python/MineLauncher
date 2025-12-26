package com.example.rpg.listener;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.util.Text;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class NpcListener implements Listener {
    private final RPGPlugin plugin;

    public NpcListener(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (!entity.getPersistentDataContainer().has(plugin.npcManager().npcKey(), PersistentDataType.STRING)) {
            return;
        }
        String npcId = entity.getPersistentDataContainer().get(plugin.npcManager().npcKey(), PersistentDataType.STRING);
        if (npcId == null) {
            return;
        }
        Npc npc = plugin.npcManager().getNpc(npcId);
        if (npc == null) {
            return;
        }
        Player player = event.getPlayer();
        if (!npc.dialog().isEmpty()) {
            player.sendMessage(Text.mm("<gold>" + npc.name() + ":"));
            for (String line : npc.dialog()) {
                player.sendMessage(Text.mm("<gray>" + line));
            }
        }
        if (npc.role() == NpcRole.QUESTGIVER && npc.questLink() != null) {
            player.sendMessage(Text.mm("<yellow>Quest verfügbar: <white>" + npc.questLink()));
            plugin.guiManager().openQuestList(player);
        }
        if (npc.role() == NpcRole.VENDOR && npc.shopId() != null) {
            var shop = plugin.shopManager().getShop(npc.shopId());
            if (shop == null) {
                player.sendMessage(Text.mm("<red>Shop nicht gefunden."));
                return;
            }
            plugin.guiManager().openShop(player, shop);
        }
    }
}
