package de.yourname.rpg.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.yourname.rpg.core.PlayerData;
import de.yourname.rpg.quest.Quest;
import de.yourname.rpg.zone.Zone;
import de.yourname.rpg.npc.RpgNpc;
import de.yourname.rpg.loot.LootTable;
import de.yourname.rpg.skill.SkillTree;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class StorageService {
    private final Path dataFolder;
    private final Gson gson;

    private static final Type QUEST_LIST_TYPE = new TypeToken<List<Quest>>() {}.getType();
    private static final Type ZONE_LIST_TYPE = new TypeToken<List<Zone>>() {}.getType();
    private static final Type NPC_LIST_TYPE = new TypeToken<List<RpgNpc>>() {}.getType();
    private static final Type LOOT_LIST_TYPE = new TypeToken<List<LootTable>>() {}.getType();
    private static final Type SKILL_TREE_LIST_TYPE = new TypeToken<List<SkillTree>>() {}.getType();

    public StorageService(Path dataFolder) {
        this.dataFolder = dataFolder;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void ensureFolders() {
        try {
            Files.createDirectories(dataFolder.resolve("playerdata"));
            Files.createDirectories(dataFolder.resolve("quests"));
            Files.createDirectories(dataFolder.resolve("zones"));
            Files.createDirectories(dataFolder.resolve("npcs"));
            Files.createDirectories(dataFolder.resolve("loottables"));
            Files.createDirectories(dataFolder.resolve("skills"));
            Files.createDirectories(dataFolder.resolve("backups"));
        } catch (IOException e) {
            throw new IllegalStateException("Could not create data directories", e);
        }
    }

    public Optional<PlayerData> loadPlayerData(UUID uuid) {
        Path file = dataFolder.resolve("playerdata").resolve(uuid + ".json");
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            return Optional.ofNullable(gson.fromJson(reader, PlayerData.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void savePlayerData(PlayerData data) {
        Path file = dataFolder.resolve("playerdata").resolve(data.getUuid() + ".json");
        writeJson(file, data);
    }

    public List<Quest> loadQuests() {
        return loadList(dataFolder.resolve("quests").resolve("quests.json"), QUEST_LIST_TYPE);
    }

    public void saveQuests(List<Quest> quests) {
        writeJson(dataFolder.resolve("quests").resolve("quests.json"), quests);
    }

    public List<Zone> loadZones() {
        return loadList(dataFolder.resolve("zones").resolve("zones.json"), ZONE_LIST_TYPE);
    }

    public void saveZones(List<Zone> zones) {
        writeJson(dataFolder.resolve("zones").resolve("zones.json"), zones);
    }

    public List<RpgNpc> loadNpcs() {
        return loadList(dataFolder.resolve("npcs").resolve("npcs.json"), NPC_LIST_TYPE);
    }

    public void saveNpcs(List<RpgNpc> npcs) {
        writeJson(dataFolder.resolve("npcs").resolve("npcs.json"), npcs);
    }

    public List<LootTable> loadLootTables() {
        return loadList(dataFolder.resolve("loottables").resolve("loottables.json"), LOOT_LIST_TYPE);
    }

    public void saveLootTables(List<LootTable> tables) {
        writeJson(dataFolder.resolve("loottables").resolve("loottables.json"), tables);
    }

    public List<SkillTree> loadSkillTrees() {
        return loadList(dataFolder.resolve("skills").resolve("skilltrees.json"), SKILL_TREE_LIST_TYPE);
    }

    public void saveSkillTrees(List<SkillTree> trees) {
        writeJson(dataFolder.resolve("skills").resolve("skilltrees.json"), trees);
    }

    private <T> List<T> loadList(Path file, Type type) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            List<T> data = gson.fromJson(reader, type);
            return data == null ? new ArrayList<>() : data;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void writeJson(Path file, Object data) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not write file " + file, e);
        }
    }
}
