package com.example.rpg.behavior;

import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class WalkToTargetNode extends BehaviorNode {
    public WalkToTargetNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        if (context.mob() instanceof Mob mob) {
            mob.setTarget(target);
            mob.getPathfinder().moveTo(target);
            return BehaviorStatus.RUNNING;
        }
        return BehaviorStatus.FAILURE;
    }
}
