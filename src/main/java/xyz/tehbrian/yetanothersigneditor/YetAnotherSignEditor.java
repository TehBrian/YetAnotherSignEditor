package xyz.tehbrian.yetanothersigneditor;

import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.tehlib.core.configurate.Config;
import dev.tehbrian.tehlib.paper.TehPlugin;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionLoader;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_PlotSquared_6_1;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_WorldGuard_7_0;
import xyz.tehbrian.yetanothersigneditor.command.CommandService;
import xyz.tehbrian.yetanothersigneditor.command.MainCommand;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.inject.CommandModule;
import xyz.tehbrian.yetanothersigneditor.inject.ConfigModule;
import xyz.tehbrian.yetanothersigneditor.inject.PluginModule;
import xyz.tehbrian.yetanothersigneditor.inject.RestrictionHelperModule;
import xyz.tehbrian.yetanothersigneditor.inject.UserModule;
import xyz.tehbrian.yetanothersigneditor.listeners.SignListener;

import java.util.Arrays;
import java.util.List;

/**
 * The main class for the YetAnotherSignEditor plugin.
 */
public final class YetAnotherSignEditor extends TehPlugin {

    /**
     * The Guice injector.
     */
    private @MonotonicNonNull Injector injector;

    /**
     * Called when the plugin is enabled.
     */
    @Override
    public void onEnable() {
        try {
            this.injector = Guice.createInjector(
                    new CommandModule(),
                    new ConfigModule(),
                    new PluginModule(this),
                    new RestrictionHelperModule(),
                    new UserModule()
            );
        } catch (final Exception e) {
            this.getLog4JLogger().error("Something went wrong while creating the Guice injector.");
            this.getLog4JLogger().error("Disabling plugin.");
            this.disableSelf();
            this.getLog4JLogger().error("Printing stack trace, please send this to the developers:", e);
            return;
        }

        if (!this.loadConfiguration()) {
            this.disableSelf();
            return;
        }
        this.setupCommands();
        this.setupRestrictions();

        registerListeners(
                this.injector.getInstance(SignListener.class)
        );
    }

    /**
     * Loads the plugin's configuration. If an exception is caught, logs the
     * error and returns false.
     *
     * @return whether the loading was successful
     */
    public boolean loadConfiguration() {
        this.saveResourceSilently("lang.yml");

        final List<Config> configsToLoad = List.of(
                this.injector.getInstance(LangConfig.class)
        );

        for (final Config config : configsToLoad) {
            try {
                config.load();
            } catch (final ConfigurateException e) {
                this.getLog4JLogger().error("Exception caught during config load for {}", config.configurateWrapper().filePath());
                this.getLog4JLogger().error("Please check your config.");
                this.getLog4JLogger().error("Printing stack trace:", e);
                return false;
            }
        }

        this.getLog4JLogger().info("Successfully loaded configuration.");
        return true;
    }

    private void setupCommands() {
        final @NonNull CommandService commandService = this.injector.getInstance(CommandService.class);
        commandService.init();

        final @Nullable PaperCommandManager<CommandSender> commandManager = commandService.get();
        if (commandManager == null) {
            this.getLog4JLogger().error("The CommandService was null after initialization!");
            this.getLog4JLogger().error("Disabling plugin.");
            this.disableSelf();
            return;
        }

        new MinecraftExceptionHandler<CommandSender>()
                .withArgumentParsingHandler()
                .withInvalidSenderHandler()
                .withInvalidSyntaxHandler()
                .withNoPermissionHandler()
                .withCommandExecutionHandler()
                .apply(commandManager, s -> s);

        this.injector.getInstance(MainCommand.class).register(commandManager);
    }

    private void setupRestrictions() {
        final var loader = new SpigotRestrictionLoader(
                this.getLog4JLogger(),
                Arrays.asList(this.getServer().getPluginManager().getPlugins()),
                List.of(R_PlotSquared_6_1.class, R_WorldGuard_7_0.class)
        );

        loader.load(this.injector.getInstance(SpigotRestrictionHelper.class));
    }

}
