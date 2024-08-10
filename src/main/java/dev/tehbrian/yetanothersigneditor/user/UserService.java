package dev.tehbrian.yetanothersigneditor.user;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UserService {
	private static final NamespacedKey FORMATTING_ENABLED = new NamespacedKey("yase", "formatting-enabled");
	private static final NamespacedKey FORMATTING_TYPE = new NamespacedKey("yase", "formatting-type");

	private final Map<UUID, User> users = new HashMap<>();

	public User getUser(final UUID uuid) {
		return this.users.computeIfAbsent(uuid, u -> {
			final var user = new User(u);

			final @Nullable Player player = user.getPlayer();
			if (player != null) {
				final PersistentDataContainer pdc = user.getPlayer().getPersistentDataContainer();

				final @Nullable Boolean formattingEnabled = pdc.get(FORMATTING_ENABLED, PersistentDataType.BOOLEAN);
				if (formattingEnabled != null) {
					user.formattingEnabled(formattingEnabled);
				}

				final @Nullable String formattingType = pdc.get(FORMATTING_TYPE, PersistentDataType.STRING);
				if (formattingType != null) {
					user.formattingType(User.FormattingType.valueOf(formattingType));
				}
			}

			return user;
		});
	}

	public User getUser(final Player player) {
		return this.getUser(player.getUniqueId());
	}

	public void save() {
		for (final User user : this.users.values()) {
			final @Nullable Player player = user.getPlayer();
			if (player != null) {
				final PersistentDataContainer pdc = user.getPlayer().getPersistentDataContainer();
				pdc.set(FORMATTING_ENABLED, PersistentDataType.BOOLEAN, user.formattingEnabled());
				pdc.set(FORMATTING_TYPE, PersistentDataType.STRING, user.formattingType().name());
			}
		}
	}
}
