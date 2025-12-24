package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.LootEntry;
import com.example.rpg.model.LootTable;
import com.example.rpg.model.Npc;
import com.example.rpg.model.NpcRole;
import com.example.rpg.model.Quest;
import com.example.rpg.model.QuestStep;
import com.example.rpg.model.QuestStepType;
import com.example.rpg.model.Rarity;
import com.example.rpg.model.Zone;
import com.example.rpg.util.ItemBuilder;
import com.example.rpg.util.Text;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class RPGAdminCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGAdminCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (!player.hasPermission("rpg.admin")) {
            player.sendMessage(Text.mm("<red>Keine Rechte."));
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openAdminMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "wand" -> giveWand(player);
            case "zone" -> handleZone(player, args);
            case "npc" -> handleNpc(player, args);
            case "quest" -> handleQuest(player, args);
            case "loot" -> handleLoot(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin <wand|zone|npc|quest|loot>"));
        }
        return true;
    }

    private void giveWand(Player player) {
        ItemStack wand = new ItemBuilder(Material.STICK)
            .name(Text.mm("<yellow>Editor Wand"))
            .loreLine(Text.mm("<gray>Links: Pos1, Rechts: Pos2"))
            .build();
        var meta = wand.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.wandKey(), PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        player.getInventory().addItem(wand);
        player.sendMessage(Text.mm("<green>Editor Wand erhalten."));
    }

    private void handleZone(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createZone(player, args);
            case "setlevel" -> setZoneLevel(player, args);
            case "setmod" -> setZoneModifiers(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin zone <create|setlevel|setmod>"));
        }
    }

    private void createZone(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone create <id>"));
            return;
        }
        Location pos1 = readPosition(player, "pos1");
        Location pos2 = readPosition(player, "pos2");
        if (pos1 == null || pos2 == null) {
            player.sendMessage(Text.mm("<red>Setze Pos1/Pos2 mit der Wand."));
            return;
        }
        String id = args[2];
        Zone zone = new Zone(id);
        zone.setName(id);
        zone.setWorld(pos1.getWorld().getName());
        zone.setBounds(pos1, pos2);
        plugin.zoneManager().zones().put(id, zone);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone erstellt: " + id);
        player.sendMessage(Text.mm("<green>Zone erstellt: " + id));
    }

    private void setZoneLevel(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setlevel <id> <min> <max>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Integer min = parseInt(args[3]);
        Integer max = parseInt(args[4]);
        if (min == null || max == null || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Ungültiger Levelbereich. Beispiel: <white>/rpgadmin zone setlevel <id> 1 30</white>"));
            return;
        }
        zone.setMinLevel(min);
        zone.setMaxLevel(max);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Level gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Level aktualisiert."));
    }

    private void setZoneModifiers(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpgadmin zone setmod <id> <slow> <damage>"));
            return;
        }
        Zone zone = plugin.zoneManager().getZone(args[2]);
        if (zone == null) {
            player.sendMessage(Text.mm("<red>Zone nicht gefunden."));
            return;
        }
        Double slow = parseDouble(args[3]);
        Double dmg = parseDouble(args[4]);
        if (slow == null || dmg == null || slow <= 0.0 || dmg <= 0.0) {
            player.sendMessage(Text.mm("<red>Ungültige Werte. Beispiel: <white>/rpgadmin zone setmod <id> 0.8 1.2</white>"));
            return;
        }
        zone.setSlowMultiplier(slow);
        zone.setDamageMultiplier(dmg);
        plugin.zoneManager().saveZone(zone);
        plugin.auditLog().log(player, "Zone Modifikatoren gesetzt: " + zone.id());
        player.sendMessage(Text.mm("<green>Zone Modifikatoren aktualisiert."));
    }

    private void handleNpc(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createNpc(player, args);
            case "dialog" -> setNpcDialog(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin npc <create|dialog>"));
        }
    }

    private void createNpc(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc create <id> <role>"));
            return;
        }
        String id = args[2];
        Optional<NpcRole> roleOpt = parseEnum(NpcRole.class, args[3]);
        if (roleOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannte Rolle. Erlaubt: <white>" + java.util.Arrays.toString(NpcRole.values())));
            return;
        }
        NpcRole role = roleOpt.get();
        Npc npc = new Npc(id);
        npc.setName(id);
        npc.setRole(role);
        npc.setLocation(player.getLocation());
        npc.setDialog(List.of("Hallo!", "Ich habe eine Aufgabe für dich."));
        plugin.npcManager().npcs().put(id, npc);
        plugin.npcManager().spawnNpc(npc);
        plugin.npcManager().saveNpc(npc);
        plugin.auditLog().log(player, "NPC erstellt: " + id);
        player.sendMessage(Text.mm("<green>NPC erstellt: " + id));
    }

    private void setNpcDialog(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpgadmin npc dialog <id>"));
            return;
        }
        String id = args[2];
        Npc npc = plugin.npcManager().getNpc(id);
        if (npc == null) {
            player.sendMessage(Text.mm("<red>NPC nicht gefunden."));
            return;
        }
        plugin.promptManager().prompt(player, Text.mm("<yellow>Dialogzeile eingeben:"), input -> {
            npc.setDialog(List.of(input));
            plugin.npcManager().saveNpc(npc);
            plugin.auditLog().log(player, "NPC Dialog gesetzt: " + id);
            player.sendMessage(Text.mm("<green>Dialog gespeichert."));
        });
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createQuest(player, args);
            case "addstep" -> addQuestStep(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin quest <create|addstep>"));
        }
    }

    private void createQuest(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest create <id> <name>"));
            return;
        }
        String id = args[2];
        String name = args[3];
        Quest quest = new Quest(id);
        quest.setName(name);
        quest.setDescription("Neue Quest");
        quest.setRepeatable(false);
        quest.setMinLevel(1);
        quest.setSteps(List.of());
        plugin.questManager().quests().put(id, quest);
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest erstellt: " + id);
        player.sendMessage(Text.mm("<green>Quest erstellt: " + id));
    }

    private void addQuestStep(Player player, String[] args) {
        if (args.length < 6) {
            player.sendMessage(Text.mm("<gray>/rpgadmin quest addstep <id> <type> <target> <amount>"));
            return;
        }
        Quest quest = plugin.questManager().getQuest(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Optional<QuestStepType> typeOpt = parseEnum(QuestStepType.class, args[3]);
        if (typeOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Unbekannter Step-Typ. Erlaubt: <white>" + java.util.Arrays.toString(QuestStepType.values())));
            return;
        }
        QuestStepType type = typeOpt.get();
        String target = args[4];
        Integer amount = parseInt(args[5]);
        if (amount == null || amount < 1) {
            player.sendMessage(Text.mm("<red>Amount muss >= 1 sein.</red>"));
            return;
        }
        quest.steps().add(new QuestStep(type, target, amount));
        plugin.questManager().saveQuest(quest);
        plugin.auditLog().log(player, "Quest Step hinzugefügt: " + quest.id());
        player.sendMessage(Text.mm("<green>Quest Step hinzugefügt."));
    }

    private void handleLoot(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "create" -> createLoot(player, args);
            case "addentry" -> addLootEntry(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpgadmin loot <create|addentry>"));
        }
    }

    private void createLoot(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot create <id> <appliesTo>"));
            return;
        }
        String id = args[2];
        String appliesTo = args[3];
        LootTable table = new LootTable(id);
        table.setAppliesTo(appliesTo);
        plugin.lootManager().tables().put(id, table);
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Table erstellt: " + id);
        player.sendMessage(Text.mm("<green>Loot Table erstellt."));
    }

    private void addLootEntry(Player player, String[] args) {
        if (args.length < 8) {
            player.sendMessage(Text.mm("<gray>/rpgadmin loot addentry <id> <material> <chance> <min> <max> <rarity>"));
            return;
        }
        LootTable table = plugin.lootManager().getTable(args[2]);
        if (table == null) {
            player.sendMessage(Text.mm("<red>Loot Table nicht gefunden."));
            return;
        }
        String material = args[3];
        Material mat = Material.matchMaterial(material.toUpperCase(Locale.ROOT));
        if (mat == null) {
            player.sendMessage(Text.mm("<red>Unbekanntes Material: <white>" + material + "</white>"));
            return;
        }
        Double chance = parseDouble(args[4]);
        Integer min = parseInt(args[5]);
        Integer max = parseInt(args[6]);
        Optional<Rarity> rarityOpt = parseEnum(Rarity.class, args[7]);
        if (chance == null || min == null || max == null || rarityOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Ungültige Parameter. Beispiel: <white>/rpgadmin loot addentry <id> IRON_NUGGET 0.5 1 3 COMMON</white>"));
            return;
        }
        if (chance < 0.0 || chance > 1.0 || min < 1 || max < min) {
            player.sendMessage(Text.mm("<red>Chance 0..1 und min/max gültig setzen.</red>"));
            return;
        }
        Rarity rarity = rarityOpt.get();
        table.entries().add(new LootEntry(mat.name(), chance, min, max, rarity));
        plugin.lootManager().saveTable(table);
        plugin.auditLog().log(player, "Loot Entry hinzugefügt: " + table.id());
        player.sendMessage(Text.mm("<green>Loot Entry hinzugefügt."));
    }

    private Location readPosition(Player player, String key) {
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        String value = player.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(",");
        if (parts.length < 4) {
            return null;
        }
        return new Location(player.getServer().getWorld(parts[0]),
            Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    // -----------------------
    // Parsing-Helper (crash-sicher)
    // -----------------------
    private static Integer parseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double parseDouble(String raw) {
        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static <E extends Enum<E>> Optional<E> parseEnum(Class<E> type, String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String key = raw.trim().toUpperCase(Locale.ROOT);
        try {
            return Optional.of(Enum.valueOf(type, key));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
