package com.example.rpg.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldUtils {
    public static void unloadAndDeleteWorld(World world, Location fallback) {
        if (world == null) {
            return;
        }

        for (Player player : world.getPlayers()) {
            if (fallback != null) {
                player.teleport(fallback);
            } else if (!Bukkit.getWorlds().isEmpty()) {
                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            }
            player.sendMessage(Text.mm("<yellow>Der Dungeon löst sich auf..."));
        }

        Bukkit.unloadWorld(world, false);
        File worldFolder = world.getWorldFolder();
        deleteDirectory(worldFolder);
    }

    private static void deleteDirectory(File directory) {
        if (directory == null || !directory.exists()) {
            return;
        }
        try (Stream<Path> walk = Files.walk(directory.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
