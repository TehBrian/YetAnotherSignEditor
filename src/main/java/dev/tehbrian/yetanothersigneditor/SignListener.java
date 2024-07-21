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

import java.util.List;

import static dev.tehbrian.yetanothersigneditor.SignFormatting.MAGIC_NUMBER_OF_TICKS;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.format;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.mmToPdc;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.shouldFormat;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.shouldFormatMiniMessage;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.unformatLines;

public final class SignListener implements Listener {

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

  /**
   * Unformats text on sign open.
   */
  @EventHandler(ignoreCancelled = true)
  public void onSignOpen(final PlayerOpenSignEvent event) {
    // if the cause is plugin, assume we initiated the open and that the sign text has been serialized.
    // if the cause is interact, unformat the sign text and re-open.
    if (event.getCause() != PlayerOpenSignEvent.Cause.INTERACT) {
      return;
    }

    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!shouldFormat(user)) {
      return;
    }

    event.setCancelled(true);

    final Sign sign = event.getSign();
    final Side side = event.getSide();
    final SignSide signSide = sign.getSide(side);

    final List<Component> newLines = unformatLines(sign, side, user);
    for (int i = 0; i < newLines.size(); i++) {
      signSide.line(i, newLines.get(i));
    }
    sign.update();

    this.yetAnotherSignEditor.getServer().getScheduler().runTaskLater(
        this.yetAnotherSignEditor,
        () -> player.openSign(sign, side),
        MAGIC_NUMBER_OF_TICKS
    );
  }

  /**
   * Formats text on sign change.
   */
  @EventHandler
  public void onSignChange(final SignChangeEvent event) {
    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!shouldFormat(user)) {
      return;
    }

    if (shouldFormatMiniMessage(user)) { // store mm to pdc.
      final Sign sign = (Sign) event.getBlock().getState();
      mmToPdc(sign, event.getSide(), event.lines());
      sign.update(); // important! pdc is only stored to snapshot of state.
    }

    final List<Component> newLines = format(event.lines(), user);
    for (int i = 0; i < newLines.size(); i++) {
      event.line(i, newLines.get(i));
    }
  }

}
