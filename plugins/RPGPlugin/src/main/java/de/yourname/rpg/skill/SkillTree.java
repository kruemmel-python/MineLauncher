package de.yourname.rpg.skill;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {
    private String id;
    private String name;
    private List<SkillNode> nodes = new ArrayList<>();

    public SkillTree() {
    }

    public SkillTree(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<SkillNode> getNodes() {
        return nodes;
    }
}
