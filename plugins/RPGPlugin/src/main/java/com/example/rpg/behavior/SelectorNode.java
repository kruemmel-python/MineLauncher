package com.example.rpg.behavior;

public class SelectorNode extends CompositeNode {
    public SelectorNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        for (BehaviorNode child : children()) {
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.SUCCESS || status == BehaviorStatus.RUNNING) {
                return status;
            }
        }
        return BehaviorStatus.FAILURE;
    }
}
