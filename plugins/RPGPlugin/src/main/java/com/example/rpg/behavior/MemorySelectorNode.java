package com.example.rpg.behavior;

/**
 * Selector node that remembers the running child per mob context.
 */
public class MemorySelectorNode extends CompositeNode {
    public MemorySelectorNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        String key = BehaviorKeys.memorySelectorKey(nodeId);
        int startIndex = (int) context.getStateLong(key, 0);
        for (int i = startIndex; i < children().size(); i++) {
            BehaviorNode child = children().get(i);
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.SUCCESS) {
                context.putStateLong(key, 0);
                return BehaviorStatus.SUCCESS;
            }
            if (status == BehaviorStatus.RUNNING) {
                context.putStateLong(key, i);
                return BehaviorStatus.RUNNING;
            }
            context.putStateLong(key, i + 1);
        }
        context.putStateLong(key, 0);
        return BehaviorStatus.FAILURE;
    }
}
