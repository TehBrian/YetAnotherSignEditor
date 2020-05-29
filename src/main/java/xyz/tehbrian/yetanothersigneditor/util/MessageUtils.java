package xyz.tehbrian.yetanothersigneditor.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;

import java.util.List;

public class MessageUtils {

    private MessageUtils() {
    }

    public static String color(String string) {
        return string == null ? null : ChatColor.translateAlternateColorCodes('&', string);
    }

    public static String getMessage(String configKey) {
        return color(YetAnotherSignEditor.getInstance().getConfig().getString(configKey));
    }

    public static List<String> getMessageList(String configKey) {
        List<String> messages = YetAnotherSignEditor.getInstance().getConfig().getStringList(configKey);
        messages.replaceAll(MessageUtils::color);
        return messages;
    }

    public static void sendMessageList(CommandSender sender, String messageKey) {
        for (String message : getMessageList(messageKey)) {
            sender.sendMessage(message);
        }
    }
}
