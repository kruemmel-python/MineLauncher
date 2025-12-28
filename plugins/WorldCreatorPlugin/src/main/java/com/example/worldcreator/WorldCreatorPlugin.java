package com.example.worldcreator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class WorldCreatorPlugin extends JavaPlugin implements Listener {
    private static final String MENU_TITLE = ChatColor.DARK_AQUA + "World Creator";
    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private NamespacedKey worldTypeKey;
    private final Map<WorldTypeOption, Integer> slotMap = new EnumMap<>(WorldTypeOption.class);

    @Override
    public void onEnable() {
        worldTypeKey = new NamespacedKey(this, "world-type");
        Bukkit.getPluginManager().registerEvents(this, this);

        slotMap.put(WorldTypeOption.VOID, 10);
        slotMap.put(WorldTypeOption.WATER, 11);
        slotMap.put(WorldTypeOption.SKY_ISLANDS, 12);
        slotMap.put(WorldTypeOption.SKY_REALMS, 13);
        slotMap.put(WorldTypeOption.JUNGLE, 14);
        slotMap.put(WorldTypeOption.DESERT, 15);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler können dieses Kommando nutzen.");
            return true;
        }

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("tp")) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Verwende: /worlds tp <welt>");
                    return true;
                }
                World world = Bukkit.getWorld(args[1]);
                if (world == null) {
                    player.sendMessage(ChatColor.RED + "Welt nicht gefunden.");
                    return true;
                }
                player.teleport(world.getSpawnLocation());
                player.sendMessage(ChatColor.GREEN + "Teleportiert nach: " + world.getName());
                return true;
            }
            if (args[0].equalsIgnoreCase("list")) {
                String worlds = Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.joining(", "));
                player.sendMessage(ChatColor.YELLOW + "Welten: " + worlds);
                return true;
            }
        }

        player.openInventory(buildMenu());
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!event.getView().getTitle().equals(MENU_TITLE)) {
            return;
        }

        event.setCancelled(true);
        var clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        var meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        var typeName = meta.getPersistentDataContainer().get(worldTypeKey, PersistentDataType.STRING);
        if (typeName == null) {
            return;
        }

        var option = WorldTypeOption.valueOf(typeName);
        createWorldAndTeleport(player, option);
    }

    private Inventory buildMenu() {
        var inventory = Bukkit.createInventory(null, 27, MENU_TITLE);
        for (var entry : slotMap.entrySet()) {
            inventory.setItem(entry.getValue(), createMenuItem(entry.getKey()));
        }
        return inventory;
    }

    private ItemStack createMenuItem(WorldTypeOption option) {
        var item = new ItemStack(option.getIcon());
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + option.getDisplayName());
            meta.getPersistentDataContainer().set(worldTypeKey, PersistentDataType.STRING, option.name());
            item.setItemMeta(meta);
        }
        return item;
    }

    private void createWorldAndTeleport(Player player, WorldTypeOption option) {
        var worldName = option.name().toLowerCase() + "-" + NAME_FORMAT.format(LocalDateTime.now());
        var creator = new WorldCreator(worldName);

        switch (option) {
            case VOID -> creator.generator(new VoidChunkGenerator());
            case WATER -> creator.generator(new WaterChunkGenerator());
            case SKY_ISLANDS -> creator.generator(new SkyIslandsChunkGenerator());
            case SKY_REALMS -> {
                creator.generator(new SkyIslandsChunkGenerator());
                creator.biomeProvider(new MultiBiomeProvider());
            }
            case JUNGLE -> creator.biomeProvider(new FixedBiomeProvider(Biome.JUNGLE));
            case DESERT -> creator.biomeProvider(new FixedBiomeProvider(Biome.DESERT));
        }

        World world = creator.createWorld();
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Welt konnte nicht erstellt werden.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Welt erstellt: " + world.getName());
        player.teleport(world.getSpawnLocation());
    }
}
