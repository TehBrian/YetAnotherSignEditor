package xyz.tehbrian.yetanothersigneditor;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class FormatUtil {

    private FormatUtil() {
    }

    public static @NonNull Component reverseLegacy(final @NonNull Component component) {
        return PlainTextComponentSerializer.plainText().deserialize(LegacyComponentSerializer.legacyAmpersand().serialize(component));
    }

    public static @NonNull Component legacy(final @NonNull Component component) {
        return legacy(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static @NonNull Component legacy(final @NonNull String string) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
    }

    public static @NonNull Component reverseMiniMessage(final @NonNull Component component) {
        return PlainTextComponentSerializer.plainText().deserialize(MiniMessage.get().serialize(component));
    }

    public static @NonNull Component miniMessage(final @NonNull Component component) {
        return miniMessage(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public static @NonNull Component miniMessage(final @NonNull String string) {
        return MiniMessage.get().parse(string);
    }

    public static @NonNull Component plain(final @NonNull String string) {
        return PlainTextComponentSerializer.plainText().deserialize(string);
    }

}
