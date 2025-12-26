package com.example.rpg.manager;

import com.example.rpg.model.Skill;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTreeManager {
    private final SkillManager skillManager;
    private final Map<String, SkillNode> nodes = new HashMap<>();

    public SkillTreeManager(SkillManager skillManager) {
        this.skillManager = skillManager;
        rebuild();
    }

    public void rebuild() {
        nodes.clear();
        for (Skill skill : skillManager.skills().values()) {
            nodes.put(skill.id(), new SkillNode(skill));
        }
        for (SkillNode node : nodes.values()) {
            String parentId = node.skill().requiredSkill();
            if (parentId != null) {
                SkillNode parent = nodes.get(parentId);
                if (parent != null) {
                    parent.children().add(node);
                    node.setParent(parent);
                }
            }
        }
    }

    public List<SkillNode> roots() {
        List<SkillNode> roots = new ArrayList<>();
        for (SkillNode node : nodes.values()) {
            if (node.parent() == null) {
                roots.add(node);
            }
        }
        roots.sort(Comparator.comparing(n -> n.skill().id()));
        return roots;
    }

    public Map<String, SkillNode> nodes() {
        return nodes;
    }

    public static class SkillNode {
        private final Skill skill;
        private SkillNode parent;
        private final List<SkillNode> children = new ArrayList<>();

        public SkillNode(Skill skill) {
            this.skill = skill;
        }

        public Skill skill() {
            return skill;
        }

        public SkillNode parent() {
            return parent;
        }

        public void setParent(SkillNode parent) {
            this.parent = parent;
        }

        public List<SkillNode> children() {
            return children;
        }
    }
}
