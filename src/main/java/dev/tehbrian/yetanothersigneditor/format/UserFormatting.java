package dev.tehbrian.yetanothersigneditor.format;

import dev.tehbrian.yetanothersigneditor.Permission;
import dev.tehbrian.yetanothersigneditor.user.User;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class UserFormatting {
	private UserFormatting() {
	}

	private static boolean shouldFormat(final User user, final @Nullable Player player) {
		return player != null
				&& user.formattingEnabled()
				&& player.hasPermission(Permission.FORMAT);
	}

	public static boolean shouldFormat(final User user) {
		final Player player = user.getPlayer();
		return shouldFormat(user, player);
	}

	public static boolean shouldFormatLegacy(final User user) {
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
}
