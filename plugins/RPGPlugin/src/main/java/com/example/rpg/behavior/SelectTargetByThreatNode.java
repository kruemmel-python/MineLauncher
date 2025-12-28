package com.example.rpg.behavior;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Selects the highest threat player as target.
 */
public class SelectTargetByThreatNode extends BehaviorNode {
    private final double maxRange;
    private final boolean requireLineOfSight;

    /**
     * Creates a selector with default range and LOS requirement.
     *
     * @param id node id
     */
    public SelectTargetByThreatNode(String id) {
        this(id, 32.0, false);
    }

    /**
     * Creates a selector.
     *
     * @param id node id
     * @param maxRange maximum range
     * @param requireLineOfSight whether line of sight is required
     */
    public SelectTargetByThreatNode(String id, double maxRange, boolean requireLineOfSight) {
        super(id);
        this.maxRange = maxRange;
        this.requireLineOfSight = requireLineOfSight;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
        if (threatTable == null || threatTable.isEmpty()) {
            return BehaviorStatus.FAILURE;
        }
        LivingEntity mob = context.mob();
        Player target = threatTable.getTopThreatTarget(player -> isValidTarget(mob, player));
        if (target == null) {
            return BehaviorStatus.FAILURE;
        }
        context.setTarget(target);
        return BehaviorStatus.SUCCESS;
    }

    private boolean isValidTarget(LivingEntity mob, Player player) {
        if (player == null || player.isDead() || !player.isValid()) {
            return false;
        }
        if (!player.getWorld().equals(mob.getWorld())) {
            return false;
        }
        double distanceSquared = mob.getLocation().distanceSquared(player.getLocation());
        if (distanceSquared > maxRange * maxRange) {
            return false;
        }
        if (requireLineOfSight && !mob.hasLineOfSight(player)) {
            return false;
        }
        return !player.isSpectator();
    }
}
