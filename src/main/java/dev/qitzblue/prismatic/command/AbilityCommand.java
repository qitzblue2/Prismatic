package dev.qitzblue.prismatic.command;

import dev.qitzblue.prismatic.PrismaticPlugin;
import dev.qitzblue.prismatic.ability.AbilityBook;
import dev.qitzblue.prismatic.item.PrismaticItems;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

/**
 * Backs the clickable entries in the compendium. Books can only hand the server a
 * command, so this is gated three ways: the caller must be holding an awakened
 * Prismatic, must have opened the book recently, and pays the ability's cooldown.
 */
public final class AbilityCommand implements CommandExecutor {

    private final PrismaticPlugin plugin;

    public AbilityCommand(PrismaticPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length != 1) return true;

        if (!player.hasPermission("prismatic.use")) {
            player.sendMessage(plugin.prefixed(plugin.cfg().noPermission));
            return true;
        }
        if (!plugin.hasOpenCompendium(player)) {
            player.sendMessage(plugin.prefixed("<#FFB7E5>Open the compendium first."));
            return true;
        }
        if (!holdingAwakened(player)) {
            player.sendMessage(plugin.prefixed("<#FFB7E5>You are not holding the Prismatic."));
            return true;
        }

        plugin.runAbility(player, args[0].toLowerCase(Locale.ROOT));
        return true;
    }

    private boolean holdingAwakened(Player player) {
        for (ItemStack stack : List.of(player.getInventory().getItemInMainHand(),
                                       player.getInventory().getItemInOffHand())) {
            if (plugin.items().is(stack, PrismaticItems.AWAKENED)) return true;
        }
        return false;
    }

    /** Ability ids the book can ask for. */
    public static boolean known(String ability) {
        return switch (ability) {
            case AbilityBook.VANISH, AbilityBook.PICKAXE, AbilityBook.ELYTRA,
                 AbilityBook.ROD, AbilityBook.THUNDERSTEP -> true;
            default -> false;
        };
    }
}
