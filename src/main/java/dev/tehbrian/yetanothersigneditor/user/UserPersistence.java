package dev.tehbrian.yetanothersigneditor.user;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

public class UserPersistence {
	private static final NamespacedKey FORMATTING_ENABLED = new NamespacedKey("yase", "formatting-enabled");
	private static final NamespacedKey FORMATTING_TYPE = new NamespacedKey("yase", "formatting-type");

	public static void load(final User user) {
		final @Nullable Player player = user.getPlayer();
		if (player == null) {
			return;
		}
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

	public static void save(final User user) {
		final @Nullable Player player = user.getPlayer();
		if (player == null) {
			return;
		}
		final PersistentDataContainer pdc = user.getPlayer().getPersistentDataContainer();

		pdc.set(FORMATTING_ENABLED, PersistentDataType.BOOLEAN, user.formattingEnabled());
		pdc.set(FORMATTING_TYPE, PersistentDataType.STRING, user.formattingType().name());
	}
}
