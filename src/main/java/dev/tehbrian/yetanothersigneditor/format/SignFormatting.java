package dev.tehbrian.yetanothersigneditor.format;

import dev.tehbrian.yetanothersigneditor.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

import static dev.tehbrian.yetanothersigneditor.format.UserFormatting.shouldFormatLegacy;
import static dev.tehbrian.yetanothersigneditor.format.UserFormatting.shouldFormatMiniMessage;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

public final class SignFormatting {
	/**
	 * Number of ticks before the server opens the updated sign.
	 */
	public static final int MAGIC_NUMBER_OF_TICKS = 2;
	private static final int MAX_LINE_LENGTH = 384;

	private SignFormatting() {
	}

	public static Component format(final String line, final User user) {
		if (shouldFormatLegacy(user)) {
			return Format.legacy(line);
		} else if (shouldFormatMiniMessage(user)) {
			return Format.miniMessage(line);
		} else {
			return Format.plain(line);
		}
	}

	public static Component format(final Component line, final User user) {
		if (shouldFormatLegacy(user)) {
			return Format.legacy(line);
		} else if (shouldFormatMiniMessage(user)) {
			return Format.miniMessage(line);
		} else {
			return Format.plain(line);
		}
	}

	public static List<Component> format(final List<Component> lines, final User user) {
		return lines.stream().map(line -> format(line, user)).toList();
	}

	private static String unformatRaw(final Component line, final User user) {
		String newLine;

		if (shouldFormatLegacy(user)) {
			newLine = Format.serializeLegacy(line);
		} else if (shouldFormatMiniMessage(user)) {
			newLine = Format.serializeMiniMessage(line);
		} else {
			newLine = Format.serializePlain(line);
		}

		// truncate text if it's so long that the server will kick the player.
		if (newLine.length() > MAX_LINE_LENGTH) {
			newLine = newLine.substring(0, MAX_LINE_LENGTH);
		}

		return newLine;
	}

	private static Component unformat(final Component line, final User user) {
		return Format.plain(unformatRaw(line, user));
	}

	private static List<Component> unformat(final List<Component> lines, final User user) {
		return lines.stream().map(line -> unformat(line, user)).toList();
	}

	public static List<Component> unformatSignLines(final Sign sign, final Side side, final User user) {
		final SignSide signSide = sign.getSide(side);

		if (shouldFormatMiniMessage(user)) {
			// merge with native MM.
			return rangeClosed(0, 3).mapToObj(i -> requireNonNullElseGet(
					NativePersistence.mmFromPdc(sign, side, i),
					() -> unformat(signSide.line(i), user)
			)).toList();
		} else {
			return unformat(signSide.lines(), user);
		}
	}

	public static void lines(final SignChangeEvent event, final List<Component> lines) {
		range(0, lines.size()).forEach(i -> event.line(i, lines.get(i)));
	}

	public static void lines(final SignSide signSide, final List<Component> lines) {
		range(0, lines.size()).forEach(i -> signSide.line(i, lines.get(i)));
	}
}
