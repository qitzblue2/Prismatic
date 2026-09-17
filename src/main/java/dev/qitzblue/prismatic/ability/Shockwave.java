package dev.qitzblue.prismatic.ability;

import dev.qitzblue.prismatic.util.Cfg;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Ability one: the end-rod shockwave. */
public final class Shockwave {

    private final JavaPlugin plugin;
    private final Cfg cfg;

    public Shockwave(JavaPlugin plugin, Cfg cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
    }

    public void fire(Player source) {
        Location centre = source.getLocation().clone();
        World world = centre.getWorld();

        world.playSound(centre, Sound.ENTITY_GENERIC_EXPLODE, 0.9f, 1.7f);
        world.playSound(centre, Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 1.9f);
        world.playSound(centre, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 0.8f);

        animate(centre);
        applyEffects(source, centre);
    }

    /** Rings of end rod particles racing outwards, one ring per scheduled step. */
    private void animate(Location centre) {
        World world = centre.getWorld();
        new BukkitRunnable() {
            int ring = 0;

            @Override
            public void run() {
                if (ring >= cfg.rings) {
                    cancel();
                    return;
                }
                double radius = cfg.shockwaveRadius * (ring + 1) / cfg.rings;
                double step = (Math.PI * 2) / cfg.pointsPerRing;
                for (int i = 0; i < cfg.pointsPerRing; i++) {
                    double angle = i * step;
                    Location at = centre.clone().add(
                            Math.cos(angle) * radius, 0.25, Math.sin(angle) * radius);
                    world.spawnParticle(Particle.END_ROD, at, 1, 0, 0, 0, 0);
                }
                ring++;
            }
        }.runTaskTimer(plugin, 0L, cfg.ticksBetweenRings);
    }

    private void applyEffects(Player source, Location centre) {
        double radius = cfg.shockwaveRadius;
        double radiusSq = radius * radius;
        int slowTicks = cfg.slownessDuration * 20;

        for (Entity entity : centre.getWorld().getNearbyEntities(centre, radius, radius, radius)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target.equals(source) && !cfg.hitSelf) continue;
            if (target instanceof Player player && player.getGameMode() == GameMode.SPECTATOR) continue;
            if (target.getLocation().distanceSquared(centre) > radiusSq) continue;

            if (slowTicks > 0) {
                target.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOWNESS, slowTicks, cfg.slownessLevel - 1, false, true, true));
            }
            if (target instanceof Player player) {
                disableShield(player);
            }
            if (cfg.knockback > 0) {
                Vector push = target.getLocation().toVector().subtract(centre.toVector());
                if (push.lengthSquared() < 1.0e-4) push = new Vector(0, 1, 0);
                push.normalize().multiply(cfg.knockback).setY(cfg.knockback * 0.5);
                target.setVelocity(target.getVelocity().add(push));
            }
        }
    }

    /**
     * Puts the shield on its use cooldown, which is exactly how a vanilla axe disable works:
     * the shield greys out and cannot be raised until it runs down.
     */
    private void disableShield(Player target) {
        if (cfg.shieldDisable <= 0) return;
        target.setCooldown(Material.SHIELD, cfg.shieldDisable * 20);
        boolean holding = target.getInventory().getItemInMainHand().getType() == Material.SHIELD
                || target.getInventory().getItemInOffHand().getType() == Material.SHIELD;
        if (holding) {
            target.playSound(target.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.2f);
        }
    }
}
