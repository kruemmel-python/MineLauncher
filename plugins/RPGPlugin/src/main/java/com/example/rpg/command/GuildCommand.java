package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.Guild;
import com.example.rpg.model.GuildMemberRole;
import com.example.rpg.model.GuildQuest;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.util.Text;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuildCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public GuildCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (label.equalsIgnoreCase("g")) {
            if (args.length == 0) {
                player.sendMessage(Text.mm("<gray>/g <message>"));
                return true;
            }
            String message = join(args, 0);
            guildChat(player, new String[] {"chat", message});
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest|hall>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "create" -> createGuild(player, args);
            case "invite" -> invitePlayer(player, args);
            case "accept" -> acceptInvite(player);
            case "leave" -> leaveGuild(player);
            case "disband" -> disbandGuild(player);
            case "info" -> guildInfo(player);
            case "chat" -> guildChat(player, args);
            case "bank" -> bankCommand(player, args);
            case "quest" -> questCommand(player, args);
            case "hall" -> hallCommand(player, args);
            default -> player.sendMessage(Text.mm("<gray>/guild <create|invite|accept|leave|disband|info|chat|bank|quest|hall>"));
        }
        return true;
    }

    private void createGuild(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild create <id> <name>"));
            return;
        }
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        String id = args[1].toLowerCase();
        String name = join(args, 2);
        if (plugin.guildManager().guildById(id).isPresent()) {
            player.sendMessage(Text.mm("<red>Gilden-ID existiert bereits."));
            return;
        }
        plugin.guildManager().createGuild(id, name, player);
        player.sendMessage(Text.mm("<green>Gilde erstellt: " + name));
    }

    private void invitePlayer(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um einzuladen."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild invite <player>"));
            return;
        }
        Player target = player.getServer().getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(Text.mm("<red>Spieler nicht online."));
            return;
        }
        if (plugin.guildManager().isMember(target.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Spieler ist bereits in einer Gilde."));
            return;
        }
        plugin.guildManager().invite(target.getUniqueId(), guild.id());
        player.sendMessage(Text.mm("<green>Einladung gesendet."));
        target.sendMessage(Text.mm("<yellow>Gildeneinladung von " + guild.name() + ". /guild accept"));
    }

    private void acceptInvite(Player player) {
        if (plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist bereits in einer Gilde."));
            return;
        }
        Optional<Guild> guild = plugin.guildManager().acceptInvite(player.getUniqueId());
        if (guild.isEmpty()) {
            player.sendMessage(Text.mm("<red>Keine Einladung gefunden."));
            return;
        }
        player.sendMessage(Text.mm("<green>Du bist der Gilde beigetreten."));
    }

    private void leaveGuild(Player player) {
        if (!plugin.guildManager().isMember(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        plugin.guildManager().leaveGuild(player.getUniqueId());
        player.sendMessage(Text.mm("<yellow>Gilde verlassen."));
    }

    private void disbandGuild(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (!guild.leader().equals(player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Nur der Gildenleiter kann auflösen."));
            return;
        }
        plugin.guildManager().disbandGuild(guild);
        player.sendMessage(Text.mm("<yellow>Gilde aufgelöst."));
    }

    private void guildInfo(Player player) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        String members = guild.members().keySet().stream()
            .map(uuid -> {
                Player online = player.getServer().getPlayer(uuid);
                return online != null ? online.getName() : uuid.toString().substring(0, 8);
            })
            .collect(Collectors.joining(", "));
        player.sendMessage(Text.mm("<gold>Gilde: <white>" + guild.name()));
        player.sendMessage(Text.mm("<gray>Mitglieder: <white>" + members));
        player.sendMessage(Text.mm("<gray>Gildenbank: <gold>" + guild.bankGold() + "</gold> Gold"));
    }

    private void guildChat(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild chat <message>"));
            return;
        }
        Guild guild = guildOpt.get();
        String message = join(args, 1);
        for (UUID member : guild.members().keySet()) {
            Player target = player.getServer().getPlayer(member);
            if (target != null) {
                target.sendMessage(Text.mm("<aqua>[Gilde] <white>" + player.getName() + ": " + message));
            }
        }
    }

    private void bankCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "balance" -> player.sendMessage(Text.mm("<gold>Gildenbank: " + guild.bankGold() + " Gold"));
            case "deposit" -> depositGuild(player, guild, args);
            case "withdraw" -> withdrawGuild(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild bank <balance|deposit|withdraw>"));
        }
    }

    private void depositGuild(Player player, Guild guild, String[] args) {
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank deposit <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (profile.gold() < amount) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold."));
            return;
        }
        profile.setGold(profile.gold() - amount);
        plugin.guildManager().deposit(guild, amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold eingezahlt."));
    }

    private void withdrawGuild(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte zum Abheben."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild bank withdraw <amount>"));
            return;
        }
        Integer amount = parseAmount(player, args[2]);
        if (amount == null) {
            return;
        }
        if (!plugin.guildManager().withdraw(guild, amount)) {
            player.sendMessage(Text.mm("<red>Gildenbank hat nicht genug Gold."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.setGold(profile.gold() + amount);
        player.sendMessage(Text.mm("<green>" + amount + " Gold abgehoben."));
    }

    private void questCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "list" -> listQuests(player, guild);
            case "create" -> createQuest(player, guild, args);
            case "progress" -> progressQuest(player, guild, args);
            case "complete" -> completeQuest(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild quest <list|create|progress|complete>"));
        }
    }

    private void listQuests(Player player, Guild guild) {
        if (guild.quests().isEmpty()) {
            player.sendMessage(Text.mm("<yellow>Keine Gilden-Quests verfügbar."));
            return;
        }
        player.sendMessage(Text.mm("<gold>Gilden-Quests:</gold>"));
        for (GuildQuest quest : guild.quests().values()) {
            String status = quest.completed() ? "<green>abgeschlossen" : "<yellow>" + quest.progress() + "/" + quest.goal();
            player.sendMessage(Text.mm("<gray>" + quest.id() + " - <white>" + quest.name() + " <gray>(" + status + "<gray>)"));
        }
    }

    private void createQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests zu erstellen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest create <id> <goal> <name>"));
            return;
        }
        String id = args[2].toLowerCase();
        Integer goal = parseAmount(player, args[3]);
        if (goal == null) {
            return;
        }
        if (guild.quests().containsKey(id)) {
            player.sendMessage(Text.mm("<red>Quest-ID existiert bereits."));
            return;
        }
        String name = args.length > 4 ? join(args, 4) : id;
        GuildQuest quest = new GuildQuest(id);
        quest.setName(name);
        quest.setDescription("Gildenquest");
        quest.setGoal(goal);
        quest.setProgress(0);
        quest.setCompleted(false);
        guild.quests().put(id, quest);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Gilden-Quest erstellt."));
    }

    private void progressQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Fortschritt zu setzen."));
            return;
        }
        if (args.length < 4) {
            player.sendMessage(Text.mm("<gray>/guild quest progress <id> <amount>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        Integer amount = parseAmount(player, args[3]);
        if (amount == null) {
            return;
        }
        quest.setProgress(quest.progress() + amount);
        if (quest.progress() >= quest.goal()) {
            quest.setCompleted(true);
        }
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest-Fortschritt aktualisiert."));
    }

    private void completeQuest(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte, um Quests abzuschließen."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild quest complete <id>"));
            return;
        }
        GuildQuest quest = guild.quests().get(args[2]);
        if (quest == null) {
            player.sendMessage(Text.mm("<red>Quest nicht gefunden."));
            return;
        }
        quest.setCompleted(true);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Quest abgeschlossen."));
    }

    private void hallCommand(Player player, String[] args) {
        Optional<Guild> guildOpt = plugin.guildManager().guildFor(player.getUniqueId());
        if (guildOpt.isEmpty()) {
            player.sendMessage(Text.mm("<red>Du bist in keiner Gilde."));
            return;
        }
        Guild guild = guildOpt.get();
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/guild hall <set|go|upgrade>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "set" -> {
                if (!guild.leader().equals(player.getUniqueId())) {
                    player.sendMessage(Text.mm("<red>Nur der Leader kann die Halle setzen."));
                    return;
                }
                var loc = player.getLocation();
                guild.setHall(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ());
                plugin.guildManager().saveAll();
                player.sendMessage(Text.mm("<green>Gildenhalle gesetzt."));
            }
            case "go" -> {
                var hall = plugin.guildManager().hallLocation(guild);
                if (hall == null) {
                    player.sendMessage(Text.mm("<red>Keine Gildenhalle gesetzt."));
                    return;
                }
                if (guild.hallUpgrades().getOrDefault("teleport", 0) < 1) {
                    player.sendMessage(Text.mm("<red>Teleport-Upgrade erforderlich."));
                    return;
                }
                player.teleport(hall);
                player.sendMessage(Text.mm("<green>Zur Gildenhalle teleportiert."));
            }
            case "upgrade" -> upgradeHall(player, guild, args);
            default -> player.sendMessage(Text.mm("<gray>/guild hall <set|go|upgrade>"));
        }
    }

    private void upgradeHall(Player player, Guild guild, String[] args) {
        if (!isOfficerOrLeader(guild, player.getUniqueId())) {
            player.sendMessage(Text.mm("<red>Keine Rechte für Upgrades."));
            return;
        }
        if (args.length < 3) {
            player.sendMessage(Text.mm("<gray>/guild hall upgrade <craft|teleport|buff>"));
            return;
        }
        String type = args[2].toLowerCase();
        int current = guild.hallUpgrades().getOrDefault(type, 0);
        int cost = switch (type) {
            case "craft" -> 500 + (current * 300);
            case "teleport" -> 800 + (current * 400);
            case "buff" -> 1000 + (current * 500);
            default -> -1;
        };
        if (cost < 0) {
            player.sendMessage(Text.mm("<red>Unbekanntes Upgrade."));
            return;
        }
        if (guild.bankGold() < cost) {
            player.sendMessage(Text.mm("<red>Nicht genug Gold in der Gildenbank. Benötigt: " + cost));
            return;
        }
        guild.setBankGold(guild.bankGold() - cost);
        guild.hallUpgrades().put(type, current + 1);
        plugin.guildManager().saveAll();
        player.sendMessage(Text.mm("<green>Upgrade verbessert: " + type + " (Stufe " + (current + 1) + ")"));
    }

    private boolean isOfficerOrLeader(Guild guild, UUID member) {
        GuildMemberRole role = guild.members().get(member);
        return role == GuildMemberRole.LEADER || role == GuildMemberRole.OFFICER;
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

    private String join(String[] args, int start) {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < args.length; i++) {
            if (i > start) {
                builder.append(" ");
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }
}
