package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.SkillSynergy;
import com.example.rpg.skill.SkillEffectConfig;
import com.example.rpg.skill.SkillEffectType;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SkillSynergyManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, SkillSynergy> synergies = new HashMap<>();

    public SkillSynergyManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "skill_synergies.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        if (!file.exists()) {
            seedDefaults();
        }
        load();
    }

    public void onSkillUsed(Player player, String skillId) {
        long now = System.currentTimeMillis();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.setLastSkill(skillId, now);
        for (SkillSynergy synergy : synergies.values()) {
            if (!synergy.skills().contains(skillId)) {
                continue;
            }
            if (!hasRequiredSkills(player, synergy)) {
                continue;
            }
            for (SkillEffectConfig effect : synergy.effects()) {
                plugin.skillEffects().apply(effect, player, profile);
            }
            player.sendMessage(com.example.rpg.util.Text.mm("<aqua>Synergie ausgelöst: " + synergy.id()));
        }
    }

    private boolean hasRequiredSkills(Player player, SkillSynergy synergy) {
        long window = synergy.windowSeconds() * 1000L;
        List<Player> scopePlayers = resolveScope(player, synergy);
        for (String required : synergy.skills()) {
            boolean matched = false;
            for (Player member : scopePlayers) {
                PlayerProfile profile = plugin.playerDataManager().getProfile(member);
                if (required.equalsIgnoreCase(profile.lastSkillId())
                    && (System.currentTimeMillis() - profile.lastSkillTime()) <= window) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private List<Player> resolveScope(Player player, SkillSynergy synergy) {
        List<Player> players = new ArrayList<>();
        players.add(player);
        String scope = synergy.scope() != null ? synergy.scope().toUpperCase() : "PARTY";
        double radius = synergy.radius();
        if ("PARTY".equals(scope)) {
            plugin.partyManager().getParty(player.getUniqueId()).ifPresent(party -> {
                for (var memberId : party.members()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.getWorld().equals(player.getWorld())
                        && member.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                        players.add(member);
                    }
                }
            });
        } else if ("GUILD".equals(scope)) {
            plugin.guildManager().guildFor(player.getUniqueId()).ifPresent(guild -> {
                for (var memberId : guild.members().keySet()) {
                    Player member = plugin.getServer().getPlayer(memberId);
                    if (member != null && member.getWorld().equals(player.getWorld())
                        && member.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                        players.add(member);
                    }
                }
            });
        } else {
            for (Player nearby : player.getWorld().getPlayers()) {
                if (nearby.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
                    players.add(nearby);
                }
            }
        }
        return players;
    }

    private void load() {
        synergies.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            SkillSynergy synergy = new SkillSynergy(id);
            synergy.setSkills(section.getStringList("skills"));
            synergy.setScope(section.getString("scope", "PARTY"));
            synergy.setRadius(section.getDouble("radius", 8));
            synergy.setWindowSeconds(section.getInt("windowSeconds", 6));
            List<Map<?, ?>> rawEffects = section.getMapList("effects");
            for (Map<?, ?> raw : rawEffects) {
                String typeName = String.valueOf(raw.getOrDefault("type", "DAMAGE"));
                SkillEffectType type = SkillEffectType.valueOf(typeName);
                Map<String, Object> params = new HashMap<>();
                Object paramsRaw = raw.get("params");
                if (paramsRaw instanceof Map<?, ?> map) {
                    for (Map.Entry<?, ?> entry : map.entrySet()) {
                        params.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
                synergy.effects().add(new SkillEffectConfig(type, params));
            }
            synergies.put(id, synergy);
        }
    }

    private void seedDefaults() {
        config.set("steam_burst.skills", List.of("frost_bolt", "ember_shot"));
        config.set("steam_burst.scope", "PARTY");
        config.set("steam_burst.radius", 6);
        config.set("steam_burst.windowSeconds", 6);
        Map<String, Object> damageParams = new HashMap<>();
        damageParams.put("amount", 6);
        damageParams.put("radius", 4);
        config.set("steam_burst.effects", List.of(Map.of("type", "DAMAGE", "params", damageParams)));
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save skill_synergies.yml: " + e.getMessage());
        }
    }
}
