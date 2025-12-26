package com.example.rpg.behavior;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeNode extends BehaviorNode {
    private final List<BehaviorNode> children = new ArrayList<>();

    protected CompositeNode(String id) {
        super(id);
    }

    public List<BehaviorNode> children() {
        return children;
    }
}
