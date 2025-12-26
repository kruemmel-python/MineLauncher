package com.example.rpg.schematic;

import com.example.rpg.schematic.nbt.NbtCompound;
import com.example.rpg.schematic.nbt.NbtList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Barrel;
import org.bukkit.block.Chest;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import net.kyori.adventure.text.Component;
import org.bukkit.Nameable;

public class BlockEntityApplier {
    private final Logger logger;

    public BlockEntityApplier(Logger logger) {
        this.logger = logger;
    }

    public void apply(World world, Location origin, NbtCompound nbt, Transform transform, int width, int length) {
        int[] pos = readPos(nbt);
        if (pos == null || pos.length < 3) {
            return;
        }
        int[] transformed = transform.apply(pos[0], pos[1], pos[2], width, length);
        Location location = origin.clone().add(transformed[0], transformed[1], transformed[2]);
        Block block = world.getBlockAt(location);
        BlockState state = block.getState();
        if (state instanceof Sign sign) {
            applySign(sign, nbt);
        } else if (state instanceof CreatureSpawner spawner) {
            applySpawner(spawner, nbt);
        } else if (state instanceof Chest chest) {
            applyContainerName(chest, nbt);
        } else if (state instanceof Barrel barrel) {
            applyContainerName(barrel, nbt);
        }
    }

    private void applySign(Sign sign, NbtCompound nbt) {
        String[] lines = new String[4];
        boolean hasLine = false;
        for (int i = 0; i < 4; i++) {
            String key = "Text" + (i + 1);
            String value = nbt.getString(key, null);
            if (value != null) {
                lines[i] = parseSignText(value);
                hasLine = true;
            }
        }
        NbtCompound frontText = nbt.getCompound("front_text");
        if (frontText != null) {
            NbtList messages = frontText.getList("messages");
            if (messages != null) {
                List<String> msgList = messages.strings();
                for (int i = 0; i < Math.min(4, msgList.size()); i++) {
                    lines[i] = parseSignText(msgList.get(i));
                    hasLine = true;
                }
            }
        }
        if (!hasLine) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            if (lines[i] != null) {
                sign.setLine(i, lines[i]);
            }
        }
        sign.update(true, false);
    }

    private void applySpawner(CreatureSpawner spawner, NbtCompound nbt) {
        NbtCompound spawnData = nbt.getCompound("SpawnData");
        String id = null;
        if (spawnData != null) {
            id = spawnData.getString("id", null);
            if (id == null) {
                NbtCompound entity = spawnData.getCompound("entity");
                if (entity != null) {
                    id = entity.getString("id", null);
                }
            }
        }
        if (id == null) {
            id = nbt.getString("EntityId", null);
        }
        if (id == null) {
            return;
        }
        EntityType type = EntityType.fromName(stripNamespace(id));
        if (type != null) {
            spawner.setSpawnedType(type);
            spawner.update(true, false);
        }
    }

    private void applyContainerName(BlockState container, NbtCompound nbt) {
        String name = nbt.getString("CustomName", null);
        if (name == null) {
            return;
        }
        if (container instanceof Nameable nameable) {
            nameable.customName(Component.text(parseSignText(name)));
            container.update(true, false);
        }
    }

    private int[] readPos(NbtCompound nbt) {
        int[] pos = nbt.getIntArray("Pos");
        if (pos != null) {
            return pos;
        }
        NbtList list = nbt.getList("Pos");
        if (list != null) {
            List<Double> values = list.doubles();
            if (values.size() >= 3) {
                return new int[]{values.get(0).intValue(), values.get(1).intValue(), values.get(2).intValue()};
            }
        }
        return null;
    }

    private String parseSignText(String raw) {
        if (raw == null) {
            return "";
        }
        if (raw.startsWith("{") && raw.contains("\"text\"")) {
            int start = raw.indexOf("\"text\"");
            int colon = raw.indexOf(':', start);
            int firstQuote = raw.indexOf('"', colon + 1);
            if (firstQuote >= 0) {
                int secondQuote = raw.indexOf('"', firstQuote + 1);
                if (secondQuote > firstQuote) {
                    return raw.substring(firstQuote + 1, secondQuote);
                }
            }
        }
        return raw.replace('"', ' ').trim();
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
