package com.example.rpg.behavior;

import java.util.UUID;

public abstract class BehaviorNode {
    private final String id;
    protected final String nodeId;

    protected BehaviorNode(String id) {
        this.id = id;
        this.nodeId = id;
    }

    public String id() {
        return id;
    }

    /**
     * Returns the unique node instance id used for blackboard memory.
     *
     * @return unique node id
     */
    public String getNodeId() {
        return nodeId;
    }

    public abstract BehaviorStatus tick(BehaviorContext context);

    protected String key(UUID entityId) {
        return id + ":" + entityId;
    }
}
