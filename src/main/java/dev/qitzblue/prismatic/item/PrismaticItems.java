package dev.qitzblue.prismatic.item;

import dev.qitzblue.prismatic.util.Cfg;
import dev.qitzblue.prismatic.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/** Builds every Prismatic item and identifies them again afterwards. */
public final class PrismaticItems {

    public static final String STAR     = "star";
    public static final String AWAKENED = "star_awakened";
    public static final String KEY      = "key";
    public static final String PICKAXE  = "haste_pickaxe";
    public static final String ELYTRA   = "dash_elytra";
    public static final String ROD      = "rainfall_rod";

    private static final String NAME_GRADIENT = "<gradient:#FFB7E5:#A9D6FF>";

    private final JavaPlugin plugin;
    private final Cfg cfg;
    private final NamespacedKey idKey;

    public PrismaticItems(JavaPlugin plugin, Cfg cfg) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.idKey = new NamespacedKey(plugin, "item_id");
    }

    // ------------------------------------------------------------- identity

    /** The Prismatic id stored on this stack, or null if it is an ordinary item. */
    public String idOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return null;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public boolean is(ItemStack stack, String id) {
        return id.equals(idOf(stack));
    }

    public boolean isStar(ItemStack stack) {
        String id = idOf(stack);
        return STAR.equals(id) || AWAKENED.equals(id);
    }

    public boolean isPrismatic(ItemStack stack) {
        return idOf(stack) != null;
    }

    // ------------------------------------------------------------- building

    public ItemStack star(boolean awakened) {
        ItemStack stack = new ItemStack(Material.NETHER_STAR);
        stack.editMeta(meta -> {
            meta.displayName(Text.mm(NAME_GRADIENT + "<bold>Prismatic</bold></gradient>"
                    + (awakened ? " <white>✦</white>" : "")));
            meta.lore(awakened ? List.of(
                    Text.mm("<gray>Right-click <dark_gray>·</dark_gray> <white>shockwave"),
                    Text.mm("<gray>Shift-right-click <dark_gray>·</dark_gray> <white>compendium"),
                    Component.empty(),
                    Text.mm("<#A9D6FF><italic>The key turned. It is awake.</italic>")
            ) : List.of(
                    Text.mm("<gray>Right-click <dark_gray>·</dark_gray> <white>shockwave"),
                    Component.empty(),
                    Text.mm("<dark_gray><italic>Something is still locked inside.</italic>")
            ));
            meta.setEnchantmentGlintOverride(awakened);
            meta.setMaxStackSize(1);
            tag(meta, awakened ? AWAKENED : STAR);
            model(meta, awakened ? "prismatic_star_awakened" : "prismatic_star");
        });
        return stack;
    }

    public ItemStack key() {
        ItemStack stack = new ItemStack(cfg.keyMaterial);
        stack.editMeta(meta -> {
            meta.displayName(Text.mm(NAME_GRADIENT + "<bold>Prismatic Key</bold></gradient>"));
            meta.lore(List.of(
                    Text.mm("<gray>Combine with <white>Prismatic</white> to awaken it."),
                    Component.empty(),
                    Text.mm("<dark_gray><italic>Cut to fit a lock that sings.</italic>")
            ));
            meta.setEnchantmentGlintOverride(true);
            tag(meta, KEY);
            model(meta, "prismatic_key");
        });
        return stack;
    }

    public ItemStack hastePickaxe() {
        ItemStack stack = new ItemStack(cfg.pickaxeMaterial);
        stack.editMeta(meta -> {
            meta.displayName(Text.mm("<#FFB7E5><bold>Oblivion</bold>"));
            meta.lore(List.of(Text.mm("<gray>Drawn from the Prismatic.")));
            tag(meta, PICKAXE);
        });
        return stack;
    }

    public ItemStack dashElytra() {
        ItemStack stack = new ItemStack(Material.ELYTRA);
        stack.editMeta(meta -> {
            meta.displayName(Text.mm("<#CBC3F5><bold>Ely-Boost</bold>"));
            meta.lore(List.of(Text.mm("<gray>A handful of flights, no more.")));
            if (meta instanceof Damageable damageable) {
                int max = Material.ELYTRA.getMaxDurability();
                damageable.setDamage(Math.max(0, max - cfg.elytraDurability));
            }
            tag(meta, ELYTRA);
        });
        return stack;
    }

    public ItemStack rainfallRod() {
        ItemStack stack = new ItemStack(Material.FISHING_ROD);
        stack.editMeta(meta -> {
            meta.displayName(Text.mm("<#A9D6FF><bold>Rainfall</bold>"));
            meta.lore(List.of(
                    Text.mm("<gray>Right-click <dark_gray>·</dark_gray> <white>call the rain"),
                    Component.empty(),
                    Text.mm("<dark_gray><italic>The sky owes you one.</italic>")
            ));
            meta.setEnchantmentGlintOverride(true);
            meta.setMaxStackSize(1);
            tag(meta, ROD);
        });
        return stack;
    }

    // ------------------------------------------------------------- internals

    private void tag(ItemMeta meta, String id) {
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
    }

    private void model(ItemMeta meta, String modelName) {
        if (!cfg.customTextures) return;
        NamespacedKey key = NamespacedKey.fromString(cfg.textureNamespace + ":" + modelName);
        if (key == null) {
            plugin.getLogger().warning("Bad texture namespace '" + cfg.textureNamespace
                    + "' - leaving items on their vanilla models.");
            return;
        }
        meta.setItemModel(key);
    }
}
