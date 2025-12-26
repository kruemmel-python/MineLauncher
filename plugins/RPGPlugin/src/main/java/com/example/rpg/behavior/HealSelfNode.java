package com.example.rpg.behavior;

public class HealSelfNode extends BehaviorNode {
    private final double amount;

    public HealSelfNode(String id, double amount) {
        super(id);
        this.amount = amount;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double next = Math.min(maxHealth, context.mob().getHealth() + amount);
        context.mob().setHealth(next);
        return BehaviorStatus.SUCCESS;
    }
}
