package com.example.rpg.behavior;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Moves the mob to the last seen location and searches the area for a new target.
 */
public class SearchLastSeenAreaNode extends BehaviorNode {
    private final double arriveDistance;
    private final double searchRadius;
    private final int searchTicks;

    /**
     * Creates a search node with defaults.
     *
     * @param id node id
     */
    public SearchLastSeenAreaNode(String id) {
        this(id, 2.0, 12.0, 80);
    }

    /**
     * Creates a search node.
     *
     * @param id node id
     * @param arriveDistance distance considered "arrived"
     * @param searchRadius search radius for reacquiring targets
     * @param searchTicks number of ticks to search
     */
    public SearchLastSeenAreaNode(String id, double arriveDistance, double searchRadius, int searchTicks) {
        super(id);
        this.arriveDistance = arriveDistance;
        this.searchRadius = searchRadius;
        this.searchTicks = searchTicks;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        LivingEntity mob = context.mob();
        Player target = context.target();
        if (target != null && !target.isDead() && target.isValid() && mob.hasLineOfSight(target)) {
            return BehaviorStatus.SUCCESS;
        }
        Location lastSeen = context.getState(BehaviorKeys.LAST_SEEN, Location.class);
        if (lastSeen == null) {
            return BehaviorStatus.FAILURE;
        }
        if (!lastSeen.getWorld().equals(mob.getWorld())) {
            context.removeState(BehaviorKeys.LAST_SEEN);
            return BehaviorStatus.FAILURE;
        }
        double arriveDistanceSquared = arriveDistance * arriveDistance;
        if (mob.getLocation().distanceSquared(lastSeen) > arriveDistanceSquared) {
            moveTowards(mob, lastSeen);
            return BehaviorStatus.RUNNING;
        }
        long now = System.currentTimeMillis();
        long searchUntil = context.getStateLong(BehaviorKeys.SEARCH_UNTIL, 0);
        if (searchUntil <= 0) {
            searchUntil = now + (searchTicks * 50L);
            context.putStateLong(BehaviorKeys.SEARCH_UNTIL, searchUntil);
        }
        if (now > searchUntil) {
            context.removeState(BehaviorKeys.LAST_SEEN);
            context.removeState(BehaviorKeys.SEARCH_UNTIL);
            context.removeState(BehaviorKeys.SEARCH_ANGLE);
            return BehaviorStatus.FAILURE;
        }
        performSearchLook(mob, lastSeen, context);
        Player found = tryAcquireTarget(context, mob);
        if (found != null) {
            context.setTarget(found);
            return BehaviorStatus.SUCCESS;
        }
        return BehaviorStatus.RUNNING;
    }

    private void moveTowards(LivingEntity mob, Location location) {
        if (mob instanceof Mob pathing) {
            pathing.getPathfinder().moveTo(location);
            return;
        }
        Vector direction = location.toVector().subtract(mob.getLocation().toVector());
        if (direction.lengthSquared() > 0.001) {
            Vector velocity = direction.normalize().multiply(0.2);
            mob.setVelocity(velocity);
        }
    }

    private void performSearchLook(LivingEntity mob, Location lastSeen, BehaviorContext context) {
        Double angle = context.getState(BehaviorKeys.SEARCH_ANGLE, Double.class);
        if (angle == null) {
            angle = ThreadLocalRandom.current().nextDouble(0, 360);
        } else {
            angle = (angle + ThreadLocalRandom.current().nextDouble(10, 30)) % 360;
        }
        context.putState(BehaviorKeys.SEARCH_ANGLE, angle);
        Location look = lastSeen.clone();
        look.setYaw(angle.floatValue());
        mob.lookAt(look);
    }

    private Player tryAcquireTarget(BehaviorContext context, LivingEntity mob) {
        ThreatTable threatTable = context.getState(BehaviorKeys.THREAT_TABLE, ThreatTable.class);
        if (threatTable != null) {
            return threatTable.getTopThreatTarget(player -> isValidTarget(mob, player, searchRadius, true));
        }
        Optional<Player> nearest = mob.getWorld().getPlayers().stream()
            .filter(player -> isValidTarget(mob, player, searchRadius, true))
            .min(Comparator.comparingDouble(player -> player.getLocation().distanceSquared(mob.getLocation())));
        return nearest.orElse(null);
    }

    private boolean isValidTarget(LivingEntity mob, Player player, double maxRange, boolean requireLos) {
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
        if (requireLos && !mob.hasLineOfSight(player)) {
            return false;
        }
        return !player.isSpectator();
    }
}
