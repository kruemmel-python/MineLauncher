package de.yourname.rpg.npc;

import de.yourname.rpg.quest.QuestService;
import de.yourname.rpg.util.PdcKeys;
import java.util.Optional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class NpcInteractionHandler implements Listener {
    private final NpcService npcService;
    private final QuestService questService;
    private final PdcKeys keys;

    public NpcInteractionHandler(NpcService npcService, QuestService questService, PdcKeys keys) {
        this.npcService = npcService;
        this.questService = questService;
        this.keys = keys;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        String npcId = pdc.get(keys.npcId(), PersistentDataType.STRING);
        if (npcId == null) {
            return;
        }
        Optional<RpgNpc> npc = npcService.getNpc(npcId);
        if (npc.isEmpty()) {
            return;
        }
        Player player = event.getPlayer();
        questService.notifyTalk(player.getUniqueId(), npcId);
        player.sendMessage("§a" + npcId + ": " + npc.get().getDialogId());
    }
}
