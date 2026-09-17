package dev.qitzblue.prismatic.util;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/** Typed, re-loadable view over config.yml. */
public final class Cfg {

    private final JavaPlugin plugin;

    public Material keyMaterial;
    public boolean customTextures;
    public String textureNamespace;

    public int shockwaveCooldown;
    public double shockwaveRadius;
    public int slownessLevel;
    public int slownessDuration;
    public boolean shieldBreak;
    public int shieldDisable;
    public double knockback;
    public boolean hitSelf;
    public int rings;
    public int pointsPerRing;
    public int ticksBetweenRings;

    public int compendiumCooldown;
    public int clickWindow;

    public int vanishCooldown, vanishDuration;
    public int pickaxeCooldown, hasteLevel, hasteDuration;
    public Material pickaxeMaterial;
    public int elytraCooldown, elytraDurability;
    public double launchUp, launchForward;
    public boolean returnChestplate;
    public int rodCooldown, rainDuration;
    public boolean rodThunder, rodConsume;
    public int thunderCooldown, spectatorDuration;
    public boolean lightningDamages;

    public String prefix, onCooldown, noPermission;

    public Cfg(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        FileConfiguration c = plugin.getConfig();

        keyMaterial      = material(c.getString("item.key-material"), Material.TRIAL_KEY);
        customTextures   = c.getBoolean("item.custom-textures", true);
        textureNamespace = c.getString("item.texture-namespace", "prismatic");

        shockwaveCooldown = c.getInt("shockwave.cooldown", 20);
        shockwaveRadius   = c.getDouble("shockwave.radius", 6.0);
        slownessLevel     = Math.max(1, c.getInt("shockwave.slowness-level", 3));
        slownessDuration  = c.getInt("shockwave.slowness-duration", 10);
        shieldBreak       = c.getBoolean("shockwave.shield-break", true);
        shieldDisable     = c.getInt("shockwave.shield-disable", 8);
        knockback         = c.getDouble("shockwave.knockback", 0.0);
        hitSelf           = c.getBoolean("shockwave.hit-self", false);
        rings             = Math.max(1, c.getInt("shockwave.rings", 4));
        pointsPerRing     = Math.max(4, c.getInt("shockwave.points-per-ring", 44));
        ticksBetweenRings = Math.max(1, c.getInt("shockwave.ticks-between-rings", 2));

        compendiumCooldown = c.getInt("compendium.cooldown", 0);
        clickWindow        = Math.max(5, c.getInt("compendium.click-window", 60));

        vanishCooldown  = c.getInt("abilities.vanish.cooldown", 60);
        vanishDuration  = c.getInt("abilities.vanish.duration", 15);

        pickaxeCooldown = c.getInt("abilities.haste-pickaxe.cooldown", 90);
        pickaxeMaterial = material(c.getString("abilities.haste-pickaxe.material"), Material.DIAMOND_PICKAXE);
        hasteLevel      = Math.max(1, c.getInt("abilities.haste-pickaxe.haste-level", 5));
        hasteDuration   = c.getInt("abilities.haste-pickaxe.haste-duration", 60);

        elytraCooldown   = c.getInt("abilities.dash-elytra.cooldown", 90);
        elytraDurability = Math.max(1, c.getInt("abilities.dash-elytra.durability", 10));
        launchUp         = c.getDouble("abilities.dash-elytra.launch-up", 1.35);
        launchForward    = c.getDouble("abilities.dash-elytra.launch-forward", 1.05);
        returnChestplate = c.getBoolean("abilities.dash-elytra.return-chestplate", true);

        rodCooldown  = c.getInt("abilities.rainfall-rod.cooldown", 120);
        rainDuration = c.getInt("abilities.rainfall-rod.rain-duration", 300);
        rodThunder   = c.getBoolean("abilities.rainfall-rod.thunder", false);
        rodConsume   = c.getBoolean("abilities.rainfall-rod.consume-on-use", true);

        thunderCooldown   = c.getInt("abilities.thunderstep.cooldown", 120);
        spectatorDuration = Math.max(1, c.getInt("abilities.thunderstep.spectator-duration", 5));
        lightningDamages  = c.getBoolean("abilities.thunderstep.lightning-damages", false);

        prefix       = c.getString("messages.prefix", "");
        onCooldown   = c.getString("messages.on-cooldown", "<#FFB7E5>Still settling: <time>s");
        noPermission = c.getString("messages.no-permission", "<red>You cannot use that.");
    }

    private Material material(String name, Material fallback) {
        if (name == null || name.isBlank()) return fallback;
        Material m = Material.matchMaterial(name.trim().toUpperCase());
        if (m == null) {
            plugin.getLogger().warning("Unknown material '" + name + "', falling back to " + fallback);
            return fallback;
        }
        return m;
    }
}
