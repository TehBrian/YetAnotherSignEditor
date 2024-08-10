package dev.tehbrian.yetanothersigneditor.format;

import dev.tehbrian.yetanothersigneditor.user.User;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Objects;

import static dev.tehbrian.yetanothersigneditor.format.UserFormatting.shouldFormatMiniMessage;
import static java.util.stream.IntStream.range;


/**
 * Handles the persistence of native MiniMessage code on signs to allow
 * lossless editing of otherwise lossy tags, e.g., {@code <rainbow>}.
 *
 * <p>Each line is stored separately to invalidate/persist specific lines,
 * which is needed because /yase set can set individual lines using formating
 * types different from the rest of the sign.</p>z
 */
public final class NativePersistence {

	/*
	 * FYI, if you're looking at all these keys and saying, "What the fuck? Is
	 * Brian stupid? Why not just store them as a newline-delimited list?", I
	 * *did*, initially. But thanks to /yase set, each line could individually have
	 * either persisted native MM or none/invalidated, and I'm not in the business
	 * of using strings to encode null.
	 */

	private static final NamespacedKey FRONT_0 = pdcKey("front-0");
	private static final NamespacedKey FRONT_1 = pdcKey("front-1");
	private static final NamespacedKey FRONT_2 = pdcKey("front-2");
	private static final NamespacedKey FRONT_3 = pdcKey("front-3");

	private static final NamespacedKey BACK_0 = pdcKey("back-0");
	private static final NamespacedKey BACK_1 = pdcKey("back-1");
	private static final NamespacedKey BACK_2 = pdcKey("back-2");
	private static final NamespacedKey BACK_3 = pdcKey("back-3");

	private NativePersistence() {
	}

	private static NamespacedKey pdcKey(final String area) {
		return Objects.requireNonNull(NamespacedKey.fromString("yase:native-mm-" + area));
	}

	/**
	 * It works. If you have a better idea, PR it.
	 */
	private static NamespacedKey toPdcKey(final Side side, final int line) {
		if (side == Side.FRONT) {
			return switch (line) {
				case 0 -> FRONT_0;
				case 1 -> FRONT_1;
				case 2 -> FRONT_2;
				case 3 -> FRONT_3;
				default -> throw new IllegalArgumentException();
			};
		} else {
			return switch (line) {
				case 0 -> BACK_0;
				case 1 -> BACK_1;
				case 2 -> BACK_2;
				case 3 -> BACK_3;
				default -> throw new IllegalArgumentException();
			};
		}
	}

	private static @Nullable String rawMmFromPdc(final Sign sign, final Side side, final int line) {
		final var pdc = sign.getPersistentDataContainer();
		return pdc.get(toPdcKey(side, line), PersistentDataType.STRING);
	}

	public static @Nullable Component mmFromPdc(final Sign sign, final Side side, final int line) {
		final @Nullable String raw = rawMmFromPdc(sign, side, line);
		if (raw == null) {
			return null;
		}
		return Format.plain(raw);
	}

	public static void mmToPdc(final Sign sign, final Side side, final int line, final Component mm) {
		final var pdc = sign.getPersistentDataContainer();
		final String data = Format.serializePlain(mm);
		pdc.set(toPdcKey(side, line), PersistentDataType.STRING, data);
	}

	public static void mmToPdc(final Sign sign, final Side side, final List<Component> mm) {
		if (mm.size() != 4) {
			throw new IllegalArgumentException();
		}
		range(0, mm.size()).forEach(i -> mmToPdc(sign, side, i, mm.get(i)));
	}

	public static void maybeInvalidate(
			final Sign sign,
			final Side side,
			final int line,
			final Component newText,
			final User user
	) {
		final var pdc = sign.getPersistentDataContainer();
		final @Nullable String raw = rawMmFromPdc(sign, side, line);
		if (raw == null) {
			// no stored mm to invalidate! :D
			return;
		}

		// currently stored line formatted by MM.
		final Component oldShit = Format.miniMessage(raw);

		// new lines formatted by the user's formatting type.
		final Component newShit = SignFormatting.format(newText, user);

		// if they result in the same component, no need to invalidate.
		if (!oldShit.equals(newShit)) {
			pdc.remove(toPdcKey(side, line));
		}
	}

	public static void maybeInvalidate(
			final Sign sign,
			final Side side,
			final List<Component> newTexts,
			final User user
	) {
		if (newTexts.size() != 4) {
			throw new IllegalArgumentException();
		}
		range(0, newTexts.size()).forEach(i -> maybeInvalidate(sign, side, i, newTexts.get(i), user));
	}

	public static void handlePersistence(
			final Sign sign,
			final Side side,
			final int line,
			final Component newText,
			final User user
	) {
		if (shouldFormatMiniMessage(user)) {
			// store mm to pdc.
			mmToPdc(sign, side, line, newText);
		} else {
			// if contents have changed, invalidate previously stored mm.
			maybeInvalidate(sign, side, line, newText, user);
		}
		sign.update(); // important! pdc is only stored to snapshot of state.
	}

	public static void handlePersistence(
			final Sign sign,
			final Side side,
			final List<Component> newTexts,
			final User user
	) {
		if (shouldFormatMiniMessage(user)) {
			// store mm to pdc.
			mmToPdc(sign, side, newTexts);
		} else {
			// if contents have changed, invalidate previously stored mm.
			maybeInvalidate(sign, side, newTexts, user);
		}
		sign.update(); // important! pdc is only stored to snapshot of state.
	}

}
