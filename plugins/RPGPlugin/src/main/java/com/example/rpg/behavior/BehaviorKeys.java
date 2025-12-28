package com.example.rpg.behavior;

/**
 * Constants for blackboard keys used by behavior nodes.
 */
public final class BehaviorKeys {
    public static final String LAST_SEEN = "bt.lastSeen";
    public static final String LAST_SEEN_TIMESTAMP = "bt.lastSeenTs";
    public static final String SEARCH_UNTIL = "bt.searchUntil";
    public static final String SEARCH_ANGLE = "bt.searchAngle";
    public static final String THREAT_TABLE = "bt.threat";
    public static final String DEBUG_LAST_LOG = "bt.debugLastLog";

    private static final String MEMORY_SEQUENCE_PREFIX = "bt.mem.seq.";
    private static final String MEMORY_SELECTOR_PREFIX = "bt.mem.sel.";

    private BehaviorKeys() {
    }

    /**
     * Builds a memory key for a sequence node.
     *
     * @param nodeId node id
     * @return key for memory sequence
     */
    public static String memorySequenceKey(String nodeId) {
        return MEMORY_SEQUENCE_PREFIX + nodeId;
    }

    /**
     * Builds a memory key for a selector node.
     *
     * @param nodeId node id
     * @return key for memory selector
     */
    public static String memorySelectorKey(String nodeId) {
        return MEMORY_SELECTOR_PREFIX + nodeId;
    }
}
