package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RPGCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openPlayerMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "skill" -> handleSkill(player, args);
            case "quest" -> handleQuest(player, args);
            case "respec" -> handleRespec(player);
            case "class" -> handleClass(player, args);
            case "bind" -> handleBind(player, args);
            case "money" -> handleMoney(player);
            case "pay" -> handlePay(player, args);
            case "profession" -> handleProfession(player, args);
            case "skilltree" -> plugin.skillTreeGui().open(player);
            case "enchant" -> plugin.guiManager().openEnchanting(player, null);
            case "combatlog" -> handleCombatLog(player, args);
            case "event" -> handleEvent(player, args);
            case "order" -> handleOrder(player, args);
            case "home" -> handleHome(player, args);
            case "faction" -> handleFaction(player);
            default -> player.sendMessage(Text.mm("<gray>/rpg <skill|quest|respec|class|bind|money|pay|profession|skilltree|enchant|combatlog|event|order|home|faction>"));
        }
        return true;
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg skill <id>"));
            return;
        }
        String skillId = args[1].toLowerCase();
        plugin.useSkill(player, skillId);
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg quest <accept|abandon|list>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> plugin.guiManager().openQuestList(player);
            case "abandon" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest abandon <id>"));
                    return;
                }
                String questId = args[2];
                profile.activeQuests().remove(questId);
                player.sendMessage(Text.mm("<yellow>Quest abgebrochen: " + questId));
            }
            case "complete" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest complete <id>"));
                    return;
                }
                String questId = args[2];
                var quest = plugin.questManager().getQuest(questId);
                var progress = profile.activeQuests().get(questId);
                if (quest == null || progress == null) {
                    player.sendMessage(Text.mm("<red>Quest nicht aktiv."));
                    return;
                }
                if (!plugin.completeQuestIfReady(player, quest, progress)) {
                    player.sendMessage(Text.mm("<yellow>Quest noch nicht abgeschlossen."));
                }
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg quest <list|abandon|complete>"));
        }
    }

    private void handleRespec(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.learnedSkills().clear();
        profile.setSkillPoints(profile.level() * 2);
        profile.stats().replaceAll((stat, value) -> 5);
        profile.applyAttributes(player, plugin.itemStatManager(), plugin.classManager());
        player.sendMessage(Text.mm("<green>Respec durchgeführt. Skillpunkte zurückgesetzt."));
    }

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            player.sendMessage(Text.mm("<yellow>Klassen: " + plugin.classManager().classes().keySet()));
            return;
        }
        if (args[1].equalsIgnoreCase("choose")) {
            if (args.length < 3) {
                player.sendMessage(Text.mm("<gray>/rpg class choose <id>"));
                return;
            }
            String id = args[2];
            ClassDefinition definition = plugin.classManager().getClass(id);
            if (definition == null) {
                player.sendMessage(Text.mm("<red>Unbekannte Klasse."));
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            profile.setClassId(id);
            for (String skill : definition.startSkills()) {
                profile.learnedSkills().put(skill, 1);
            }
            profile.applyAttributes(player, plugin.itemStatManager(), plugin.classManager());
            player.sendMessage(Text.mm("<green>Klasse gewählt: " + definition.name()));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
    }

    private void handleBind(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg bind <slot 1-9> <skillId|Skillname>"));
            return;
        }
        Integer slot;
        try {
            slot = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        if (slot < 1 || slot > 9) {
            player.sendMessage(Text.mm("<red>Slot muss 1-9 sein."));
            return;
        }
        String input = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        Skill skill = resolveSkillByIdOrName(input);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Unbekannter Skill: " + input));
            return;
        }
        String skillId = skill.id().toLowerCase();
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            player.sendMessage(Text.mm("<red>Skill nicht gelernt."));
            return;
        }
        plugin.skillHotbarManager().bindSkill(profile, slot, skillId);
        player.sendMessage(Text.mm("<green>Skill gebunden: Slot " + slot + " -> " + skill.name()));
    }

    private Skill resolveSkillByIdOrName(String input) {
        String normalized = input.trim().toLowerCase();
        Skill skill = plugin.skillManager().getSkill(normalized);
        if (skill != null) {
            return skill;
        }
        for (Skill entry : plugin.skillManager().skills().values()) {
            if (entry.name() != null && entry.name().trim().equalsIgnoreCase(input.trim())) {
                return entry;
            }
        }
        return null;
    }

    private void handleMoney(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        player.sendMessage(Text.mm("<gold>Gold: <white>" + profile.gold()));
    }

    private void handlePay(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg pay <player> <amount>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        Integer amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return;
        }
        PlayerProfile senderProfile = plugin.playerDataManager().getProfile(player);
        PlayerProfile targetProfile = plugin.playerDataManager().getProfile(target);
        if (senderProfile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        senderProfile.setGold(senderProfile.gold() - amount);
        targetProfile.setGold(targetProfile.gold() + amount);
        player.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold an " + target.getName() + " gesendet."));
        target.sendMessage(Text.mm("<green>Du hast <gold>" + amount + "</gold> Gold von " + player.getName() + " erhalten."));
    }

    private void handleProfession(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> {
                if (profile.professions().isEmpty()) {
                    player.sendMessage(Text.mm("<yellow>Keine Berufe freigeschaltet."));
                    return;
                }
                String summary = profile.professions().entrySet().stream()
                    .filter(entry -> entry.getKey().endsWith("_level"))
                    .map(entry -> entry.getKey().replace("_level", "") + ": " + entry.getValue())
                    .collect(java.util.stream.Collectors.joining(", "));
                player.sendMessage(Text.mm("<gold>Berufe: <white>" + summary));
            }
            case "set" -> {
                if (args.length < 4) {
                    player.sendMessage(Text.mm("<gray>/rpg profession set <name> <level>"));
                    return;
                }
                Integer level;
                try {
                    level = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    player.sendMessage(Text.mm("<red>Level ungültig."));
                    return;
                }
                plugin.professionManager().setLevel(profile, args[2].toLowerCase(), level);
                player.sendMessage(Text.mm("<green>Beruf gesetzt."));
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg profession <list|set>"));
        }
    }

    private void handleCombatLog(Player player, String[] args) {
        if (args.length < 2) {
            boolean enabled = plugin.toggleCombatLog(player.getUniqueId());
            player.sendMessage(Text.mm(enabled ? "<green>Kampflog aktiviert." : "<red>Kampflog deaktiviert."));
            return;
        }
        String value = args[1].toLowerCase();
        if ("on".equals(value) || "true".equals(value)) {
            plugin.setCombatLog(player.getUniqueId(), true);
            player.sendMessage(Text.mm("<green>Kampflog aktiviert."));
            return;
        }
        if ("off".equals(value) || "false".equals(value)) {
            plugin.setCombatLog(player.getUniqueId(), false);
            player.sendMessage(Text.mm("<red>Kampflog deaktiviert."));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg combatlog <on|off>"));
    }

    private void handleEvent(Player player, String[] args) {
        if (args.length < 2 || "list".equalsIgnoreCase(args[1])) {
            player.sendMessage(Text.mm("<gold>Aktive Events:"));
            boolean any = false;
            for (var event : plugin.worldEventManager().events().values()) {
                if (!event.active()) {
                    continue;
                }
                any = true;
                String zone = event.zoneId() != null ? event.zoneId() : "global";
                player.sendMessage(Text.mm("<gray>- <white>" + event.name() + " <gray>(Zone: " + zone + ")"));
            }
            if (!any) {
                player.sendMessage(Text.mm("<yellow>Keine aktiven Events."));
            }
            return;
        }
        if ("status".equalsIgnoreCase(args[1]) && args.length >= 3) {
            var event = plugin.worldEventManager().getEvent(args[2]);
            if (event == null) {
                player.sendMessage(Text.mm("<red>Event nicht gefunden."));
                return;
            }
            player.sendMessage(Text.mm("<gold>Event: <white>" + event.name()));
            for (int i = 0; i < event.steps().size(); i++) {
                var step = event.steps().get(i);
                int current = event.progress().getOrDefault(i, 0);
                player.sendMessage(Text.mm("<gray>Step " + (i + 1) + ": " + step.type()
                    + " " + step.target() + " <white>" + current + "/" + step.amount()));
            }
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg event <list|status <id>>"));
    }

    private void handleOrder(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg order <list|create|fulfill>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list" -> {
                if (plugin.craftingOrderManager().orders().isEmpty()) {
                    player.sendMessage(Text.mm("<yellow>Keine Aufträge."));
                    return;
                }
                player.sendMessage(Text.mm("<gold>Crafting-Aufträge:"));
                for (var order : plugin.craftingOrderManager().orders().values()) {
                    player.sendMessage(Text.mm("<gray>" + order.id() + ": <white>" + order.amount() + "x "
                        + order.material() + " <gold>(" + order.rewardGold() + " Gold)"));
                }
            }
            case "create" -> createOrder(player, args);
            case "fulfill" -> fulfillOrder(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpg order <list|create|fulfill>"));
        }
    }

    private void createOrder(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(Text.mm("<gray>/rpg order create <material> <amount> <reward>"));
            return;
        }
        String material = args[2].toUpperCase();
        if (org.bukkit.Material.matchMaterial(material) == null) {
            player.sendMessage(Text.mm("<red>Material ungültig."));
            return;
        }
        Integer amount = parseAmount(player, args[3]);
        Integer reward = parseAmount(player, args[4]);
        if (amount == null || reward == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.gold() < reward) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        String id = "order_" + (plugin.craftingOrderManager().orders().size() + 1);
        var order = new com.example.rpg.model.CraftingOrder(id);
        order.setRequester(player.getUniqueId());
        order.setMaterial(material);
        order.setAmount(amount);
        order.setRewardGold(reward);
        plugin.craftingOrderManager().orders().put(id, order);
        plugin.craftingOrderManager().saveOrder(order);
        profile.setGold(profile.gold() - reward);
        player.sendMessage(Text.mm("<green>Auftrag erstellt: " + id));
    }

    private void fulfillOrder(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg order fulfill <id>"));
            return;
        }
        var order = plugin.craftingOrderManager().getOrder(args[2]);
        if (order == null) {
            player.sendMessage(Text.mm("<red>Auftrag nicht gefunden."));
            return;
        }
        org.bukkit.Material material = org.bukkit.Material.matchMaterial(order.material());
        if (material == null) {
            player.sendMessage(Text.mm("<red>Ungültiges Material."));
            return;
        }
        int total = countMaterial(player, material);
        if (total < order.amount()) {
            player.sendMessage(Text.mm("<red>Nicht genug Materialien."));
            return;
        }
        removeMaterial(player, material, order.amount());
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.setGold(profile.gold() + order.rewardGold());
        plugin.craftingOrderManager().removeOrder(order.id());
        player.sendMessage(Text.mm("<green>Auftrag erfüllt! +" + order.rewardGold() + " Gold."));
    }

    private void handleHome(Player player, String[] args) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg home <set|go|upgrade>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "set" -> {
                var loc = player.getLocation();
                profile.setHome(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                player.setRespawnLocation(loc, true);
                player.sendMessage(Text.mm("<green>Home gesetzt."));
            }
            case "go" -> {
                if (profile.homeWorld() == null) {
                    player.sendMessage(Text.mm("<red>Kein Home gesetzt."));
                    return;
                }
                var world = player.getServer().getWorld(profile.homeWorld());
                if (world == null) {
                    player.sendMessage(Text.mm("<red>Home-Welt nicht verfügbar."));
                    return;
                }
                player.teleport(new org.bukkit.Location(world, profile.homeX(), profile.homeY(), profile.homeZ()));
                player.sendMessage(Text.mm("<green>Teleportiert."));
            }
            case "upgrade" -> upgradeHome(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpg home <set|go|upgrade>"));
        }
    }

    private void upgradeHome(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/rpg home upgrade <craft|teleport|buff>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        String type = args[2].toLowerCase();
        int current = profile.housingUpgrades().getOrDefault(type, 0);
        int cost = switch (type) {
            case "craft" -> 200 + (current * 150);
            case "teleport" -> 300 + (current * 200);
            case "buff" -> 400 + (current * 250);
            default -> -1;
        };
        if (cost < 0) {
            player.sendMessage(Text.mm("<red>Unbekanntes Upgrade."));
            return;
        }
        if (profile.gold() < cost) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold. Benötigt: " + cost));
            return;
        }
        profile.setGold(profile.gold() - cost);
        profile.housingUpgrades().put(type, current + 1);
        player.sendMessage(Text.mm("<green>Upgrade verbessert: " + type + " (Stufe " + (current + 1) + ")"));
    }

    private void handleFaction(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.factionRep().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Fraktionswerte."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Fraktionsruf:"));
        for (var entry : profile.factionRep().entrySet()) {
            var faction = plugin.factionManager().getFaction(entry.getKey());
            String name = faction != null ? faction.name() : entry.getKey();
            player.sendMessage(Text.mm("<gray>" + name + ": <white>" + entry.getValue()));
        }
    }

    private Integer parseAmount(Player player, String input) {
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            player.sendMessage(Text.mm("<red>Betrag ungültig."));
            return null;
        }
        if (amount <= 0) {
            player.sendMessage(Text.mm("<red>Betrag muss > 0 sein."));
            return null;
        }
        return amount;
    }

    private int countMaterial(Player player, org.bukkit.Material material) {
        int total = 0;
        for (var stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    private void removeMaterial(Player player, org.bukkit.Material material, int amount) {
        int remaining = amount;
        var contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            var stack = contents[i];
            if (stack == null || stack.getType() != material) {
                continue;
            }
            int take = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
            remaining -= take;
            if (remaining <= 0) {
                break;
            }
        }
        player.getInventory().setContents(contents);
    }
}
