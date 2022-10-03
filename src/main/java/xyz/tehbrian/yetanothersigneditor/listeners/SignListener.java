package xyz.tehbrian.yetanothersigneditor.listeners;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;
import xyz.tehbrian.yetanothersigneditor.util.Format;
import xyz.tehbrian.yetanothersigneditor.util.Permissions;

import java.util.List;

/**
 * Listens for sign-related events.
 */
public final class SignListener implements Listener {

  private final YetAnotherSignEditor yetAnotherSignEditor;
  private final UserService userService;
  private final SpigotRestrictionHelper restrictionHelper;

  @Inject
  public SignListener(
      final YetAnotherSignEditor yetAnotherSignEditor,
      final UserService userService,
      final SpigotRestrictionHelper restrictionHelper
  ) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
    this.userService = userService;
    this.restrictionHelper = restrictionHelper;
  }

  /**
   * Opens the sign editor on sign interact.
   *
   * @param event the event
   */
  @EventHandler(ignoreCancelled = true)
  public void onSignInteract(final PlayerInteractEvent event) {
    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!user.editEnabled() || !player.hasPermission(Permissions.EDIT)) {
      return;
    }

    if (!Tag.SIGNS.isTagged(player.getInventory().getItemInMainHand().getType())
        || event.getAction() != Action.RIGHT_CLICK_BLOCK
        || event.getHand() != EquipmentSlot.HAND
        || player.getGameMode() == GameMode.ADVENTURE
        || player.isSneaking()) {
      return;
    }

    final @Nullable Block clickedBlock = event.getClickedBlock();
    if (clickedBlock == null
        || !(clickedBlock.getState() instanceof final Sign sign)
        || !this.restrictionHelper.checkRestrictions(player, clickedBlock.getLocation(), ActionType.ALL)) {
      return;
    }

    final List<Component> lines = sign.lines();
    for (int i = 0; i < lines.size(); i++) {
      final Component text = lines.get(i);

      Component plainText = Format.plain(text);
      if (user.colorEnabled() && player.hasPermission(Permissions.COLOR)) {
        if (user.formattingType() == User.FormattingType.LEGACY
            && player.hasPermission(Permissions.LEGACY)) {
          plainText = Format.reverseLegacy(text);
        } else if (user.formattingType() == User.FormattingType.MINIMESSAGE
            && player.hasPermission(Permissions.MINIMESSAGE)) {
          plainText = Format.reverseMiniMessage(text);
        }
      }

      sign.line(i, plainText);
    }

    sign.update();

    Bukkit.getScheduler().scheduleSyncDelayedTask(
        this.yetAnotherSignEditor,
        () -> player.openSign(sign),
        2 // magic number so that bukkit loads the updated sign
    );

    event.setCancelled(true);
  }

  /**
   * Colors sign text on sign change.
   *
   * @param event the event
   */
  @EventHandler
  public void onSignChange(final SignChangeEvent event) {
    final Player player = event.getPlayer();
    final User user = this.userService.getUser(player);

    if (!user.colorEnabled() || !player.hasPermission(Permissions.COLOR)) {
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
