package xyz.tehbrian.yetanothersigneditor.commands;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;
import xyz.tehbrian.yetanothersigneditor.Constants;
import xyz.tehbrian.yetanothersigneditor.FormatUtil;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.user.User;
import xyz.tehbrian.yetanothersigneditor.user.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class YaseCommand implements CommandExecutor, TabCompleter {

    private final YetAnotherSignEditor yetAnotherSignEditor;
    private final UserService userService;
    private final LangConfig langConfig;

    /**
     * @param yetAnotherSignEditor injected
     * @param userService          injected
     * @param langConfig           injected
     */
    @Inject
    public YaseCommand(
            final @NonNull YetAnotherSignEditor yetAnotherSignEditor,
            final @NonNull UserService userService,
            final @NonNull LangConfig langConfig
    ) {
        this.yetAnotherSignEditor = yetAnotherSignEditor;
        this.userService = userService;
        this.langConfig = langConfig;
    }

    @Override
    public boolean onCommand(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            final @NotNull String[] args
    ) {
        if (args.length >= 1) {
            switch (args[0].toLowerCase()) {
                case "set" -> {
                    if (!sender.hasPermission(Constants.Permissions.SET)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("no_permission")));
                        return true;
                    }
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("player_only")));
                        return true;
                    }
                    if (args.length < 2) {
                        break;
                    }

                    final Block block = player.getTargetBlock(6);
                    if (block == null) {
                        return true;
                    }
                    if (!(block.getState() instanceof final Sign sign)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("set", "not_a_sign")));
                        return true;
                    }

                    final int line;
                    try {
                        line = Integer.parseInt(args[1]) - 1; // arrays are 0-indexed
                    } catch (final NumberFormatException e) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("set", "not_a_line")));
                        return true;
                    }

                    if (line > 3 || line < 0) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("set", "not_a_line")));
                        return true;
                    }

                    final String text = String.join(" ", Arrays.asList(args).subList(2, args.length));

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
                    return true;
                }
                case "edit" -> {
                    if (!sender.hasPermission(Constants.Permissions.EDIT)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("no_permission")));
                        return true;
                    }
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("player_only")));
                        return true;
                    }

                    if (this.userService.getUser(player).toggleEditEnabled()) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("edit", "enabled")));
                    } else {
                        sender.sendMessage(this.langConfig.c(NodePath.path("edit", "disabled")));
                    }
                    return true;
                }
                case "color" -> {
                    if (!sender.hasPermission(Constants.Permissions.COLOR)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("no_permission")));
                        return true;
                    }
                    if (!(sender instanceof final Player player)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("player_only")));
                        return true;
                    }

                    final User user = this.userService.getUser(player);

                    if (args.length >= 2) {
                        final User.FormattingType formattingType;
                        try {
                            formattingType = User.FormattingType.valueOf(args[1]);
                        } catch (final IllegalArgumentException e) {
                            sender.sendMessage(this.langConfig.c(NodePath.path("color", "invalid")));
                            return true;
                        }

                        user.formattingType(formattingType);
                        sender.sendMessage(this.langConfig.c(
                                NodePath.path("color", "set"),
                                Map.of("formatting_type", formattingType.toString())
                        ));
                        return true;
                    }

                    if (user.toggleColorEnabled()) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("color", "enabled")));
                    } else {
                        sender.sendMessage(this.langConfig.c(NodePath.path("color", "disabled")));
                    }
                    return true;
                }
                case "reload" -> {
                    if (!sender.hasPermission(Constants.Permissions.RELOAD)) {
                        sender.sendMessage(this.langConfig.c(NodePath.path("no_permission")));
                        return true;
                    }

                    this.yetAnotherSignEditor.loadConfigs();
                    sender.sendMessage(this.langConfig.c(NodePath.path("reload")));
                    return true;
                }
                default -> {
                }
            }
        }

        for (final Component component : this.langConfig.cl(NodePath.path("help"))) {
            sender.sendMessage(component);
        }
        return true;
    }


    @Override
    public @NotNull List<String> onTabComplete(
            @NotNull final CommandSender sender,
            @NotNull final Command command,
            @NotNull final String label,
            final @NotNull String[] args
    ) {
        final List<String> completions = new ArrayList<>();
        final List<String> possibilities = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission(Constants.Permissions.SET)) {
                possibilities.add("set");
            }
            if (sender.hasPermission(Constants.Permissions.EDIT)) {
                possibilities.add("edit");
            }
            if (sender.hasPermission(Constants.Permissions.COLOR)) {
                possibilities.add("color");
            }
            if (sender.hasPermission(Constants.Permissions.RELOAD)) {
                possibilities.add("reload");
            }

            StringUtil.copyPartialMatches(args[0], possibilities, completions);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set") && sender.hasPermission(Constants.Permissions.SET)) {
                possibilities.add("1");
                possibilities.add("2");
                possibilities.add("4");
                possibilities.add("3");

                StringUtil.copyPartialMatches(args[1], possibilities, completions);
            } else if (args[0].equalsIgnoreCase("color") && sender.hasPermission(Constants.Permissions.COLOR)) {
                if (sender.hasPermission(Constants.Permissions.MINI_MESSAGE)) {
                    possibilities.add("MINI_MESSAGE");
                }
                if (sender.hasPermission(Constants.Permissions.LEGACY)) {
                    possibilities.add("LEGACY");
                }

                StringUtil.copyPartialMatches(args[1], possibilities, completions);
            }
        }

        Collections.sort(completions);
        return completions;
    }

}
