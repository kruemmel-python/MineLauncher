package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class EntitySpawner {
    private final Logger logger;

    public EntitySpawner(Logger logger) {
        this.logger = logger;
    }

    public void spawn(World world, Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        String id = nbt.getString("Id", null);
        if (id == null) {
            id = nbt.getString("id", null);
        }
        if (id == null) {
            return;
        }
        EntityType type = EntityType.fromName(stripNamespace(id));
        if (type == null) {
            logger.warning("Unsupported entity type: " + id);
            return;
        }
        Location location = resolvePosition(origin, nbt, transform, width, length);
        if (location == null) {
            return;
        }
        Entity entity = world.spawnEntity(location, type);
        if (entity instanceof ArmorStand armorStand) {
            applyArmorStand(armorStand, nbt);
        } else if (entity instanceof ItemFrame itemFrame) {
            applyItemFrame(itemFrame, nbt);
        }
    }

    private Location resolvePosition(Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        NbtList posList = nbt.getList("Pos");
        if (posList == null) {
            return null;
        }
        List<Double> coords = posList.doubles();
        if (coords.size() < 3) {
            return null;
        }
        int x = coords.get(0).intValue();
        int y = coords.get(1).intValue();
        int z = coords.get(2).intValue();
        int[] transformed = transform.apply(x, y, z, width, length);
        return origin.clone().add(transformed[0], transformed[1], transformed[2]);
    }

    private void applyArmorStand(ArmorStand armorStand, NbtCompound nbt) {
        armorStand.setSmall(nbt.getInt("Small", 0) == 1);
        armorStand.setInvisible(nbt.getInt("Invisible", 0) == 1);
        armorStand.setArms(nbt.getInt("ShowArms", 0) == 1);
        String customName = nbt.getString("CustomName", null);
        if (customName != null && !customName.isBlank()) {
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(customName);
        }
    }

    private void applyItemFrame(ItemFrame itemFrame, NbtCompound nbt) {
        NbtCompound item = nbt.getCompound("Item");
        if (item == null) {
            return;
        }
        String id = item.getString("id", null);
        if (id == null) {
            return;
        }
        Material material = Material.matchMaterial(stripNamespace(id).toUpperCase());
        if (material == null) {
            logger.warning("Unknown item frame item: " + id);
            return;
        }
        int count = item.getInt("Count", 1);
        itemFrame.setItem(new ItemStack(material, Math.max(1, count)));
    }

    private String stripNamespace(String id) {
        if (id == null) {
            return null;
        }
        if (id.contains(":")) {
            return id.substring(id.indexOf(':') + 1);
        }
        return id;
    }
}
