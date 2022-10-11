package xyz.tehbrian.yetanothersigneditor.listener;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;
import xyz.tehbrian.yetanothersigneditor.util.Format;
import xyz.tehbrian.yetanothersigneditor.util.Permissions;

import java.util.List;

/**
 * Allows players to format sign text on sign change.
 */
public final class SignFormatListener implements Listener {

  private final UserService userService;

  @Inject
  public SignFormatListener(
      final UserService userService
  ) {
    this.userService = userService;
  }

  @EventHandler
  public void onSignChange(final SignChangeEvent event) {
    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!user.formatEnabled() || !player.hasPermission(Permissions.FORMAT)) {
      return;
    }

    final List<Component> lines = event.lines();
    for (int i = 0; i < lines.size(); i++) {
      if (user.formattingType() == User.FormattingType.LEGACY && player.hasPermission(Permissions.LEGACY)) {
        event.line(i, Format.legacy(lines.get(i)));
      } else if (user.formattingType() == User.FormattingType.MINIMESSAGE && player.hasPermission(Permissions.MINIMESSAGE)) {
        event.line(i, Format.miniMessage(lines.get(i)));
      }
    }
  }

}
