package dev.qitzblue.prismatic.listener;

import dev.qitzblue.prismatic.PrismaticPlugin;
import dev.qitzblue.prismatic.item.PrismaticItems;
import org.bukkit.Keyed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * A vanilla recipe can only match on material, so the registered recipe is the
 * trigger and this is the actual gate: the result only survives if the grid
 * really holds a tagged Prismatic and a tagged Prismatic Key.
 */
public final class CraftListener implements Listener {

    private final PrismaticPlugin plugin;

    public CraftListener(PrismaticPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        Recipe recipe = event.getRecipe();

        boolean isUpgrade = recipe instanceof Keyed keyed
                && keyed.getKey().equals(plugin.upgradeRecipeKey());

        if (isUpgrade) {
            event.getInventory().setResult(validUpgrade(matrix) ? plugin.items().star(true) : null);
            return;
        }

        // Any other recipe that would consume a Prismatic item is refused outright.
        for (ItemStack stack : matrix) {
            if (plugin.items().isPrismatic(stack)) {
                event.getInventory().setResult(null);
                return;
            }
        }
    }

    /** A beacon would happily eat the Prismatic star, since it is still a Nether Star. */
    @EventHandler(ignoreCancelled = true)
    public void onBeaconClick(InventoryClickEvent event) {
        if (event.getInventory().getType() != InventoryType.BEACON) return;
        if (plugin.items().isPrismatic(event.getCurrentItem())
                || plugin.items().isPrismatic(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    private boolean validUpgrade(ItemStack[] matrix) {
        int stars = 0;
        int keys = 0;
        for (ItemStack stack : matrix) {
            if (stack == null || stack.getType().isAir()) continue;
            String id = plugin.items().idOf(stack);
            if (PrismaticItems.STAR.equals(id)) {
                stars++;
            } else if (PrismaticItems.KEY.equals(id)) {
                keys++;
            } else {
                return false;   // a stray item, or an already-awakened star
            }
        }
        return stars == 1 && keys == 1;
    }
}
