package dev.qitzblue.prismatic.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Text {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Text() {}

    /** MiniMessage, with the italics Minecraft forces onto item names/lore switched off. */
    public static Component mm(String input) {
        return MM.deserialize(input).decoration(TextDecoration.ITALIC, false);
    }

    /** MiniMessage for chat and books, where the italic default is fine to leave alone. */
    public static Component raw(String input) {
        return MM.deserialize(input);
    }
}
