package xyz.tehbrian.yetanothersigneditor.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import dev.tehbrian.tehlib.paper.cloud.PaperCloudCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;
import xyz.tehbrian.yetanothersigneditor.util.Format;
import xyz.tehbrian.yetanothersigneditor.util.Permissions;

public final class MainCommand extends PaperCloudCommand<CommandSender> {

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

  @Override
  public void register(final PaperCommandManager<CommandSender> commandManager) {
    final var main = commandManager.commandBuilder("yase")
        .meta(CommandMeta.DESCRIPTION, "Various commands for YetAnotherSignEditor.");

    final var help = main
        .handler(c -> c.getSender().sendMessage(this.langConfig.c(NodePath.path("help"))));

    final var reload = main
        .literal("reload", ArgumentDescription.of("Reload the plugin's config."))
        .permission(Permissions.RELOAD)
        .handler(c -> {
          if (this.yetAnotherSignEditor.loadConfiguration()) {
            c.getSender().sendMessage(this.langConfig.c(NodePath.path("reload", "successful")));
          } else {
            c.getSender().sendMessage(this.langConfig.c(NodePath.path("reload", "unsuccessful")));
          }
        });

    final var edit = main
        .literal("edit", ArgumentDescription.of("Toggle your ability to edit sign text."))
        .permission(Permissions.EDIT)
        .senderType(Player.class)
        .handler(c -> {
          final Player player = (Player) c.getSender();
          if (this.userService.getUser(player).toggleEditEnabled()) {
            player.sendMessage(this.langConfig.c(NodePath.path("edit", "enabled")));
          } else {
            player.sendMessage(this.langConfig.c(NodePath.path("edit", "disabled")));
          }
        });

    final var format = main
        .literal("format", ArgumentDescription.of("Toggle your ability to format sign text."))
        .permission(Permissions.FORMAT)
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
            .<CommandSender, User.FormattingType>newBuilder(User.FormattingType.class, "formatting_type")
            .build())
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final User.FormattingType formattingType = c.get("formatting_type");

          this.userService.getUser(player).formattingType(formattingType);
          player.sendMessage(this.langConfig.c(
              NodePath.path("format", "set"),
              Placeholder.parsed("formatting_type", formattingType.toString())
          ));
        });

    final var set = main
        .literal("set", ArgumentDescription.of("Set the text of the sign you're looking at."))
        .permission(Permissions.SET)
        .senderType(Player.class)
        .argument(IntegerArgument.<CommandSender>newBuilder("line").withMin(1).withMax(4).build())
        .argument(StringArgument.<CommandSender>newBuilder("text").greedy().asOptional().build())
        .handler(c -> {
          final Player player = (Player) c.getSender();
          final int line = c.<Integer>get("line") - 1; // arrays are 0-indexed
          final String text = c.<String>getOptional("text").orElse("");

          final @Nullable Block targetedBlock = player.getTargetBlock(6);
          if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
            player.sendMessage(this.langConfig.c(NodePath.path("set", "not_a_sign")));
            return;
          }
          if (!this.restrictionHelper.checkRestrictions(player, targetedBlock.getLocation(), ActionType.ALL)) {
            player.sendMessage(this.langConfig.c(NodePath.path("set", "no_permission")));
            return;
          }

          final User user = this.userService.getUser(player);

          Component formattedText = Format.plain(text);
          if (user.formatEnabled() && player.hasPermission(Permissions.FORMAT)) {
            if (user.formattingType() == User.FormattingType.LEGACY
                && player.hasPermission(Permissions.LEGACY)) {
              formattedText = Format.legacy(text);
            } else if (user.formattingType() == User.FormattingType.MINIMESSAGE
                && player.hasPermission(Permissions.MINIMESSAGE)) {
              formattedText = Format.miniMessage(text);
            }
          }

          sign.line(line, formattedText);

          sign.update();
        });

    commandManager.command(help)
        .command(reload)
        .command(edit)
        .command(format)
        .command(formatFormattingType)
        .command(set);
  }

}
