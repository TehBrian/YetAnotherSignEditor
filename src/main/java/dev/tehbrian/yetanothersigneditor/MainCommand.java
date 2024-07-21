package dev.tehbrian.yetanothersigneditor;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.user.User;
import dev.tehbrian.yetanothersigneditor.user.UserService;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;

import java.util.List;

import static dev.tehbrian.yetanothersigneditor.SignFormatting.MAGIC_NUMBER_OF_TICKS;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.shouldFormat;
import static dev.tehbrian.yetanothersigneditor.SignFormatting.unformatLines;
import static net.kyori.adventure.text.minimessage.tag.resolver.TagResolver.resolver;

public final class MainCommand {

  public static final int MAX_DISTANCE = 6;

  private final YetAnotherSignEditor yetAnotherSignEditor;
  private final SpigotRestrictionHelper restrictionHelper;
  private final UserService userService;
  private final LangConfig langConfig;

  @Inject
  public MainCommand(
      final YetAnotherSignEditor yetAnotherSignEditor,
      final SpigotRestrictionHelper restrictionHelper,
      final UserService userService,
      final LangConfig langConfig
  ) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
    this.restrictionHelper = restrictionHelper;
    this.userService = userService;
    this.langConfig = langConfig;
  }

  public void register(final PaperCommandManager<CommandSender> commandManager) {
    final var main = commandManager.commandBuilder("yase")
        .meta(CommandMeta.DESCRIPTION, "Commands from YetAnotherSignEditor.");

    final var help = main
        .handler(c -> c.getSender().sendMessage(this.langConfig.c(NodePath.path("help"))));

    final var set = main
        .literal("set", ArgumentDescription.of("Set the text of the targeted sign."))
        .permission(Permission.SET)
        .senderType(Player.class)
        .argument(IntegerArgument.<CommandSender>builder("line").withMin(1).withMax(4).build())
        .argument(StringArgument.<CommandSender>builder("text").greedy().asOptional().build())
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final int line = c.<Integer>get("line") - 1; // signs are 0-indexed.
          final String text = c.<String>getOptional("text").orElse("");

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

          Component formattedText = Format.plain(text);
          if (user.formatEnabled() && player.hasPermission(Permission.FORMAT)) {
            if (user.formattingType() == User.FormattingType.LEGACY
                && player.hasPermission(Permission.LEGACY)) {
              formattedText = Format.legacy(text);
            } else if (user.formattingType() == User.FormattingType.MINIMESSAGE
                && player.hasPermission(Permission.MINIMESSAGE)) {
              formattedText = Format.miniMessage(text);
            }
          }

          sign.getSide(sign.getInteractableSideFor(player)).line(line, formattedText);
          sign.update();
          sign.getWorld().playSound(Sound.sound(
              sign.getBlock().getBlockSoundGroup().getPlaceSound(),
              Source.BLOCK,
              1.0F, 0.25F
          ));
        });

    final var open = main
        .literal("open", ArgumentDescription.of("Open the targeted sign."))
        .permission(Permission.OPEN)
        .senderType(Player.class)
        .handler(c -> {
          final Player player = (Player) c.getSender();
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
            final List<Component> newLines = unformatLines(sign, side, user);
            for (int i = 0; i < newLines.size(); i++) {
              signSide.line(i, newLines.get(i));
            }
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
        .literal("copy", ArgumentDescription.of("Copy the text of the targeted sign."))
        .permission(Permission.COPY)
        .senderType(Player.class)
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final User user = this.userService.getUser(player);

          final @Nullable Block targetedBlock = player.getTargetBlockExact(MAX_DISTANCE);
          if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
            player.sendMessage(this.langConfig.c(NodePath.path("not-a-sign")));
            return;
          }

          final Side side = sign.getInteractableSideFor(player);
          final SignSide signSide = sign.getSide(side);

          player.sendMessage(this.langConfig.c(
              NodePath.path("copy", "message"),
              resolver(
                  Placeholder.parsed("x", Integer.toString(targetedBlock.getX())),
                  Placeholder.parsed("y", Integer.toString(targetedBlock.getY())),
                  Placeholder.parsed("z", Integer.toString(targetedBlock.getZ())),
                  Placeholder.component("line_1", hoverLine(signSide.line(0), user)),
                  Placeholder.component("line_2", hoverLine(signSide.line(1), user)),
                  Placeholder.component("line_3", hoverLine(signSide.line(2), user)),
                  Placeholder.component("line_4", hoverLine(signSide.line(3), user))
              )
          ));
        });

    final var unwax = main
        .literal("unwax", ArgumentDescription.of("Unwax the targeted sign."))
        .permission(Permission.UNWAX)
        .senderType(Player.class)
        .handler(c -> {
          final Player player = (Player) c.getSender();

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
          sign.getWorld().playSound(Sound.sound(
              org.bukkit.Sound.ITEM_HONEYCOMB_WAX_ON,
              Source.BLOCK,
              1.0F, 1.0F
          ));
          sign.getWorld().playSound(Sound.sound(
              org.bukkit.Sound.ITEM_BOTTLE_EMPTY,
              Source.BLOCK,
              1.0F, 0.7F
          ));
        });

    final var format = main
        .literal("format", ArgumentDescription.of("Toggle text formatting or change your formatting type."))
        .permission(Permission.FORMAT)
        .senderType(Player.class)
        .handler(c -> {
          final Player sender = (Player) c.getSender();
          if (this.userService.getUser(sender).toggleFormatEnabled()) {
            sender.sendMessage(this.langConfig.c(NodePath.path("format", "enabled")));
          } else {
            sender.sendMessage(this.langConfig.c(NodePath.path("format", "disabled")));
          }
        });

    final var formatFormattingType = format
        .argument(EnumArgument
            .<CommandSender, User.FormattingType>builder(User.FormattingType.class, "formatting_type")
            .build())
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final User.FormattingType formattingType = c.get("formatting_type");

          this.userService.getUser(player).formattingType(formattingType);
          player.sendMessage(this.langConfig.c(
              NodePath.path("format", "set"),
              Placeholder.parsed("formatting_type", formattingType.pretty())
          ));
        });

    final var reload = main
        .literal("reload", ArgumentDescription.of("Reload the plugin's config."))
        .permission(Permission.RELOAD)
        .handler(c -> {
          if (this.yetAnotherSignEditor.loadConfiguration()) {
            c.getSender().sendMessage(this.langConfig.c(NodePath.path("reload", "successful")));
          } else {
            c.getSender().sendMessage(this.langConfig.c(NodePath.path("reload", "unsuccessful")));
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

  private Component hoverLine(final Component component, final User user) {
    final String raw = SignFormatting.unformatRaw(component, user);
    return component
        .hoverEvent(this.langConfig.c(NodePath.path("copy", "hover"), Placeholder.unparsed("line", raw)))
        .clickEvent(ClickEvent.copyToClipboard(raw));
  }

}
