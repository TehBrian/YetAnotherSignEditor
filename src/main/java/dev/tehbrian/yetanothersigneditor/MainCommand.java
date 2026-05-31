package dev.tehbrian.yetanothersigneditor;

import com.google.inject.Inject;
import dev.tehbrian.mayi.core.ActionType;
import dev.tehbrian.mayi.paper.PaperMayi;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.format.Format;
import dev.tehbrian.yetanothersigneditor.format.SignFormatting;
import dev.tehbrian.yetanothersigneditor.user.User;
import dev.tehbrian.yetanothersigneditor.user.UserPersistence;
import dev.tehbrian.yetanothersigneditor.user.UserService;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PlayerSource;
import org.incendo.cloud.paper.util.sender.Source;
import org.spongepowered.configurate.NodePath;

import java.util.List;

import static dev.tehbrian.yetanothersigneditor.format.NativePersistence.handlePersistence;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.MAGIC_NUMBER_OF_TICKS;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.format;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.lines;
import static dev.tehbrian.yetanothersigneditor.format.SignFormatting.unformatSignLines;
import static dev.tehbrian.yetanothersigneditor.format.UserFormatting.shouldFormat;
import static net.kyori.adventure.sound.Sound.sound;
import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;
import static org.incendo.cloud.component.CommandComponent.builder;
import static org.incendo.cloud.description.Description.description;
import static org.incendo.cloud.parser.standard.EnumParser.enumParser;
import static org.incendo.cloud.parser.standard.IntegerParser.integerParser;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

public final class MainCommand {

	public static final int MAX_DISTANCE = 6;

	private final YetAnotherSignEditor yetAnotherSignEditor;
	private final PaperMayi restrictionHelper;
	private final UserService userService;
	private final LangConfig langConfig;

	@Inject
	public MainCommand(
			final YetAnotherSignEditor yetAnotherSignEditor,
			final PaperMayi restrictionHelper,
			final UserService userService,
			final LangConfig langConfig
	) {
		this.yetAnotherSignEditor = yetAnotherSignEditor;
		this.restrictionHelper = restrictionHelper;
		this.userService = userService;
		this.langConfig = langConfig;
	}

	public void register(final PaperCommandManager<Source> commandManager) {
		final var main = commandManager.commandBuilder("yase")
				.commandDescription(description("Commands from YetAnotherSignEditor."));

		final var help = main
				.handler(c -> c.sender().source().sendMessage(this.langConfig.c(NodePath.path("help"))));

		final var set = main
				.literal("set", description("Set the text of the targeted sign."))
				.permission(Permission.SET)
				.senderType(PlayerSource.class)
				.argument(builder("line", integerParser(1, 4)))
				.argument(builder("text", greedyStringParser()).optional())
				.handler(c -> {
					final Player player = c.sender().source();
					final int line = c.<Integer>get("line") - 1; // signs are 0-indexed.
					final String text = c.<String>optional("text").orElse("");

					final @Nullable Block targetedBlock = player.getTargetBlockExact(MAX_DISTANCE);
					if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
						player.sendMessage(this.langConfig.c(NodePath.path("not-a-sign")));
						return;
					}

					if (!this.restrictionHelper.checkRestrictions(player, targetedBlock.getLocation(), ActionType.ALL)) {
						player.sendMessage(this.langConfig.c(NodePath.path("no-permission-here")));
						return;
					}

					final User user = this.userService.getUser(player);
					final Side side = sign.getInteractableSideFor(player);

					handlePersistence(sign, side, line, Format.plain(text), user);

					final Component formattedText = format(text, user);

					sign.getSide(side).line(line, formattedText);
					sign.update();

					sign.getWorld().playSound(sound(
							sign.getBlock().getBlockSoundGroup().getPlaceSound(),
							Sound.Source.BLOCK, 1.0F, 0.25F
					));
				});

