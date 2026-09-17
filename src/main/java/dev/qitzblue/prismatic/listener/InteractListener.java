package dev.qitzblue.prismatic.listener;

import dev.qitzblue.prismatic.PrismaticPlugin;
import dev.qitzblue.prismatic.item.PrismaticItems;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class InteractListener implements Listener {

    private final PrismaticPlugin plugin;

    public InteractListener(PrismaticPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;   // main hand only, no double fire
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack held = event.getItem();
        String id = plugin.items().idOf(held);
        if (id == null) return;

        switch (id) {
            case PrismaticItems.STAR, PrismaticItems.AWAKENED -> {
                event.setCancelled(true);
                if (event.getPlayer().isSneaking() && PrismaticItems.AWAKENED.equals(id)) {
                    plugin.openCompendium(event.getPlayer());
                } else {
                    plugin.fireShockwave(event.getPlayer());
                }
            }
            case PrismaticItems.ROD -> {
                event.setCancelled(true);                     // never cast the line
                plugin.useRainfallRod(event.getPlayer(), held);
            }
            default -> { }
        }
    }
}
