package de.yourname.rpg.skill;

import de.yourname.rpg.core.Registry;
import de.yourname.rpg.storage.StorageService;
import java.util.Optional;

public class SkillService {
    private final Registry<SkillTree> registry = new Registry<>();
    private final StorageService storageService;

    public SkillService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void load() {
        storageService.loadSkillTrees().forEach(tree -> registry.register(tree.getId(), tree));
    }

    public void save() {
        storageService.saveSkillTrees(registry.all().stream().toList());
    }

    public Optional<SkillTree> getTree(String id) {
        return registry.get(id);
    }
}
