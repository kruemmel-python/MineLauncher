package com.example.rpg.behavior;

public class InverterNode extends BehaviorNode {
    private final BehaviorNode child;

    public InverterNode(String id, BehaviorNode child) {
        super(id);
        this.child = child;
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        BehaviorStatus status = child.tick(context);
        return switch (status) {
            case SUCCESS -> BehaviorStatus.FAILURE;
            case FAILURE -> BehaviorStatus.SUCCESS;
            case RUNNING -> BehaviorStatus.RUNNING;
        };
    }
}