		final var open = main
				.literal("open", description("Open the targeted sign."))
				.permission(Permission.OPEN)
				.senderType(PlayerSource.class)
				.handler(c -> {
					final Player player = c.sender().source();
					final User user = this.userService.getUser(player);

					final @Nullable Block targetedBlock = player.getTargetBlockExact(MAX_DISTANCE);
					if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
						player.sendMessage(this.langConfig.c(NodePath.path("not-a-sign")));
						return;
					}

					if (!this.restrictionHelper.checkRestrictions(player, targetedBlock.getLocation(), ActionType.ALL)) {
						player.sendMessage(this.langConfig.c(NodePath.path("no-permission-here")));
						return;
					}

					final Side side = sign.getInteractableSideFor(player);
					final SignSide signSide = sign.getSide(side);

					if (shouldFormat(user)) {
						lines(signSide, unformatSignLines(sign, side, user));
						sign.update();

						this.yetAnotherSignEditor.getServer().getScheduler().runTaskLater(
								this.yetAnotherSignEditor,
								() -> player.openSign(sign, side),
								MAGIC_NUMBER_OF_TICKS
						);
					} else {
						player.openSign(sign, side);
					}
				});

		final var copy = main
				.literal("copy", description("Copy the text of the targeted sign."))
				.permission(Permission.COPY)
				.senderType(PlayerSource.class)
				.handler(c -> {
					final Player player = c.sender().source();
					final User user = this.userService.getUser(player);

					final @Nullable Block targetedBlock = player.getTargetBlockExact(MAX_DISTANCE);
					if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
						player.sendMessage(this.langConfig.c(NodePath.path("not-a-sign")));
						return;
					}

					final Side side = sign.getInteractableSideFor(player);
					final SignSide signSide = sign.getSide(side);
					final List<String> raw = SignFormatting.unformatSignLines(sign, side, user)
							.stream().map(Format::serializePlain).toList();

					player.sendMessage(this.langConfig.c(
							NodePath.path("copy", "message"),
							resolver(
									Placeholder.parsed("x", Integer.toString(targetedBlock.getX())),
									Placeholder.parsed("y", Integer.toString(targetedBlock.getY())),
									Placeholder.parsed("z", Integer.toString(targetedBlock.getZ())),
									Placeholder.component("line_0", this.hoverLine(signSide.line(0), raw.get(0))),
									Placeholder.component("line_1", this.hoverLine(signSide.line(1), raw.get(1))),
									Placeholder.component("line_2", this.hoverLine(signSide.line(2), raw.get(2))),
									Placeholder.component("line_3", this.hoverLine(signSide.line(3), raw.get(3)))
							)
					));
				});

		final var unwax = main
				.literal("unwax", description("Unwax the targeted sign."))
				.permission(Permission.UNWAX)
				.senderType(PlayerSource.class)
				.handler(c -> {
					final Player player = c.sender().source();

					final @Nullable Block targetedBlock = player.getTargetBlockExact(MAX_DISTANCE);
					if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
						player.sendMessage(this.langConfig.c(NodePath.path("not-a-sign")));
						return;
					}

					if (!this.restrictionHelper.checkRestrictions(player, targetedBlock.getLocation(), ActionType.ALL)) {
						player.sendMessage(this.langConfig.c(NodePath.path("no-permission-here")));
						return;
					}

					if (!sign.isWaxed()) {
						player.sendMessage(this.langConfig.c(NodePath.path("not-waxed")));
						return;
					}

					sign.setWaxed(false);
					sign.update();

					sign.getWorld().playEffect(sign.getLocation(), Effect.COPPER_WAX_OFF, 0);
					sign.getWorld().playSound(sound(
							org.bukkit.Sound.ITEM_HONEYCOMB_WAX_ON,
							Sound.Source.BLOCK, 1.0F, 1.0F
					));
					sign.getWorld().playSound(sound(
							org.bukkit.Sound.ITEM_BOTTLE_EMPTY,
							Sound.Source.BLOCK, 1.0F, 0.7F
					));
				});

		final var format = main
				.literal("format", description("Toggle text formatting or change your formatting type."))
				.permission(Permission.FORMAT)
				.senderType(PlayerSource.class)
				.handler(c -> {
					final Player sender = c.sender().source();
					final User user = this.userService.getUser(sender);

					if (user.toggleFormattingEnabled()) {
						sender.sendMessage(this.langConfig.c(NodePath.path("format", "enabled")));
					} else {
						sender.sendMessage(this.langConfig.c(NodePath.path("format", "disabled")));
					}
					UserPersistence.save(user);
				});

		final var formatFormattingType = format
				.argument(builder("formatting_type", enumParser(User.FormattingType.class)))
				.handler(c -> {
					final Player player = c.sender().source();
					final User.FormattingType formattingType = c.get("formatting_type");
					final User user = this.userService.getUser(player);

					user.formattingType(formattingType);
					player.sendMessage(this.langConfig.c(
							NodePath.path("format", "set"),
							Placeholder.parsed("formatting_type", formattingType.pretty())
					));
					UserPersistence.save(user);
				});

		final var reload = main
				.literal("reload", description("Reload the plugin's config."))
				.permission(Permission.RELOAD)
				.handler(c -> {
					if (this.yetAnotherSignEditor.loadConfiguration()) {
						c.sender().source().sendMessage(this.langConfig.c(NodePath.path("reload", "successful")));
					} else {
						c.sender().source().sendMessage(this.langConfig.c(NodePath.path("reload", "unsuccessful")));
					}
				});

		commandManager.command(help)
				.command(set)
				.command(open)
				.command(copy)
				.command(unwax)
				.command(format)
				.command(formatFormattingType)
				.command(reload);
	}

	private Component hoverLine(final Component component, final String raw) {
		return component
				.hoverEvent(this.langConfig.c(NodePath.path("copy", "hover"), Placeholder.unparsed("line", raw)))
				.clickEvent(ClickEvent.copyToClipboard(raw));
	}

}
