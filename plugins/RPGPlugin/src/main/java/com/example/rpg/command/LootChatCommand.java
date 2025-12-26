package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LootChatCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public LootChatCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean current = plugin.getConfig().getBoolean("lootchat.enabled", true);
        boolean next = args.length == 1 ? Boolean.parseBoolean(args[0]) : !current;
        plugin.getConfig().set("lootchat.enabled", next);
        plugin.saveConfig();
        sender.sendMessage(Text.mm(next ? "<green>Lootchat aktiviert." : "<red>Lootchat deaktiviert."));
        return true;
    }
}
