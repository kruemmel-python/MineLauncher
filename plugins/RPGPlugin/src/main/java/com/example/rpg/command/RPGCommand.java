package com.example.rpg.command;

import com.example.rpg.RPGPlugin;
import com.example.rpg.model.ClassDefinition;
import com.example.rpg.model.PlayerProfile;
import com.example.rpg.model.Skill;
import com.example.rpg.model.SkillType;
import com.example.rpg.util.Text;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RPGCommand implements CommandExecutor {
    private final RPGPlugin plugin;

    public RPGCommand(RPGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler.");
            return true;
        }
        if (args.length == 0) {
            plugin.guiManager().openPlayerMenu(player);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "skill" -> handleSkill(player, args);
            case "quest" -> handleQuest(player, args);
            case "respec" -> handleRespec(player);
            case "class" -> handleClass(player, args);
            default -> player.sendMessage(Text.mm("<gray>/rpg <skill|quest|respec|class>"));
        }
        return true;
    }

    private void handleSkill(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg skill <id>"));
            return;
        }
        String skillId = args[1].toLowerCase();
        Skill skill = plugin.skillManager().getSkill(skillId);
        if (skill == null) {
            player.sendMessage(Text.mm("<red>Unbekannter Skill."));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        if (!profile.learnedSkills().containsKey(skillId)) {
            player.sendMessage(Text.mm("<red>Skill nicht gelernt."));
            return;
        }
        if (skill.type() == SkillType.PASSIVE) {
            player.sendMessage(Text.mm("<yellow>Passiver Skill ist aktiv."));
            return;
        }
        long now = System.currentTimeMillis();
        long last = profile.skillCooldowns().getOrDefault(skillId, 0L);
        if (now - last < skill.cooldown() * 1000L) {
            long remaining = (skill.cooldown() * 1000L - (now - last)) / 1000L;
            player.sendMessage(Text.mm("<red>Cooldown: " + remaining + "s"));
            return;
        }
        if (profile.mana() < skill.manaCost()) {
            player.sendMessage(Text.mm("<red>Nicht genug Mana."));
            return;
        }
        profile.setMana(profile.mana() - skill.manaCost());
        applySkillEffect(player, profile, skill.effect());
        profile.skillCooldowns().put(skillId, now);
        player.sendMessage(Text.mm("<green>Skill benutzt: " + skill.name()));
    }

    private void applySkillEffect(Player player, PlayerProfile profile, String effect) {
        switch (effect.toLowerCase()) {
            case "dash" -> {
                Vector direction = player.getLocation().getDirection().multiply(1.2);
                direction.setY(0.3);
                player.setVelocity(direction);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1.2f);
            }
            case "heal_small" -> {
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                    ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                    : 20.0;
                double newHealth = Math.min(maxHealth, player.getHealth() + 4);
                player.setHealth(newHealth);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.5f);
            }
            case "heal_medium" -> {
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                    ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                    : 20.0;
                double newHealth = Math.min(maxHealth, player.getHealth() + 8);
                player.setHealth(newHealth);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1f, 1.2f);
            }
            case "heal_large" -> {
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH) != null
                    ? player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()
                    : 20.0;
                double newHealth = Math.min(maxHealth, player.getHealth() + 12);
                player.setHealth(newHealth);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1.0f);
            }
            case "taunt" -> {
                for (Entity entity : player.getNearbyEntities(8, 4, 8)) {
                    if (entity instanceof org.bukkit.entity.Mob mob) {
                        mob.setTarget(player);
                    }
                }
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_REPAIR, 1f, 0.7f);
            }
            case "fireball" -> {
                player.launchProjectile(org.bukkit.entity.SmallFireball.class);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1.2f);
            }
            case "frostbolt" -> {
                player.launchProjectile(org.bukkit.entity.Snowball.class);
                player.getNearbyEntities(6, 3, 6).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player))
                    .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                    .forEach(target -> target.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.SLOW, 60, 1
                    )));
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.4f);
            }
            case "arcane_blast" -> {
                player.getNearbyEntities(5, 3, 5).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player))
                    .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                    .forEach(target -> target.damage(6.0, player));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1f, 1.2f);
            }
            case "power_strike" -> {
                player.getNearbyEntities(3, 2, 3).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player))
                    .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                    .findFirst()
                    .ifPresent(target -> target.damage(8.0, player));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1f, 1.0f);
            }
            case "whirlwind" -> {
                player.getNearbyEntities(4, 2, 4).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player))
                    .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                    .forEach(target -> target.damage(5.0, player));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 0.8f);
            }
            case "execute" -> {
                player.getNearbyEntities(3, 2, 3).stream()
                    .filter(entity -> entity instanceof org.bukkit.entity.LivingEntity && !(entity instanceof Player))
                    .map(entity -> (org.bukkit.entity.LivingEntity) entity)
                    .findFirst()
                    .ifPresent(target -> target.damage(12.0, player));
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_HURT, 1f, 0.9f);
            }
            case "shield_wall" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 120, 0
                ));
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.0f);
            }
            case "fortify" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 200, 1
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.ABSORPTION, 200, 1
                ));
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.7f, 1.0f);
            }
            case "deflect" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, 200, 0
                ));
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.DAMAGE_RESISTANCE, 200, 0
                ));
                player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1.2f);
            }
            case "mining_boost" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.FAST_DIGGING, 300, 1
                ));
                profile.addXp(10);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_HIT, 0.8f, 1.0f);
            }
            case "crafting_boost" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.LUCK, 300, 1
                ));
                profile.addXp(10);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.8f, 1.1f);
            }
            case "alchemy_boost" -> {
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.REGENERATION, 120, 0
                ));
                profile.addXp(15);
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.8f, 1.0f);
            }
            default -> player.sendMessage(Text.mm("<gray>Kein Effekt definiert."));
        }
    }

    private void handleQuest(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg quest <accept|abandon|list>"));
            return;
        }
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        switch (args[1].toLowerCase()) {
            case "list" -> plugin.guiManager().openQuestList(player);
            case "abandon" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest abandon <id>"));
                    return;
                }
                String questId = args[2];
                profile.activeQuests().remove(questId);
                player.sendMessage(Text.mm("<yellow>Quest abgebrochen: " + questId));
            }
            case "complete" -> {
                if (args.length < 3) {
                    player.sendMessage(Text.mm("<gray>/rpg quest complete <id>"));
                    return;
                }
                String questId = args[2];
                var quest = plugin.questManager().getQuest(questId);
                var progress = profile.activeQuests().get(questId);
                if (quest == null || progress == null) {
                    player.sendMessage(Text.mm("<red>Quest nicht aktiv."));
                    return;
                }
                if (!plugin.completeQuestIfReady(player, quest, progress)) {
                    player.sendMessage(Text.mm("<yellow>Quest noch nicht abgeschlossen."));
                }
            }
            default -> player.sendMessage(Text.mm("<gray>/rpg quest <list|abandon|complete>"));
        }
    }

    private void handleRespec(Player player) {
        PlayerProfile profile = plugin.playerDataManager().getProfile(player);
        profile.learnedSkills().clear();
        profile.setSkillPoints(profile.level() * 2);
        profile.stats().replaceAll((stat, value) -> 5);
        profile.applyAttributes(player);
        player.sendMessage(Text.mm("<green>Respec durchgeführt. Skillpunkte zurückgesetzt."));
    }

    private void handleClass(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
            return;
        }
        if (args[1].equalsIgnoreCase("list")) {
            player.sendMessage(Text.mm("<yellow>Klassen: " + plugin.classManager().classes().keySet()));
            return;
        }
        if (args[1].equalsIgnoreCase("choose")) {
            if (args.length < 3) {
                player.sendMessage(Text.mm("<gray>/rpg class choose <id>"));
                return;
            }
            String id = args[2];
            ClassDefinition definition = plugin.classManager().getClass(id);
            if (definition == null) {
                player.sendMessage(Text.mm("<red>Unbekannte Klasse."));
                return;
            }
            PlayerProfile profile = plugin.playerDataManager().getProfile(player);
            profile.setClassId(id);
            for (String skill : definition.startSkills()) {
                profile.learnedSkills().put(skill, 1);
            }
            player.sendMessage(Text.mm("<green>Klasse gewählt: " + definition.name()));
            return;
        }
        player.sendMessage(Text.mm("<gray>/rpg class <list|choose>"));
    }
}
