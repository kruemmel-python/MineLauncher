package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BehaviorCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public BehaviorCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length < 2 || !"edit".equalsIgnoreCase(args[0])) {
            player.sendMessage(Text.mm("<gray>/behavior edit <tree>"));
            return true;
        }
        String treeName = args[1];
        plugin.behaviorTreeEditorGui().open(player, treeName);
        return true;
    }
}
