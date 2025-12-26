package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VoiceChatCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public VoiceChatCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Text.mm("<gray>/voicechat <party|guild|leave>"));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "party" -> plugin.voiceChatManager().joinParty(player);
            case "guild" -> plugin.voiceChatManager().joinGuild(player);
            case "leave" -> plugin.voiceChatManager().leave(player);
            default -> player.sendMessage(Text.mm("<gray>/voicechat <party|guild|leave>"));
        }
        return true;
    }
}
