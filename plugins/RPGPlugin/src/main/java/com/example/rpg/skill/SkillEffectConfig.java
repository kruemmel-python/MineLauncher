package com.example.rpg.skill;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillEffectConfig {
    private SkillEffectType type;
    private Map<String, Object> params = new HashMap<>();

    public SkillEffectConfig(SkillEffectType type, Map<String, Object> params) {
        this.type = type;
        if (params != null) {
            this.params.putAll(params);
        }
    }

    public SkillEffectType type() {
        return type;
    }

    public void setType(SkillEffectType type) {
        this.type = type;
    }

    public Map<String, Object> params() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String describe() {
        if (params.isEmpty()) {
            return type.name();
        }
        String joined = params.entrySet().stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", "));
        return type.name() + " (" + joined + ")";
    }
}
