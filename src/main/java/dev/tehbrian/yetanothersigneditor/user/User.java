package dev.tehbrian.yetanothersigneditor.user;

import dev.tehbrian.yetanothersigneditor.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class User {

	private final UUID uuid;

	private boolean formatEnabled;
	private User.FormattingType formattingType = FormattingType.LEGACY;

	/**
	 * @param uuid the unique identifier of the user
	 */
	public User(final UUID uuid) {
		this.uuid = uuid;
		final Player player = Objects.requireNonNull(this.getPlayer());
		this.formatEnabled = player.hasPermission(Permission.FORMAT);
	}

	public @Nullable Player getPlayer() {
		return Bukkit.getPlayer(this.uuid);
	}

	public boolean formatEnabled() {
		return this.formatEnabled;
	}

	public void formatEnabled(final boolean formatEnabled) {
		this.formatEnabled = formatEnabled;
	}

	public boolean toggleFormatEnabled() {
		this.formatEnabled(!this.formatEnabled());
		return this.formatEnabled();
	}

	public User.FormattingType formattingType() {
		return this.formattingType;
	}

	public void formattingType(final User.FormattingType formattingType) {
		this.formattingType = formattingType;
	}

	public enum FormattingType {
		LEGACY("legacy"),
		MINIMESSAGE("MiniMessage");

		private final String pretty;

		FormattingType(final String pretty) {
			this.pretty = pretty;
		}

		public String pretty() {
			return this.pretty;
		}
	}

}
