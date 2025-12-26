package com.example.rpg.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AuditLog {
    private final File file;
    private final JavaPlugin plugin;

    public AuditLog(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "audit.log");
    }

    public void log(CommandSender sender, String action) {
        String line = Instant.now() + " | " + sender.getName() + " | " + action + System.lineSeparator();
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line);
        } catch (IOException e) {
            plugin.getLogger().warning("Audit log failed: " + e.getMessage());
        }
    }
}
