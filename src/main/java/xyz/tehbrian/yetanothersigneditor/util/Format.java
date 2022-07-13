package xyz.tehbrian.yetanothersigneditor.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class Format {

    private Format() {
    }

    private static @NonNull String strippedString(final @NonNull Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NonNull Component reverseLegacy(final @NonNull Component component) {
        return plain(LegacyComponentSerializer.legacyAmpersand().serialize(component));
    }

    public static @NonNull Component reverseMiniMessage(final @NonNull Component component) {
        return plain(MiniMessage.miniMessage().serialize(component));
    }

    public static @NonNull Component legacy(final @NonNull Component component) {
        return legacy(strippedString(component));
    }

    public static @NonNull Component legacy(final @NonNull String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static @NonNull Component miniMessage(final @NonNull Component component) {
        return miniMessage(strippedString(component));
    }

    public static @NonNull Component miniMessage(final @NonNull String string) {
        return MiniMessage.miniMessage().deserialize(string);
    }

    public static @NonNull Component plain(final @NonNull Component component) {
        return plain(strippedString(component));
    }

    public static @NonNull Component plain(final @NonNull String string) {
        return PlainTextComponentSerializer.plainText().deserialize(string);
    }

}
