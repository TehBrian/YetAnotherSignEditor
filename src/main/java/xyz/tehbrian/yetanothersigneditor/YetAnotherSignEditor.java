package xyz.tehbrian.yetanothersigneditor;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.tehbrian.yetanothersigneditor.commands.YaseCommand;
import xyz.tehbrian.yetanothersigneditor.listeners.SignListener;
import xyz.tehbrian.yetanothersigneditor.player.PlayerDataManager;

public final class YetAnotherSignEditor extends JavaPlugin {

    private static YetAnotherSignEditor instance;

    private PlayerDataManager playerDataManager;

    public static YetAnotherSignEditor getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        setupConfig();
        setupEvents();
        setupCommands();
    }

    private void setupConfig() {
        saveDefaultConfig();
    }

    private void setupEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new SignListener(this), this);
    }

    private void setupCommands() {
        YaseCommand yaseCommand = new YaseCommand(this);
        getCommand("yase").setExecutor(yaseCommand);
        getCommand("yase").setTabCompleter(yaseCommand);
    }

    public PlayerDataManager getPlayerDataManager() {
        if (playerDataManager == null) {
            playerDataManager = new PlayerDataManager();
        }
        return playerDataManager;
    }
}
