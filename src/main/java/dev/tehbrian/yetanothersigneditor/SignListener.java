package dev.tehbrian.yetanothersigneditor;

import com.google.inject.Inject;
import dev.tehbrian.yetanothersigneditor.user.User;
import dev.tehbrian.yetanothersigneditor.user.UserService;
import io.papermc.paper.event.player.PlayerOpenSignEvent;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import static dev.tehbrian.yetanothersigneditor.format.NativePersistence.handlePersistence;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.MAGIC_NUMBER_OF_TICKS;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.format;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.lines;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.unformatSignLines;
import static dev.tehbrian.yetanothersigneditor.format.UserFormatting.shouldFormat;

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

		lines(signSide, unformatSignLines(sign, side, user));
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
		final Sign sign = (Sign) event.getBlock().getState();

		handlePersistence(sign, event.getSide(), event.lines(), user);

		if (shouldFormat(user)) {
			lines(event, format(event.lines(), user));
		}
	}

}
