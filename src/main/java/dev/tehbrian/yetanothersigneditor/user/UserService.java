package dev.tehbrian.yetanothersigneditor.user;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UserService {
	private final Map<UUID, User> users = new HashMap<>();

	public User getUser(final UUID uuid) {
		return this.users.computeIfAbsent(uuid, u -> {
			final var user = new User(u);
			UserPersistence.load(user);
			return user;
		});
	}

	public User getUser(final Player player) {
		return this.getUser(player.getUniqueId());
	}
}
