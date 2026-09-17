package dev.qitzblue.prismatic.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player, per-ability cooldown clock. */
public final class Cooldowns {

    private final Map<UUID, Map<String, Long>> expiry = new HashMap<>();

    /** Seconds still to wait, rounded up. 0 means ready. */
    public long remaining(Player player, String ability) {
        Map<String, Long> mine = expiry.get(player.getUniqueId());
        if (mine == null) return 0;
        Long until = mine.get(ability);
        if (until == null) return 0;
        long left = until - System.currentTimeMillis();
        return left <= 0 ? 0 : (left + 999) / 1000;
    }

    public boolean ready(Player player, String ability) {
        return remaining(player, ability) == 0;
    }

    public void start(Player player, String ability, int seconds) {
        if (seconds <= 0) return;
        expiry.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
              .put(ability, System.currentTimeMillis() + seconds * 1000L);
    }

    public void forget(UUID player) {
        expiry.remove(player);
    }

    public void clear() {
        expiry.clear();
    }
}
