package com.example.rpg.behavior;

public class HealthBelowNode extends BehaviorNode {
    private final double threshold;

    public HealthBelowNode(String id, double threshold) {
        super(id);
        this.threshold = threshold;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        double maxHealth = Math.max(1, context.definition().health());
        double current = context.mob().getHealth();
        return (current / maxHealth) < threshold ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
    }
}
