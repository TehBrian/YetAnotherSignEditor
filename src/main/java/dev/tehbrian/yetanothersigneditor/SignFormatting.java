package dev.tehbrian.yetanothersigneditor;

import dev.tehbrian.yetanothersigneditor.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

public final class SignFormatting {

  private SignFormatting() {
  }

  /**
   * Number of ticks before the server opens the updated sign.
   */
  public static final int MAGIC_NUMBER_OF_TICKS = 2;
  private static final int MAX_LINE_LENGTH = 384;

  private static Component format(final Component line, final User user) {
    if (shouldFormatLegacy(user)) {
      return Format.legacy(line);
    } else if (shouldFormatMiniMessage(user)) {
      return Format.miniMessage(line);
    } else {
      return Format.plain(line);
    }
  }

  public static List<Component> format(final List<Component> lines, final User user) {
    return lines.stream().map(line -> format(line, user)).toList();
  }

  public static String unformatRaw(final Component line, final User user) {
    String newLine;

    if (shouldFormatLegacy(user)) {
      newLine = Format.serializeLegacy(line);
    } else if (shouldFormatMiniMessage(user)) {
      newLine = Format.serializeMiniMessage(line);
    } else {
      newLine = Format.serializePlain(line);
    }

    // truncate text if it's so long that the server will kick the player.
    if (newLine.length() > MAX_LINE_LENGTH) {
      newLine = newLine.substring(0, MAX_LINE_LENGTH);
    }

    return newLine;
  }

  public static Component unformat(final Component line, final User user) {
    return Format.plain(unformatRaw(line, user));
  }

  public static List<Component> unformat(final List<Component> lines, final User user) {
    return lines.stream().map(line -> unformat(line, user)).toList();
  }

  public static List<Component> unformatLines(final Sign sign, final Side side, final User user) {
    if (shouldFormatMiniMessage(user)) { // try pdc.
      final List<Component> mm = NativePersistence.mmFromPdc(sign, side);
      if (mm != null) {
        return mm;
      }
    }

    return unformat(sign.getSide(side).lines(), user);
  }

  private static boolean shouldFormat(final User user, final Player player) {
    return player != null
        && user.formatEnabled()
        && player.hasPermission(Permission.FORMAT);
  }

  public static boolean shouldFormat(final User user) {
    final Player player = user.getPlayer();
    return shouldFormat(user, player);
  }

  private static boolean shouldFormatLegacy(final User user) {
    final Player player = user.getPlayer();
    return shouldFormat(user, player)
        && user.formattingType() == User.FormattingType.LEGACY
        && player.hasPermission(Permission.LEGACY);
  }

  public static boolean shouldFormatMiniMessage(final User user) {
    final Player player = user.getPlayer();
    return shouldFormat(user, player)
        && user.formattingType() == User.FormattingType.MINIMESSAGE
        && player.hasPermission(Permission.MINIMESSAGE);
  }

  public static void lines(final SignChangeEvent event, final List<Component> lines) {
    for (int i = 0; i < lines.size(); i++) {
      event.line(i, lines.get(i));
    }
  }

  public static void lines(final SignSide signSide, final List<Component> lines) {
    for (int i = 0; i < lines.size(); i++) {
      signSide.line(i, lines.get(i));
    }
  }

}
