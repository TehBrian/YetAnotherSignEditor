package dev.tehbrian.yetanothersigneditor;

import dev.tehbrian.yetanothersigneditor.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SignFormatting {

  private static final NamespacedKey FRONT_KEY = Objects.requireNonNull(NamespacedKey.fromString(
      "yase:native-serialized-mm-front"));
  private static final NamespacedKey BACK_KEY = Objects.requireNonNull(NamespacedKey.fromString(
      "yase:native-serialized-mm-back"));

  private static final int MAX_LINE_LENGTH = 384;
  /**
   * Number of ticks before the server opens the updated sign.
   */
  public static final int MAGIC_NUMBER_OF_TICKS = 2;


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

  private static Component unformat(final Component line, final User user) {
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

    return Format.plain(newLine);
  }

  public static List<Component> unformat(final List<Component> lines, final User user) {
    return lines.stream().map(line -> unformat(line, user)).toList();
  }

  public static List<Component> unformatLines(final Sign sign, final Side side, final User user) {
    if (shouldFormatMiniMessage(user)) { // try pdc.
      final List<Component> mm = SignFormatting.mmFromPdc(sign, side);
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

  public static @Nullable List<Component> mmFromPdc(final Sign sign, final Side side) {
    final var pdc = sign.getPersistentDataContainer();
    final @Nullable String data;

    if (side == Side.FRONT && pdc.has(FRONT_KEY)) {
      data = pdc.get(FRONT_KEY, PersistentDataType.STRING);
    } else if (side == Side.BACK && pdc.has(BACK_KEY)) {
      data = pdc.get(BACK_KEY, PersistentDataType.STRING);
    } else {
      return null;
    }

    if (data == null) {
      return null;
    }

    return data.lines().map(Format::plain).toList();
  }

  public static void mmToPdc(final Sign sign, final Side side, final List<Component> lines) {
    final var pdc = sign.getPersistentDataContainer();
    final String data = lines.stream().map(Format::serializePlain).collect(Collectors.joining("\n"));

    if (side == Side.FRONT) {
      pdc.set(FRONT_KEY, PersistentDataType.STRING, data);
    } else {
      pdc.set(BACK_KEY, PersistentDataType.STRING, data);
    }
  }

}
