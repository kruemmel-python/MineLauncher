package de.yourname.rpg.skill;

import java.util.ArrayList;
import java.util.List;

public class SkillNode {
    private Skill skill;
    private List<String> prerequisites = new ArrayList<>();

    public SkillNode() {
    }

    public SkillNode(Skill skill) {
        this.skill = skill;
    }

    public Skill getSkill() {
        return skill;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }
}
