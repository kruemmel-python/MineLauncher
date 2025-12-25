package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;

public class VoiceChatManager {
    private final RPGPlugin plugin;
    private final Map<UUID, String> channels = new HashMap<>();
    private Object voiceApi;
    private Method setPlayerGroup;
    private Method createGroup;

    public VoiceChatManager(RPGPlugin plugin) {
        this.plugin = plugin;
        tryInitApi();
    }

    public void joinParty(Player player) {
        Optional<com.example.rpg.model.Party> party = plugin.partyManager().getParty(player.getUniqueId());
        if (party.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Party."));
            return;
        }
        String channel = "party-" + party.get().leader().toString();
        joinChannel(player, channel, "Party");
    }

    public void joinGuild(Player player) {
        String guildId = plugin.playerDataManager().getProfile(player).guildId();
        if (guildId == null) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        String channel = "guild-" + guildId;
        joinChannel(player, channel, "Gilde");
    }

    public void leave(Player player) {
        channels.remove(player.getUniqueId());
        if (!setGroup(player, null, "Lobby")) {
            player.sendMessage(Text.mm("<yellow>Sprachkanal verlassen."));
        }
    }

    private void joinChannel(Player player, String channel, String label) {
        channels.put(player.getUniqueId(), channel);
        if (!setGroup(player, channel, label)) {
            player.sendMessage(Text.mm("<green>Sprachchat (" + label + ") aktiviert."));
            player.sendMessage(Text.mm("<gray>Installiere Simple Voice Chat für Mikrofon-Unterstützung."));
        }
    }

    private boolean setGroup(Player player, String groupId, String label) {
        if (voiceApi == null || setPlayerGroup == null || createGroup == null) {
            return false;
        }
        try {
            UUID uuid = groupId != null ? UUID.nameUUIDFromBytes(groupId.getBytes()) : null;
            Object group = null;
            if (groupId != null) {
                group = createGroup.invoke(voiceApi, uuid, label + " " + groupId);
            }
            setPlayerGroup.invoke(voiceApi, player.getUniqueId(), group);
            player.sendMessage(Text.mm("<green>Sprachchat (" + label + ") aktiviert."));
            return true;
        } catch (ReflectiveOperationException e) {
            plugin.getLogger().warning("Voice chat API error: " + e.getMessage());
            return false;
        }
    }

    private void tryInitApi() {
        try {
            Class<?> apiClass = Class.forName("de.maxhenkel.voicechat.api.VoicechatServerApi");
            Method getInstance = apiClass.getMethod("getInstance");
            voiceApi = getInstance.invoke(null);
            createGroup = apiClass.getMethod("createGroup", UUID.class, String.class);
            setPlayerGroup = apiClass.getMethod("setPlayerGroup", UUID.class, Object.class);
        } catch (ReflectiveOperationException e) {
            voiceApi = null;
        }
    }
}
