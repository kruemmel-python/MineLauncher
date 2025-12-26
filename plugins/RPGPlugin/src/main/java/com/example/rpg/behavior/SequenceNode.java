package com.example.rpg.behavior;

public class SequenceNode extends CompositeNode {
    public SequenceNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.FAILURE) {
                return BehaviorStatus.FAILURE;
            }
            if (status == BehaviorStatus.RUNNING) {
                return BehaviorStatus.RUNNING;
            }
        }
        return BehaviorStatus.SUCCESS;
    }
}
