package dev.tehbrian.yetanothersigneditor.user;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class UserService {
	private final Map<UUID, User> userMap = new HashMap<>();

	public User getUser(final UUID uuid) {
		return this.userMap.computeIfAbsent(uuid, User::new);
	}

	public User getUser(final Player player) {
		return this.getUser(player.getUniqueId());
	}
}
