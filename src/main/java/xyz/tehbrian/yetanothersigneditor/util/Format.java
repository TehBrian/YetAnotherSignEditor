package xyz.tehbrian.yetanothersigneditor.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class Format {

  private Format() {
  }

  private static String strippedString(final Component component) {
    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  public static Component reverseLegacy(final Component component) {
    return plain(LegacyComponentSerializer.legacyAmpersand().serialize(component));
  }

  public static Component reverseMiniMessage(final Component component) {
    return plain(MiniMessage.miniMessage().serialize(component));
  }

  public static Component legacy(final Component component) {
    return legacy(strippedString(component));
  }

  public static Component legacy(final String string) {
    return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
  }

  public static Component miniMessage(final Component component) {
    return miniMessage(strippedString(component));
  }

  public static Component miniMessage(final String string) {
    return MiniMessage.miniMessage().deserialize(string);
  }

  public static Component plain(final Component component) {
    return plain(strippedString(component));
  }

  public static Component plain(final String string) {
    return PlainTextComponentSerializer.plainText().deserialize(string);
  }

}
