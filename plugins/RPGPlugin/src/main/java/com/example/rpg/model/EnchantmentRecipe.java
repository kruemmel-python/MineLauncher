package com.example.rpg.model;

import com.example.rpg.skill.SkillEffectConfig;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;

public class EnchantmentRecipe {
    private final String id;
    private EnchantRecipeType type;
    private EnchantTargetSlot targetSlot;
    private RPGStat statToImprove;
    private int minLevel;
    private int costGold;
    private Material costMaterial;
    private int costAmount;
    private String affix;
    private final List<SkillEffectConfig> effects = new ArrayList<>();

    public EnchantmentRecipe(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public EnchantRecipeType type() {
        return type;
    }

    public void setType(EnchantRecipeType type) {
        this.type = type;
    }

    public EnchantTargetSlot targetSlot() {
        return targetSlot;
    }

    public void setTargetSlot(EnchantTargetSlot targetSlot) {
        this.targetSlot = targetSlot;
    }

    public RPGStat statToImprove() {
        return statToImprove;
    }

    public void setStatToImprove(RPGStat statToImprove) {
        this.statToImprove = statToImprove;
    }

    public int minLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public int costGold() {
        return costGold;
    }

    public void setCostGold(int costGold) {
        this.costGold = costGold;
    }

    public Material costMaterial() {
        return costMaterial;
    }

    public void setCostMaterial(Material costMaterial) {
        this.costMaterial = costMaterial;
    }

    public int costAmount() {
        return costAmount;
    }

    public void setCostAmount(int costAmount) {
        this.costAmount = costAmount;
    }

    public String affix() {
        return affix;
    }

    public void setAffix(String affix) {
        this.affix = affix;
    }

    public List<SkillEffectConfig> effects() {
        return effects;
    }
}
