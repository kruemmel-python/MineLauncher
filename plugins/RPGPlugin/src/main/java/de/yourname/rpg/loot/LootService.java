package de.yourname.rpg.loot;

import de.yourname.rpg.core.Registry;
import de.yourname.rpg.storage.StorageService;
import java.util.List;
import java.util.Optional;

public class LootService {
    private final Registry<LootTable> tables = new Registry<>();
    private final LootRoller roller = new LootRoller();
    private final StorageService storageService;

    public LootService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void load() {
        storageService.loadLootTables().forEach(table -> tables.register(table.getId(), table));
    }

    public void save() {
        storageService.saveLootTables(tables.all().stream().toList());
    }

    public Optional<LootTable> getTable(String id) {
        return tables.get(id);
    }

    public void registerTable(LootTable table) {
        tables.register(table.getId(), table);
    }

    public List<LootTable> listTables() {
        return tables.all().stream().toList();
    }

    public List<String> roll(String tableId) {
        return getTable(tableId).map(roller::roll).orElse(List.of());
    }
}
