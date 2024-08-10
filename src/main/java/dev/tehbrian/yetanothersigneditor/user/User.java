package dev.tehbrian.yetanothersigneditor.user;

import dev.tehbrian.yetanothersigneditor.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class User {

	private final UUID uuid;

	private boolean formattingEnabled;
	private User.FormattingType formattingType = FormattingType.LEGACY;

	public User(final UUID uuid) {
		this.uuid = uuid;

		final @Nullable Player player = this.getPlayer();
		if (player != null) {
			this.formattingEnabled = player.hasPermission(Permission.FORMAT);
		}
	}

	public @Nullable Player getPlayer() {
		return Bukkit.getPlayer(this.uuid);
	}

	public boolean formattingEnabled() {
		return this.formattingEnabled;
	}

	public void formattingEnabled(final boolean formatEnabled) {
		this.formattingEnabled = formatEnabled;
	}

	public boolean toggleFormattingEnabled() {
		this.formattingEnabled(!this.formattingEnabled());
		return this.formattingEnabled();
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
