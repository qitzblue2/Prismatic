package dev.qitzblue.prismatic.listener;

import dev.qitzblue.prismatic.PrismaticPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerStateListener implements Listener {

    private final PrismaticPlugin plugin;

    public PlayerStateListener(PrismaticPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Someone who logged out mid-ability is put right on the way back in: pulled
     * out of spectator, and handed back the chestplate their Ely-Boost displaced.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (plugin.abilities().isMidThunderstep(event.getPlayer())) {
            plugin.abilities().restore(event.getPlayer());
        }
        if (plugin.abilities().isMidFlight(event.getPlayer())) {
            plugin.abilities().endFlight(event.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.abilities().restore(event.getPlayer());
        plugin.abilities().endFlight(event.getPlayer());
        plugin.cooldowns().forget(event.getPlayer().getUniqueId());
    }
}
