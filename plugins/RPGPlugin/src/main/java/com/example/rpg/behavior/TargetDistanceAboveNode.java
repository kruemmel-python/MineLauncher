package com.example.rpg.behavior;

import org.bukkit.entity.Player;

public class TargetDistanceAboveNode extends BehaviorNode {
    private final double distance;

    public TargetDistanceAboveNode(String id, double distance) {
        super(id);
        this.distance = distance;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        double dist = target.getLocation().distance(context.mob().getLocation());
        return dist > distance ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}
