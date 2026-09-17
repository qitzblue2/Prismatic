package dev.qitzblue.prismatic.ability;

import dev.qitzblue.prismatic.item.PrismaticItems;
import dev.qitzblue.prismatic.util.Cfg;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Everything the compendium can hand out. */
public final class Abilities {

    private final JavaPlugin plugin;
    private final Cfg cfg;
    private final PrismaticItems items;

    /** Players currently mid-Thunderstep, and the game mode owed back to them. */
    private final Map<UUID, GameMode> owedGameMode = new HashMap<>();
    private final File stateFile;

    public Abilities(JavaPlugin plugin, Cfg cfg, PrismaticItems items) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.items = items;
        this.stateFile = new File(plugin.getDataFolder(), "thunderstep-state.yml");
        loadState();
    }

    // ----------------------------------------------------------- page one

    public void vanish(Player player) {
        Location at = player.getLocation();
        puff(at.clone().add(0, 1, 0), 60, 0.45);
        player.getWorld().playSound(at, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 1.0f, 1.1f);
        player.getWorld().playSound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8f, 1.4f);
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, cfg.vanishDuration * 20, 0, false, false, true));
    }

    // ----------------------------------------------------------- page two

    public void hastePickaxe(Player player) {
        give(player, items.hastePickaxe());
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.HASTE, cfg.hasteDuration * 20, cfg.hasteLevel - 1, false, true, true));
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.8f, 1.5f);
    }

    public void dashElytra(Player player) {
        ItemStack elytra = items.dashElytra();
        ItemStack chest = player.getInventory().getChestplate();
        if (chest == null || chest.getType().isAir()) {
            player.getInventory().setChestplate(elytra);
        } else {
            give(player, elytra);
        }

        player.setVelocity(player.getLocation().getDirection()
                .multiply(cfg.launchForward)
                .setY(cfg.launchUp));
        puff(player.getLocation(), 30, 0.3);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.3f);

        // Gliding only sticks once the player is actually off the ground.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || player.isOnGround()) return;
                ItemStack worn = player.getInventory().getChestplate();
                if (worn != null && items.is(worn, PrismaticItems.ELYTRA)) {
                    player.setGliding(true);
                }
            }
        }.runTaskLater(plugin, 3L);
    }

    public void rainfallRod(Player player) {
        give(player, items.rainfallRod());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DOLPHIN_SPLASH, 0.8f, 1.4f);
    }

    /** Fired by right-clicking the Rainfall rod itself, not by the compendium. */
    public void callRain(Player player) {
        World world = player.getWorld();
        world.setStorm(true);
        world.setThundering(cfg.rodThunder);
        world.setWeatherDuration(cfg.rainDuration * 20);
        if (cfg.rodThunder) {
            world.setThunderDuration(cfg.rainDuration * 20);
        }
        world.playSound(player.getLocation(), Sound.WEATHER_RAIN, 1.0f, 1.0f);
        puff(player.getLocation().add(0, 1, 0), 40, 0.4);
    }

    // --------------------------------------------------------- page three

    public void thunderstep(Player player) {
        World world = player.getWorld();
        Location at = player.getLocation();

        if (cfg.lightningDamages) {
            world.strikeLightning(at);
        } else {
            world.strikeLightningEffect(at);
        }
        puff(at, 50, 0.5);

        owedGameMode.put(player.getUniqueId(), player.getGameMode());
        saveState();
        player.setGameMode(GameMode.SPECTATOR);

        new BukkitRunnable() {
            @Override
            public void run() {
                restore(player);
            }
        }.runTaskLater(plugin, cfg.spectatorDuration * 20L);
    }

    /** Drops the player back out of spectator wherever they floated to. */
    public void restore(Player player) {
        GameMode owed = owedGameMode.remove(player.getUniqueId());
        if (owed == null) return;
        saveState();

        if (!player.isOnline()) return;
        if (player.getGameMode() != GameMode.SPECTATOR) return;

        player.setGameMode(owed);
        player.teleport(safeSpot(player.getLocation()));
        player.setVelocity(new Vector(0, 0, 0));
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 1.0f, 1.2f);
        puff(player.getLocation(), 40, 0.4);
    }

    public void restoreAll() {
        for (UUID id : Map.copyOf(owedGameMode).keySet()) {
            Player player = plugin.getServer().getPlayer(id);
            if (player != null) {
                restore(player);
            }
        }
    }

    /** True if this player still owes a restore - used to catch them on rejoin. */
    public boolean isMidThunderstep(Player player) {
        return owedGameMode.containsKey(player.getUniqueId());
    }

    /**
     * Spectators can sit inside solid blocks. Nudge upward so nobody suffocates
     * the instant they come back to survival.
     */
    private Location safeSpot(Location from) {
        Location spot = from.clone();
        World world = spot.getWorld();
        int ceiling = world.getMaxHeight() - 2;
        for (int i = 0; i < 12 && spot.getBlockY() < ceiling; i++) {
            boolean feet = spot.getBlock().isPassable();
            boolean head = spot.clone().add(0, 1, 0).getBlock().isPassable();
            if (feet && head) break;
            spot.add(0, 1, 0);
        }
        return spot;
    }

    // ------------------------------------------------------------ helpers

    private void puff(Location at, int count, double spread) {
        at.getWorld().spawnParticle(Particle.END_ROD, at, count, spread, spread, spread, 0.02);
    }

    private void give(Player player, ItemStack stack) {
        player.getInventory().addItem(stack).values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    // Persisted so a restart mid-ability cannot strand anyone in spectator.
    private void loadState() {
        if (!stateFile.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(stateFile);
        for (String key : yaml.getKeys(false)) {
            try {
                owedGameMode.put(UUID.fromString(key), GameMode.valueOf(yaml.getString(key, "SURVIVAL")));
            } catch (IllegalArgumentException ignored) {
                // Unparseable entry, drop it rather than fail startup.
            }
        }
    }

    private void saveState() {
        YamlConfiguration yaml = new YamlConfiguration();
        owedGameMode.forEach((id, mode) -> yaml.set(id.toString(), mode.name()));
        try {
            if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
                plugin.getLogger().warning("Could not create the plugin data folder.");
                return;
            }
            yaml.save(stateFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save Thunderstep state: " + e.getMessage());
        }
    }
}
