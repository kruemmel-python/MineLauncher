package com.example.rpg.behavior;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class FleeNode extends BehaviorNode {
    public FleeNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        Location mobLoc = context.mob().getLocation();
        Location targetLoc = target.getLocation();
        Vector direction = mobLoc.toVector().subtract(targetLoc.toVector());
        if (direction.lengthSquared() == 0) {
            return BehaviorStatus.FAILURE;
        }
        direction.normalize().multiply(0.35);
        context.mob().setVelocity(direction);
        return BehaviorStatus.RUNNING;
    }
}
