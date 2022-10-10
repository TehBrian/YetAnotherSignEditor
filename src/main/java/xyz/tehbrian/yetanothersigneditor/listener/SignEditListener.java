package xyz.tehbrian.yetanothersigneditor.listener;

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
 * Allows players to open the sign editor on sign interact.
 */
public class SignEditListener implements Listener {

  private final YetAnotherSignEditor yetAnotherSignEditor;
  private final UserService userService;
  private final SpigotRestrictionHelper restrictionHelper;

  @Inject
  public SignEditListener(
      final YetAnotherSignEditor yetAnotherSignEditor,
      final UserService userService,
      final SpigotRestrictionHelper restrictionHelper
  ) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
    this.userService = userService;
    this.restrictionHelper = restrictionHelper;
  }

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
        || !(clickedBlock.getState() instanceof final Sign clickedSign)
        || !this.restrictionHelper.checkRestrictions(player, clickedBlock.getLocation(), ActionType.ALL)) {
      return;
    }

    final List<Component> lines = clickedSign.lines();
    for (int i = 0; i < lines.size(); i++) {
      final Component text = lines.get(i);

      Component formattedText = Format.plain(text);
      if (user.formatEnabled() && player.hasPermission(Permissions.FORMAT)) {
        if (user.formattingType() == User.FormattingType.LEGACY
            && player.hasPermission(Permissions.LEGACY)) {
          formattedText = Format.reverseLegacy(text);
        } else if (user.formattingType() == User.FormattingType.MINIMESSAGE
            && player.hasPermission(Permissions.MINIMESSAGE)) {
          formattedText = Format.reverseMiniMessage(text);
        }
      }

      clickedSign.line(i, formattedText);
    }

    clickedSign.update();

    Bukkit.getScheduler().scheduleSyncDelayedTask(
        this.yetAnotherSignEditor,
        () -> player.openSign(clickedSign),
        2 // magic number so that bukkit loads the updated sign
    );

    event.setCancelled(true);
  }

}
