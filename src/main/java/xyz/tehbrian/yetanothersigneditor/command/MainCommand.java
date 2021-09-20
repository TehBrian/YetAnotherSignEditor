package xyz.tehbrian.yetanothersigneditor.command;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.IntegerArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Inject;
import dev.tehbrian.tehlib.paper.cloud.PaperCloudCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.NodePath;
import xyz.tehbrian.restrictionhelper.core.ActionType;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.yetanothersigneditor.Constants;
import xyz.tehbrian.yetanothersigneditor.FormatUtil;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;

import java.util.Map;

public final class MainCommand extends PaperCloudCommand<CommandSender> {

    private final YetAnotherSignEditor yetAnotherSignEditor;
    private final SpigotRestrictionHelper restrictionHelper;
    private final UserService userService;
    private final LangConfig langConfig;

    /**
     * @param yetAnotherSignEditor injected
     * @param restrictionHelper    injected
     * @param userService          injected
     * @param langConfig           injected
     */
    @Inject
    public MainCommand(
            @NonNull final YetAnotherSignEditor yetAnotherSignEditor,
            @NonNull final SpigotRestrictionHelper restrictionHelper,
            @NonNull final UserService userService,
            @NonNull final LangConfig langConfig
    ) {
        this.yetAnotherSignEditor = yetAnotherSignEditor;
        this.restrictionHelper = restrictionHelper;
        this.userService = userService;
        this.langConfig = langConfig;
    }

    /**
     * Register the command.
     *
     * @param commandManager the command manager
     */
    @Override
    public void register(final @NonNull PaperCommandManager<CommandSender> commandManager) {
        final var main = commandManager.commandBuilder("yase", ArgumentDescription.of("Various commands for YetAnotherSignEditor."))
                .handler(c -> {
                    for (final Component component : this.langConfig.cl(NodePath.path("help"))) {
                        c.getSender().sendMessage(component);
                    }
                });

        final var reload = main.literal("reload", ArgumentDescription.of("Reloads the plugin's config."))
                .permission(Constants.Permissions.RELOAD)
                .handler(c -> {
                    this.yetAnotherSignEditor.loadConfigs();
                    c.getSender().sendMessage(this.langConfig.c(NodePath.path("reload")));
                });

        final var edit = main.literal("edit", ArgumentDescription.of("Toggle your ability to edit sign text."))
                .permission(Constants.Permissions.EDIT)
                .senderType(Player.class)
                .handler(c -> {
                    final Player player = (Player) c.getSender();
                    if (this.userService.getUser(player).toggleEditEnabled()) {
                        player.sendMessage(this.langConfig.c(NodePath.path("edit", "enabled")));
                    } else {
                        player.sendMessage(this.langConfig.c(NodePath.path("edit", "disabled")));
                    }
                });

        final var color = main.literal("color", ArgumentDescription.of("Toggle your ability to color sign text."))
                .permission(Constants.Permissions.COLOR)
                .senderType(Player.class)
                .handler(c -> {
                    final Player sender = (Player) c.getSender();
                    if (this.userService.getUser(sender).toggleColorEnabled()) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("color", "enabled")));
                    } else {
                        sender.sendMessage(this.langConfig.c(NodePath.path("color", "disabled")));
                    }
                });

        final var colorFormattingType = color.argument(EnumArgument
                .<CommandSender, User.FormattingType>newBuilder(User.FormattingType.class, "formatting_type")
                .build())
                .handler(c -> {
                    final @NonNull Player player = (Player) c.getSender();
                    final User.@NonNull FormattingType formattingType = c.get("formatting_type");

                    this.userService.getUser(player).formattingType(formattingType);
                    player.sendMessage(this.langConfig.c(
                            NodePath.path("color", "set"),
                            Map.of("formatting_type", formattingType.toString())
                    ));
                });

        final var set = main.literal("set", ArgumentDescription.of("Set the text of the sign you're looking at."))
                .permission(Constants.Permissions.SET)
                .senderType(Player.class)
                .argument(IntegerArgument
                        .<CommandSender>newBuilder("line")
                        .withMin(1)
                        .withMax(4)
                        .build())
                .argument(StringArgument
                        .<CommandSender>newBuilder("text")
                        .greedy()
                        .asOptional()
                        .build())
                .handler(c -> {
                    final @NonNull Player player = (Player) c.getSender();
                    final int line = c.<Integer>get("line") - 1; // arrays are 0-indexed
                    final @NonNull String text = c.<String>getOptional("text").orElse("");

                    final @Nullable Block targetedBlock = player.getTargetBlock(6);
                    if (targetedBlock == null || !(targetedBlock.getState() instanceof final Sign sign)) {
                        player.sendMessage(this.langConfig.c(NodePath.path("set", "not_a_sign")));
                        return;
                    }
                    if (!this.restrictionHelper.checkRestrictions(player, targetedBlock.getLocation(), ActionType.ALL)) {
                        player.sendMessage(this.langConfig.c(NodePath.path("no_permission"))); // TODO: throw NoPermissionException somehow
                        return;
                    }

                    final User user = this.userService.getUser(player);

                    Component formattedText = FormatUtil.plain(text);
                    if (player.hasPermission(Constants.Permissions.COLOR) && user.colorEnabled()) {
                        if (user.formattingType() == User.FormattingType.LEGACY && player.hasPermission(Constants.Permissions.LEGACY)) {
                            formattedText = FormatUtil.legacy(text);
                        } else if (user.formattingType() == User.FormattingType.MINI_MESSAGE && player.hasPermission(Constants.Permissions.MINI_MESSAGE)) {
                            formattedText = FormatUtil.miniMessage(text);
                        }
                    }

                    sign.line(line, formattedText);

                    sign.update();
                });

        commandManager.command(main)
                .command(reload)
                .command(edit)
                .command(color)
                .command(colorFormattingType)
                .command(set);
    }

}
