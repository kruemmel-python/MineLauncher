package com.example.rpg.behavior;

/**
 * Sequence node that remembers the running child per mob context.
 */
public class MemorySequenceNode extends CompositeNode {
    public MemorySequenceNode(String id) {
        super(id);
    }

    @Override
    public BehaviorStatus tick(BehaviorContext context) {
        String key = BehaviorKeys.memorySequenceKey(nodeId);
        int startIndex = (int) context.getStateLong(key, 0);
        for (int i = startIndex; i < children().size(); i++) {
            BehaviorNode child = children().get(i);
            BehaviorStatus status = child.tick(context);
            if (status == BehaviorStatus.FAILURE) {
                context.putStateLong(key, 0);
                return BehaviorStatus.FAILURE;
            }
            if (status == BehaviorStatus.RUNNING) {
                context.putStateLong(key, i);
                return BehaviorStatus.RUNNING;
            }
            context.putStateLong(key, i + 1);
        }
        context.putStateLong(key, 0);
        return BehaviorStatus.SUCCESS;
    }
}
