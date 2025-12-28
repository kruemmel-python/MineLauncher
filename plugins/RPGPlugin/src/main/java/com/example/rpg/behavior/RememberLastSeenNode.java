package com.example.rpg.behavior;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Stores the last seen target location when line of sight is available.
 */
public class RememberLastSeenNode extends BehaviorNode {
    private final double maxDistance;

    /**
     * Creates a node without distance limit.
     *
     * @param id node id
     */
    public RememberLastSeenNode(String id) {
        this(id, -1);
    }

    /**
     * Creates a node with optional distance limit.
     *
     * @param id node id
     * @param maxDistance maximum distance or -1 to ignore
     */
    public RememberLastSeenNode(String id, double maxDistance) {
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
        if (!context.mob().hasLineOfSight(target)) {
            return BehaviorStatus.FAILURE;
        }
        Location location = target.getLocation().clone();
        context.putState(BehaviorKeys.LAST_SEEN, location);
        context.putStateLong(BehaviorKeys.LAST_SEEN_TIMESTAMP, System.currentTimeMillis());
        return BehaviorStatus.SUCCESS;
    }
}
