package de.yourname.rpg.npc;

import de.yourname.rpg.core.Registry;
import de.yourname.rpg.storage.StorageService;
import java.util.Optional;
import java.util.UUID;

public class NpcService {
    private final Registry<RpgNpc> registry = new Registry<>();
    private final StorageService storageService;

    public NpcService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void load() {
        storageService.loadNpcs().forEach(npc -> registry.register(npc.getId(), npc));
    }

    public void save() {
        storageService.saveNpcs(registry.all().stream().toList());
    }

    public Optional<RpgNpc> getNpc(String id) {
        return registry.get(id);
    }

    public void registerNpc(RpgNpc npc) {
        registry.register(npc.getId(), npc);
    }

    public Optional<RpgNpc> findByEntity(UUID entityUuid) {
        return registry.all().stream()
                .filter(npc -> entityUuid.equals(npc.getEntityUuid()))
                .findFirst();
    }
}
