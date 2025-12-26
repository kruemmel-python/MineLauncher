package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class MeleeAttackNode extends BehaviorNode {
    public MeleeAttackNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (target.getLocation().distanceSquared(context.mob().getLocation()) > 9) {
            return BehaviorStatus.FAILURE;
        }
        target.damage(context.definition().damage(), context.mob());
        return BehaviorStatus.SUCCESS;
    }
}
