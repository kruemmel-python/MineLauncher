package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class CastSkillNode extends BehaviorNode {
    private final String skillId;

    public CastSkillNode(String id, String skillId) {
        super(id);
        this.skillId = skillId;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        boolean success = context.plugin().useMobSkill(context.mob(), target, skillId);
        return success ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}
