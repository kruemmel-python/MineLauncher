package com.example.rpg.model;

import java.util.ArrayList;
import java.util.List;

public class DialogueNode {
    private final String id;
    private String text;
    private final List<DialogueOption> options = new ArrayList<>();

    public DialogueNode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String text() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<DialogueOption> options() {
        return options;
    }
}
