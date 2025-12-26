package com.example.rpg.behavior;

public class CooldownNode extends BehaviorNode {
    private final BehaviorNode child;
    private final long cooldownMillis;

    public CooldownNode(String id, BehaviorNode child, long cooldownMillis) {
        super(id);
        this.child = child;
        this.cooldownMillis = cooldownMillis;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        long now = System.currentTimeMillis();
        String key = key(context.mobId());
        Long last = context.cooldowns().get(key);
        if (last != null && now - last < cooldownMillis) {
            return BehaviorStatus.FAILURE;
        }
        BehaviorStatus status = child.tick(context);
        if (status == BehaviorStatus.SUCCESS) {
            context.cooldowns().put(key, now);
        }
        return status;
    }
}
