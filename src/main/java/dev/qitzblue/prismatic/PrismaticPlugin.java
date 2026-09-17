package dev.qitzblue.prismatic;

import dev.qitzblue.prismatic.ability.Abilities;
import dev.qitzblue.prismatic.ability.AbilityBook;
import dev.qitzblue.prismatic.ability.Shockwave;
import dev.qitzblue.prismatic.command.AbilityCommand;
import dev.qitzblue.prismatic.command.PrismaticCommand;
import dev.qitzblue.prismatic.item.PrismaticItems;
import dev.qitzblue.prismatic.listener.CraftListener;
import dev.qitzblue.prismatic.listener.InteractListener;
import dev.qitzblue.prismatic.listener.PlayerStateListener;
import dev.qitzblue.prismatic.util.Cfg;
import dev.qitzblue.prismatic.util.Cooldowns;
import dev.qitzblue.prismatic.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PrismaticPlugin extends JavaPlugin {

    private Cfg cfg;
    private PrismaticItems items;
    private Cooldowns cooldowns;
    private Shockwave shockwave;
    private Abilities abilities;
    private AbilityBook book;

    private NamespacedKey upgradeRecipeKey;

    /** When each player's book click window closes. */
    private final Map<UUID, Long> compendiumOpenUntil = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cfg = new Cfg(this);
        items = new PrismaticItems(this, cfg);
        cooldowns = new Cooldowns();
        shockwave = new Shockwave(this, cfg);
        abilities = new Abilities(this, cfg, items);
        book = new AbilityBook();
        upgradeRecipeKey = new NamespacedKey(this, "prismatic_upgrade");

        registerRecipe();

        getServer().getPluginManager().registerEvents(new InteractListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerStateListener(this), this);

        bind("prismatic", new PrismaticCommand(this));
        bind("prismaticability", new AbilityCommand(this));

        getLogger().info("Prismatic ready.");
    }

    @Override
    public void onDisable() {
        abilities.restoreAll();
        cooldowns.clear();
    }

    public void reload() {
        reloadConfig();
        cfg.load();
        getServer().removeRecipe(upgradeRecipeKey);
        registerRecipe();
    }

    // ------------------------------------------------------------ wiring

    private void bind(String name, CommandExecutor handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().severe("Command '" + name + "' is missing from plugin.yml.");
            return;
        }
        command.setExecutor(handler);
        if (handler instanceof TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    /**
     * Shapeless Nether Star + key material. Vanilla recipes match on material only,
     * so CraftListener re-checks that both items are genuinely Prismatic before the
     * result is allowed to stand.
     */
    private void registerRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(upgradeRecipeKey, items.star(true));
        recipe.addIngredient(Material.NETHER_STAR);
        recipe.addIngredient(cfg.keyMaterial);
        getServer().addRecipe(recipe);
    }

    // --------------------------------------------------------- behaviour

    public void fireShockwave(Player player) {
        if (!player.hasPermission("prismatic.use")) {
            player.sendMessage(prefixed(cfg.noPermission));
            return;
        }
        if (charging(player, "shockwave")) return;
        shockwave.fire(player);
        cooldowns.start(player, "shockwave", cfg.shockwaveCooldown);
    }

    public void openCompendium(Player player) {
        if (!player.hasPermission("prismatic.use")) {
            player.sendMessage(prefixed(cfg.noPermission));
            return;
        }
        if (charging(player, "compendium")) return;
        compendiumOpenUntil.put(player.getUniqueId(),
                System.currentTimeMillis() + cfg.clickWindow * 1000L);
        book.open(player);
        cooldowns.start(player, "compendium", cfg.compendiumCooldown);
    }

    public boolean hasOpenCompendium(Player player) {
        Long until = compendiumOpenUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public void runAbility(Player player, String ability) {
        if (!AbilityCommand.known(ability)) return;
        if (charging(player, ability)) return;

        switch (ability) {
            case AbilityBook.VANISH -> {
                abilities.vanish(player);
                cooldowns.start(player, ability, cfg.vanishCooldown);
            }
            case AbilityBook.PICKAXE -> {
                abilities.hastePickaxe(player);
                cooldowns.start(player, ability, cfg.pickaxeCooldown);
            }
            case AbilityBook.ELYTRA -> {
                abilities.dashElytra(player);
                cooldowns.start(player, ability, cfg.elytraCooldown);
            }
            case AbilityBook.ROD -> {
                abilities.rainfallRod(player);
                cooldowns.start(player, ability, cfg.rodCooldown);
            }
            case AbilityBook.THUNDERSTEP -> {
                abilities.thunderstep(player);
                cooldowns.start(player, ability, cfg.thunderCooldown);
            }
            default -> { }
        }
        compendiumOpenUntil.remove(player.getUniqueId());
    }

    public void useRainfallRod(Player player, ItemStack rod) {
        if (!player.hasPermission("prismatic.use")) {
            player.sendMessage(prefixed(cfg.noPermission));
            return;
        }
        if (charging(player, "rainfall")) return;
        abilities.callRain(player);
        cooldowns.start(player, "rainfall", cfg.rodCooldown);
        if (cfg.rodConsume) {
            rod.setAmount(rod.getAmount() - 1);
        }
    }

    /** True when the ability is still cooling down; tells the player how long is left. */
    private boolean charging(Player player, String ability) {
        long left = cooldowns.remaining(player, ability);
        if (left == 0) return false;
        player.sendActionBar(Text.raw(cfg.onCooldown.replace("<time>", String.valueOf(left))));
        return true;
    }

    public Component prefixed(String miniMessage) {
        return Text.raw(cfg.prefix + miniMessage);
    }

    // ---------------------------------------------------------- accessors

    public Cfg cfg()                  { return cfg; }
    public PrismaticItems items()     { return items; }
    public Cooldowns cooldowns()      { return cooldowns; }
    public Abilities abilities()      { return abilities; }
    public NamespacedKey upgradeRecipeKey() { return upgradeRecipeKey; }
}
