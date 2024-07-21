package dev.tehbrian.yetanothersigneditor;

import dev.tehbrian.yetanothersigneditor.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NativePersistence {

  private static final NamespacedKey FRONT_KEY = Objects.requireNonNull(NamespacedKey.fromString(
      "yase:native-serialized-mm-front"));
  private static final NamespacedKey BACK_KEY = Objects.requireNonNull(NamespacedKey.fromString(
      "yase:native-serialized-mm-back"));

  private static String encodeMm(final List<Component> lines) {
    return lines.stream().map(Format::serializePlain).collect(Collectors.joining("\n"));
  }

  private static List<Component> decodeMm(final String raw) {
    return padTo4(raw.lines().map(Format::plain).toList());
  }

  private static @Nullable String rawMmFromPdc(final Sign sign, final Side side) {
    final var pdc = sign.getPersistentDataContainer();
    if (side == Side.FRONT && pdc.has(FRONT_KEY)) {
      return pdc.get(FRONT_KEY, PersistentDataType.STRING);
    } else if (side == Side.BACK && pdc.has(BACK_KEY)) {
      return pdc.get(BACK_KEY, PersistentDataType.STRING);
    } else {
      return null;
    }
  }

  public static @Nullable List<Component> mmFromPdc(final Sign sign, final Side side) {
    final @Nullable String raw = rawMmFromPdc(sign, side);
    if (raw == null) {
      return null;
    }

    return decodeMm(raw);
  }

  public static void mmToPdc(final Sign sign, final Side side, final List<Component> lines) {
    final var pdc = sign.getPersistentDataContainer();
    final String data = encodeMm(lines);

    if (side == Side.FRONT) {
      pdc.set(FRONT_KEY, PersistentDataType.STRING, data);
    } else {
      pdc.set(BACK_KEY, PersistentDataType.STRING, data);
    }
  }

  public static void invalidateMmInPdc(
      final Sign sign,
      final Side side,
      final List<Component> newLines,
      final User user
  ) {
    final var pdc = sign.getPersistentDataContainer();
    final @Nullable String raw = rawMmFromPdc(sign, side);
    if (raw == null) {
      // no stored MM to invalidate! :D
      return;
    }

    // the stored lines formatted by MM.
    final List<Component> oldShit = padTo4(raw.lines().map(Format::miniMessage).toList());

    // the new lines formatted by the user's formatting type.
    final List<Component> newShit = SignFormatting.format(newLines, user);

    if (!oldShit.equals(newShit)) {
      pdc.remove(toPdcKey(side));
    }
  }

  private static NamespacedKey toPdcKey(final Side side) {
    if (side == Side.FRONT) {
      return FRONT_KEY;
    } else {
      return BACK_KEY;
    }
  }

  // "1\n2\n3\n".lines().size() == 3, not 4 :(
  // and since we're storing \n'd strings in PDC rather than a list,
  // we've got to make due.
  private static List<Component> padTo4(List<Component> list) {
    list = new ArrayList<>(list);
    while (list.size() < 4) {
      list.add(Component.empty());
    }
    return list;
  }

}
