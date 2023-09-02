package dev.tehbrian.yetanothersigneditor;

import com.google.inject.Inject;
import dev.tehbrian.yetanothersigneditor.user.User;
import dev.tehbrian.yetanothersigneditor.user.UserService;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.List;

public final class SignListener implements Listener {

  private static final int MAX_LINE_LENGTH = 384;
  /**
   * Magic number of ticks that need to pass so that the server opens the updated sign.
   */
  private static final int STUPID_MAGIC_NUMBER_OF_TICKS = 2;

  private final YetAnotherSignEditor yetAnotherSignEditor;
  private final UserService userService;

  @Inject
  public SignListener(
      final YetAnotherSignEditor yetAnotherSignEditor,
      final UserService userService
  ) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
    this.userService = userService;
  }

  private static List<Component> format(final List<Component> lines, final User user) {
    final List<Component> newLines = new ArrayList<>();
    for (final Component line : lines) {
      if (shouldFormatLegacy(user)) {
        newLines.add(Format.legacy(line));
      } else if (shouldFormatMiniMessage(user)) {
        newLines.add(Format.miniMessage(line));
      } else {
        newLines.add(Format.plain(line));
      }
    }
    return newLines;
  }

  private static List<Component> unformat(final List<Component> lines, final User user) {
    final List<Component> newLines = new ArrayList<>();
    for (final Component line : lines) {
      String newLine;
      if (shouldFormatLegacy(user)) {
        newLine = Format.serializeLegacy(line);
      } else if (shouldFormatMiniMessage(user)) {
        newLine = Format.serializeMiniMessage(line);
      } else {
        newLine = Format.serializePlain(line);
      }

      // truncate text if it's too long else the server will kick the player.
      if (newLine.length() > MAX_LINE_LENGTH) {
        newLine = newLine.substring(0, MAX_LINE_LENGTH);
      }

      newLines.add(Format.plain(newLine));
    }
    return newLines;
  }

  private static boolean shouldFormat(final User user, final Player player) {
    return player != null
        && user.formatEnabled()
        && player.hasPermission(Permission.FORMAT);
  }

  private static boolean shouldFormatLegacy(final User user) {
    final Player player = user.getPlayer();
    return shouldFormat(user, player)
        && user.formattingType() == User.FormattingType.LEGACY
        && player.hasPermission(Permission.LEGACY);
  }

  private static boolean shouldFormatMiniMessage(final User user) {
    final Player player = user.getPlayer();
    return shouldFormat(user, player)
        && user.formattingType() == User.FormattingType.MINIMESSAGE
        && player.hasPermission(Permission.MINIMESSAGE);
  }

  /**
   * De-formats sign text on sign open.
   */
  @EventHandler(ignoreCancelled = true)
  public void onSignOpen(final PlayerOpenSignEvent event) {
    // if the cause is plugin, assume we initiated the open and that the sign text has been serialized.
    // if the cause is interact, serialize the sign text and re-open.
    if (event.getCause() != PlayerOpenSignEvent.Cause.INTERACT) {
      return;
    }

    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!shouldFormat(user, player)) {
      return;
    }

    event.setCancelled(true);

    final Sign sign = event.getSign();
    final Side side = event.getSide();
    final SignSide signSide = sign.getSide(side);

    final List<Component> newLines = unformat(signSide.lines(), user);
    for (int i = 0; i < newLines.size(); i++) {
      signSide.line(i, newLines.get(i));
    }

    sign.update();

    this.yetAnotherSignEditor.getServer().getScheduler().runTaskLater(
        this.yetAnotherSignEditor,
        () -> player.openSign(sign, side),
        STUPID_MAGIC_NUMBER_OF_TICKS
    );
  }

  /**
   * Formats sign text on sign change.
   */
  @EventHandler
  public void onSignChange(final SignChangeEvent event) {
    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!shouldFormat(user, player)) {
      return;
    }

    final List<Component> newLines = format(event.lines(), user);
    for (int i = 0; i < newLines.size(); i++) {
      event.line(i, newLines.get(i));
    }
  }

}
