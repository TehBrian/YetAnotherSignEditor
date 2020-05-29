package xyz.tehbrian.yetanothersigneditor.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.player.PlayerDataManager;
import xyz.tehbrian.yetanothersigneditor.util.MessageUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
    TODO: Cleanup this incoherent mess.
    It's spaghetti, redundant, and unreadable.
    Bukkit's command system is awful, but that's
    no excuse for this pile of garbage.
 */
public class YaseCommand implements CommandExecutor, TabCompleter {

    private final YetAnotherSignEditor main;

    public YaseCommand(YetAnotherSignEditor main) {
        this.main = main;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        PlayerDataManager playerDataManager = main.getPlayerDataManager();

        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "edit": {
                    if (!sender.hasPermission("yase.edit")) {
                        sender.sendMessage(MessageUtils.getMessage("messages.no_permission"));
                        break;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageUtils.getMessage("messages.player_only"));
                        break;
                    }
                    Player player = (Player) sender;

                    if (playerDataManager.getPlayerData(player).toggleEditEnabled()) {
                        sender.sendMessage(MessageUtils.getMessage("messages.edit.enabled"));
                    } else {
                        sender.sendMessage(MessageUtils.getMessage("messages.edit.disabled"));
                    }

                    break;
                }
                case "color": {
                    if (!sender.hasPermission("yase.color")) {
                        sender.sendMessage(MessageUtils.getMessage("messages.no_permission"));
                        break;
                    }
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(MessageUtils.getMessage("messages.player_only"));
                        break;
                    }
                    Player player = (Player) sender;

                    if (playerDataManager.getPlayerData(player).toggleColorEnabled()) {
                        sender.sendMessage(MessageUtils.getMessage("messages.color.enabled"));
                    } else {
                        sender.sendMessage(MessageUtils.getMessage("messages.color.disabled"));
                    }

                    break;
                }
                case "reload":
                    if (!sender.hasPermission("yase.reload")) {
                        sender.sendMessage(MessageUtils.getMessage("messages.no_permission"));
                        break;
                    }

                    main.reloadConfig();
                    sender.sendMessage(MessageUtils.getMessage("messages.reload"));

                    break;
                default:
                    MessageUtils.sendMessageList(sender, "messages.help");
            }
        } else {
            MessageUtils.sendMessageList(sender, "messages.help");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();

            if (sender.hasPermission("yase.edit")) {
                commands.add("edit");
            }
            if (sender.hasPermission("yase.color")) {
                commands.add("color");
            }
            if (sender.hasPermission("yase.reload")) {
                commands.add("reload");
            }

            StringUtil.copyPartialMatches(args[0], commands, completions);
        }

        Collections.sort(completions);
        return completions;
    }
}
