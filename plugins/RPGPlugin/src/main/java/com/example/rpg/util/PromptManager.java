package com.example.rpg.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class PromptManager {
    private final Map<UUID, Consumer<String>> prompts = new HashMap<>();

    public void prompt(Player player, Component message, Consumer<String> handler) {
        prompts.put(player.getUniqueId(), handler);
        player.sendMessage(message);
    }

    public boolean handle(Player player, String message) {
        Consumer<String> handler = prompts.remove(player.getUniqueId());
        if (handler == null) {
            return false;
        }
        handler.accept(message);
        return true;
    }

    public void cancel(Player player) {
        prompts.remove(player.getUniqueId());
    }
}
