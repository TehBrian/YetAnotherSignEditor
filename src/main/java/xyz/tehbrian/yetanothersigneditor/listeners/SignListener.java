package xyz.tehbrian.yetanothersigneditor.listeners;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.yetanothersigneditor.FormatUtil;
import xyz.tehbrian.yetanothersigneditor.Constants;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;

import java.util.List;

/**
 * Listens for sign-related events.
 */
public final class SignListener implements Listener {

    private final YetAnotherSignEditor yetAnotherSignEditor;
    private final UserService userService;
    private final SpigotRestrictionHelper restrictionHelper;

    /**
     * @param yetAnotherSignEditor injected
     * @param userService          injected
     * @param restrictionHelper    injected
     */
    @Inject
    public SignListener(
            final @NonNull YetAnotherSignEditor yetAnotherSignEditor,
            final @NonNull UserService userService,
            final @NonNull SpigotRestrictionHelper restrictionHelper
    ) {
        this.yetAnotherSignEditor = yetAnotherSignEditor;
        this.userService = userService;
        this.restrictionHelper = restrictionHelper;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (!event.getPlayer().hasPermission(Constants.Permissions.EDIT)
                || !this.userService.getUser(player).editEnabled()) {
            return;
        }

        if (!(Tag.SIGNS.isTagged(player.getInventory().getItemInMainHand().getType()))
                || event.getAction() != Action.RIGHT_CLICK_BLOCK
                || event.getHand() != EquipmentSlot.HAND
                || player.getGameMode() == GameMode.ADVENTURE
                || player.isSneaking()) {
            return;
        }

        final @Nullable Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        final BlockState blockState = clickedBlock.getState();

        if (!this.restrictionHelper.checkRestrictions(player, clickedBlock.getLocation(), ActionType.ALL)) {
            return;
        }

        if (!(blockState instanceof final Sign sign)) {
            return;
        }

        final User user = this.userService.getUser(player);

        final List<Component> lines = sign.lines();
        for (int i = 0; i < lines.size(); i++) {
            if (user.formattingType() == User.FormattingType.LEGACY && player.hasPermission(Constants.Permissions.LEGACY)) {
                sign.line(i, FormatUtil.reverseLegacy(lines.get(i)));
            } else if (user.formattingType() == User.FormattingType.MINI_MESSAGE && player.hasPermission(Constants.Permissions.MINI_MESSAGE)) {
                sign.line(i, FormatUtil.reverseMiniMessage(lines.get(i)));
            }
        }

        sign.update();

        Bukkit.getScheduler().scheduleSyncDelayedTask(
                this.yetAnotherSignEditor,
                () -> player.openSign(sign),
                2 // magic number so that bukkit loads the updated sign
        );

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(final SignChangeEvent event) {
        final Player player = event.getPlayer();
        final User user = this.userService.getUser(player);

        if (!event.getPlayer().hasPermission(Constants.Permissions.COLOR)
                || !user.colorEnabled()) {
            return;
        }

        final List<Component> lines = event.lines();
        for (int i = 0; i < lines.size(); i++) {
            if (user.formattingType() == User.FormattingType.LEGACY && player.hasPermission(Constants.Permissions.LEGACY)) {
                event.line(i, FormatUtil.legacy(lines.get(i)));
            } else if (user.formattingType() == User.FormattingType.MINI_MESSAGE && player.hasPermission(Constants.Permissions.MINI_MESSAGE)) {
                event.line(i, FormatUtil.miniMessage(lines.get(i)));
            }
        }
    }

}
