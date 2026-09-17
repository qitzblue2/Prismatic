package dev.qitzblue.prismatic.command;

import dev.qitzblue.prismatic.PrismaticPlugin;
import dev.qitzblue.prismatic.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PrismaticCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("give", "awakened", "key", "reload");

    private final PrismaticPlugin plugin;

    public PrismaticCommand(PrismaticPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (args.length == 0) {
            usage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            plugin.reload();
            sender.sendMessage(plugin.prefixed("<#A9D6FF>Config reloaded."));
            return true;
        }

        ItemStack stack = switch (sub) {
            case "give"     -> plugin.items().star(false);
            case "awakened" -> plugin.items().star(true);
            case "key"      -> plugin.items().key();
            default         -> null;
        };
        if (stack == null) {
            usage(sender, label);
            return true;
        }

        Player target;
        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sender.sendMessage(plugin.prefixed("<red>No player called <white>" + args[1] + "<red>."));
                return true;
            }
        } else if (sender instanceof Player self) {
            target = self;
        } else {
            sender.sendMessage(plugin.prefixed("<red>Name a player when running this from console."));
            return true;
        }

        target.getInventory().addItem(stack).values()
                .forEach(left -> target.getWorld().dropItemNaturally(target.getLocation(), left));
        sender.sendMessage(plugin.prefixed("<#A9D6FF>Gave <white>" + sub + "<#A9D6FF> to <white>"
                + target.getName() + "<#A9D6FF>."));
        return true;
    }

    private void usage(CommandSender sender, String label) {
        sender.sendMessage(Text.raw("<#FFB7E5>/" + label + " give|awakened|key [player]"));
        sender.sendMessage(Text.raw("<#FFB7E5>/" + label + " reload"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String @NotNull [] args) {
        if (args.length == 1) {
            List<String> out = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(sub);
            }
            return out;
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("reload")) {
            return null;   // let Bukkit fill in online player names
        }
        return List.of();
    }
}
