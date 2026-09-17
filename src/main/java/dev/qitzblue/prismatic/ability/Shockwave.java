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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
                shatterShield(player);
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
     * Breaks the shield outright: it is destroyed in their hand, with the vanilla
     * item-break sound and shards. The use cooldown is applied on top so that
     * pulling a spare shield out of the hotbar does not simply undo it.
     */
    private void shatterShield(Player target) {
        if (cfg.shieldBreak) {
            PlayerInventory inventory = target.getInventory();
            if (inventory.getItemInMainHand().getType() == Material.SHIELD) {
                destroy(target, inventory.getItemInMainHand());
                inventory.setItemInMainHand(null);
            }
            if (inventory.getItemInOffHand().getType() == Material.SHIELD) {
                destroy(target, inventory.getItemInOffHand());
                inventory.setItemInOffHand(null);
            }
        }
        if (cfg.shieldDisable > 0) {
            target.setCooldown(Material.SHIELD, cfg.shieldDisable * 20);
        }
    }

    private void destroy(Player target, ItemStack shield) {
        Location at = target.getLocation().add(0, 1.2, 0);
        at.getWorld().playSound(at, Sound.ENTITY_ITEM_BREAK, 1.0f, 0.9f);
        at.getWorld().playSound(at, Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
        at.getWorld().spawnParticle(Particle.ITEM, at, 18, 0.25, 0.25, 0.25, 0.06, shield.clone());
    }

}
