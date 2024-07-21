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
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;

public final class MainCommand {

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
        .meta(CommandMeta.DESCRIPTION, "Various commands for YetAnotherSignEditor.");

    final var help = main
        .handler(c -> c.getSender().sendMessage(this.langConfig.c(NodePath.path("help"))));

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

    final var unwax = main
        .literal("unwax", ArgumentDescription.of("Unwax the sign you're looking at."))
        .permission(Permission.UNWAX)
        .senderType(Player.class)
        .handler(c -> {
          final Player player = (Player) c.getSender();

          final @Nullable Block targetedBlock = player.getTargetBlockExact(6);
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
        .literal("format", ArgumentDescription.of("Toggle your ability to format sign text."))
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

    final var set = main
        .literal("set", ArgumentDescription.of("Set the text of the sign you're looking at."))
        .permission(Permission.SET)
        .senderType(Player.class)
        .argument(IntegerArgument.<CommandSender>builder("line").withMin(1).withMax(4).build())
        .argument(StringArgument.<CommandSender>builder("text").greedy().asOptional().build())
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final int line = c.<Integer>get("line") - 1; // signs are 0-indexed.
          final String text = c.<String>getOptional("text").orElse("");

          final @Nullable Block targetedBlock = player.getTargetBlockExact(6);
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

    commandManager.command(help)
        .command(reload)
        .command(unwax)
        .command(format)
        .command(formatFormattingType)
        .command(set);
  }

}
