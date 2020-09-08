package xyz.tehbrian.yetanothersigneditor.util;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    public static final Pattern HEX_PATTERN = Pattern.compile("&(#[A-Fa-f0-9]{6})");

    private MessageUtils() {
    }

    public static String color(String string) {
        return string == null ? null : replaceHex(ChatColor.translateAlternateColorCodes('&', string));
    }

    public static String replaceHex(String str) {
        Matcher matcher = HEX_PATTERN.matcher(str);
        while (matcher.find()) {
            str = str.replace(matcher.group(0), ChatColor.of(matcher.group(1)).toString());
        }
        return str;
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
