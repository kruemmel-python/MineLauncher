package com.example.rpg.behavior;

import org.bukkit.entity.Player;

/**
 * Checks if the mob has line of sight to its current target.
 */
public class HasLineOfSightNode extends BehaviorNode {
    private final double maxDistance;

    /**
     * Creates a LOS check without distance limit.
     *
     * @param id node id
     */
    public HasLineOfSightNode(String id) {
        this(id, -1);
    }

    /**
     * Creates a LOS check with optional distance limit.
     *
     * @param id node id
     * @param maxDistance maximum distance or -1 to ignore
     */
    public HasLineOfSightNode(String id, double maxDistance) {
        super(id);
        this.maxDistance = maxDistance;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        Player target = context.target();
        if (target == null || target.isDead() || !target.isValid()) {
            return BehaviorStatus.FAILURE;
        }
        if (!target.getWorld().equals(context.mob().getWorld())) {
            return BehaviorStatus.FAILURE;
        }
        if (maxDistance >= 0) {
            double distanceSquared = context.mob().getLocation().distanceSquared(target.getLocation());
            if (distanceSquared > (maxDistance * maxDistance)) {
                return BehaviorStatus.FAILURE;
            }
        }
        return context.mob().hasLineOfSight(target) ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}
