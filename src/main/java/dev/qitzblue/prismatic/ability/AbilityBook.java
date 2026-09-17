package dev.qitzblue.prismatic.ability;

import dev.qitzblue.prismatic.util.Text;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.util.List;

/** The shift-right-click compendium: three pages of clickable entries. */
public final class AbilityBook {

    public static final String VANISH      = "vanish";
    public static final String PICKAXE     = "pickaxe";
    public static final String ELYTRA      = "elytra";
    public static final String ROD         = "rod";
    public static final String THUNDERSTEP = "thunderstep";

    public void open(Player player) {
        player.openBook(Book.book(
                Text.raw("<gradient:#FFB7E5:#A9D6FF>Prismatic</gradient>"),
                Component.text("Prismatic"),
                List.of(pageOne(), pageTwo(), pageThree())));
    }

    private Component pageOne() {
        return Component.empty()
                .append(heading("Vanish"))
                .append(Text.raw("<dark_gray>  i of iii\n\n"))
                .append(Text.raw("<#6A5A78>Step out of the light\nand let it close over\nyou.\n\n"))
                .append(Text.raw("<dark_gray>Invisible, <#B0629C>15s<dark_gray>.\n\n"))
                .append(entry("Vanish", VANISH,
                        "A burst of end rod light, then nothing where you stood."));
    }

    private Component pageTwo() {
        return Component.empty()
                .append(heading("Toolkit"))
                .append(Text.raw("<dark_gray>  ii of iii\n\n"))
                .append(Text.raw("<#6A5A78>Take one.\n\n"))
                .append(entry("Miner's Edge", PICKAXE,
                        "A pickaxe, and Haste V to swing it with."))
                .append(Component.text("\n"))
                .append(entry("Ely-Boost", ELYTRA,
                        "A bare elytra with a few flights in it, and a shove into the sky."))
                .append(Component.text("\n"))
                .append(entry("Rainfall", ROD,
                        "A fishing rod that calls the rain down when you use it."));
    }

    private Component pageThree() {
        return Component.empty()
                .append(heading("Thunderstep"))
                .append(Text.raw("<dark_gray>  iii of iii\n\n"))
                .append(Text.raw("<#6A5A78>The bolt takes you. You\nget five seconds above\nthe world, and then it\nwants you back.\n\n"))
                .append(entry("Ascend", THUNDERSTEP,
                        "Lightning, then five seconds as a ghost to climb. Bring the mace."));
    }

    private Component heading(String title) {
        return Text.raw("<gradient:#D9539F:#3F8DDB><bold>" + title + "</bold></gradient>\n");
    }

    /** A clickable entry. The command is gated server-side, see AbilityCommand. */
    private Component entry(String label, String ability, String tooltip) {
        Component clickable = Text.raw("<#B0629C>» <underlined>" + label + "</underlined> «")
                .clickEvent(ClickEvent.runCommand("/prismaticability " + ability))
                .hoverEvent(HoverEvent.showText(Text.raw("<#A9D6FF>" + tooltip)));
        // The newline sits outside the entry so it is not part of the click target.
        return Component.empty().append(clickable).append(Component.text("\n"));
    }
}
