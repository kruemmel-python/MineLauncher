package com.example.rpg.manager;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class GuildManager {
    private final RPGPlugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, String> guildByMember = new HashMap<>();
    private final Map<UUID, String> pendingInvites = new HashMap<>();

    public GuildManager(RPGPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "guilds.yml");
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Optional<Guild> guildById(String id) {
        return Optional.ofNullable(guilds.get(id));
    }

    public Optional<Guild> guildFor(UUID member) {
        String id = guildByMember.get(member);
        return id == null ? Optional.empty() : guildById(id);
    }

    public Location hallLocation(Guild guild) {
        if (guild.hallWorld() == null) {
            return null;
        }
        var world = plugin.getServer().getWorld(guild.hallWorld());
        if (world == null) {
            return null;
        }
        return new Location(world, guild.hallX(), guild.hallY(), guild.hallZ());
    }

    public boolean isMember(UUID member) {
        return guildByMember.containsKey(member);
    }

    public void createGuild(String id, String name, Player leader) {
        Guild guild = new Guild(id);
        guild.setName(name);
        guild.setLeader(leader.getUniqueId());
        guild.members().put(leader.getUniqueId(), GuildMemberRole.LEADER);
        guilds.put(id, guild);
        guildByMember.put(leader.getUniqueId(), id);
        PlayerProfile profile = plugin.playerDataManager().getProfile(leader);
        profile.setGuildId(id);
        saveGuild(guild);
    }

    public void disbandGuild(Guild guild) {
        for (UUID member : guild.members().keySet()) {
            guildByMember.remove(member);
            PlayerProfile profile = plugin.playerDataManager().getProfile(member);
            profile.setGuildId(null);
        }
        guilds.remove(guild.id());
        config.set(guild.id(), null);
        save();
    }

    public void invite(UUID target, String guildId) {
        pendingInvites.put(target, guildId);
    }

    public Optional<Guild> acceptInvite(UUID playerId) {
        String guildId = pendingInvites.remove(playerId);
        if (guildId == null) {
            return Optional.empty();
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return Optional.empty();
        }
        guild.members().put(playerId, GuildMemberRole.MEMBER);
        guildByMember.put(playerId, guildId);
        PlayerProfile profile = plugin.playerDataManager().getProfile(playerId);
        profile.setGuildId(guildId);
        saveGuild(guild);
        return Optional.of(guild);
    }

    public void leaveGuild(UUID member) {
        String guildId = guildByMember.remove(member);
        if (guildId == null) {
            return;
        }
        Guild guild = guilds.get(guildId);
        if (guild == null) {
            return;
        }
        if (guild.leader() != null && guild.leader().equals(member)) {
            disbandGuild(guild);
            return;
        }
        guild.members().remove(member);
        PlayerProfile profile = plugin.playerDataManager().getProfile(member);
        profile.setGuildId(null);
        saveGuild(guild);
    }

    public void setRole(Guild guild, UUID member, GuildMemberRole role) {
        guild.members().put(member, role);
        saveGuild(guild);
    }

    public void deposit(Guild guild, int amount) {
        guild.setBankGold(guild.bankGold() + amount);
        saveGuild(guild);
    }

    public boolean withdraw(Guild guild, int amount) {
        if (guild.bankGold() < amount) {
            return false;
        }
        guild.setBankGold(guild.bankGold() - amount);
        saveGuild(guild);
        return true;
    }

    public void saveAll() {
        for (Guild guild : guilds.values()) {
            saveGuild(guild);
        }
        save();
    }

    private void load() {
        guilds.clear();
        guildByMember.clear();
        for (String id : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            Guild guild = new Guild(id);
            guild.setName(section.getString("name", id));
            String leader = section.getString("leader", null);
            if (leader != null) {
                guild.setLeader(UUID.fromString(leader));
            }
            guild.setBankGold(section.getInt("bankGold", 0));
            guild.setHall(section.getString("hall.world", null),
                section.getDouble("hall.x"), section.getDouble("hall.y"), section.getDouble("hall.z"));
            ConfigurationSection upgrades = section.getConfigurationSection("hall.upgrades");
            if (upgrades != null) {
                for (String key : upgrades.getKeys(false)) {
                    guild.hallUpgrades().put(key, upgrades.getInt(key, 0));
                }
            }
            ConfigurationSection members = section.getConfigurationSection("members");
            if (members != null) {
                for (String uuid : members.getKeys(false)) {
                    try {
                        GuildMemberRole role = GuildMemberRole.valueOf(members.getString(uuid, "MEMBER"));
                        UUID memberId = UUID.fromString(uuid);
                        guild.members().put(memberId, role);
                        guildByMember.put(memberId, id);
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            ConfigurationSection quests = section.getConfigurationSection("quests");
            if (quests != null) {
                for (String questId : quests.getKeys(false)) {
                    ConfigurationSection questSection = quests.getConfigurationSection(questId);
                    if (questSection == null) {
                        continue;
                    }
                    GuildQuest quest = new GuildQuest(questId);
                    quest.setName(questSection.getString("name", questId));
                    quest.setDescription(questSection.getString("description", ""));
                    quest.setGoal(questSection.getInt("goal", 1));
                    quest.setProgress(questSection.getInt("progress", 0));
                    quest.setCompleted(questSection.getBoolean("completed", false));
                    guild.quests().put(questId, quest);
                }
            }
            guilds.put(id, guild);
        }
    }

    private void saveGuild(Guild guild) {
        ConfigurationSection section = config.createSection(guild.id());
        section.set("name", guild.name());
        section.set("leader", guild.leader() != null ? guild.leader().toString() : null);
        section.set("bankGold", guild.bankGold());
        section.set("hall.world", guild.hallWorld());
        section.set("hall.x", guild.hallX());
        section.set("hall.y", guild.hallY());
        section.set("hall.z", guild.hallZ());
        ConfigurationSection upgrades = section.createSection("hall.upgrades");
        for (Map.Entry<String, Integer> entry : guild.hallUpgrades().entrySet()) {
            upgrades.set(entry.getKey(), entry.getValue());
        }
        ConfigurationSection members = section.createSection("members");
        for (Map.Entry<UUID, GuildMemberRole> entry : guild.members().entrySet()) {
            members.set(entry.getKey().toString(), entry.getValue().name());
        }
        ConfigurationSection quests = section.createSection("quests");
        for (GuildQuest quest : guild.quests().values()) {
            ConfigurationSection questSection = quests.createSection(quest.id());
            questSection.set("name", quest.name());
            questSection.set("description", quest.description());
            questSection.set("goal", quest.goal());
            questSection.set("progress", quest.progress());
            questSection.set("completed", quest.completed());
        }
        save();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save guilds.yml: " + e.getMessage());
        }
    }
}
