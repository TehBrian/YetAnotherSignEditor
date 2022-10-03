package xyz.tehbrian.yetanothersigneditor;

import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.tehlib.core.configurate.Config;
import dev.tehbrian.tehlib.paper.TehPlugin;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionLoader;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_PlotSquared_6;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_WorldGuard_7;
import xyz.tehbrian.yetanothersigneditor.command.CommandService;
import xyz.tehbrian.yetanothersigneditor.command.MainCommand;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.inject.PluginModule;
import xyz.tehbrian.yetanothersigneditor.inject.SingletonModule;
import xyz.tehbrian.yetanothersigneditor.listeners.SignListener;

import java.util.Arrays;
import java.util.List;

/**
 * The main class for the YetAnotherSignEditor plugin.
 */
public final class YetAnotherSignEditor extends TehPlugin {

  private @MonotonicNonNull Injector injector;

  @Override
  public void onEnable() {
    try {
      this.injector = Guice.createInjector(
          new PluginModule(this),
          new SingletonModule()
      );
    } catch (final Exception e) {
      this.getSLF4JLogger().error("Something went wrong while creating the Guice injector.");
      this.getSLF4JLogger().error("Disabling plugin.");
      this.disableSelf();
      this.getSLF4JLogger().error("Printing stack trace, please send this to the developers:", e);
      return;
    }

    if (!this.loadConfiguration()) {
      this.disableSelf();
      return;
    }
    if (!this.setupCommands()) {
      this.disableSelf();
      return;
    }

    this.setupRestrictions();

    registerListeners(
        this.injector.getInstance(SignListener.class)
    );
  }

  /**
   * Loads the plugin's configuration. If an exception is caught, logs the
   * error and returns false.
   *
   * @return whether it was successful
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
        this.getSLF4JLogger().error(
            "Exception caught during config load for {}",
            config.configurateWrapper().filePath()
        );
        this.getSLF4JLogger().error("Please check your config.");
        this.getSLF4JLogger().error("Printing stack trace:", e);
        return false;
      }
    }

    this.getSLF4JLogger().info("Successfully loaded configuration.");
    return true;
  }

  /**
   * @return whether it was successful
   */
  private boolean setupCommands() {
    final CommandService commandService = this.injector.getInstance(CommandService.class);
    try {
      commandService.init();
    } catch (final Exception e) {
      this.getSLF4JLogger().error("Failed to create the CommandManager.");
      this.getSLF4JLogger().error("Printing stack trace, please send this to the developers:", e);
      return false;
    }

    final @Nullable PaperCommandManager<CommandSender> commandManager = commandService.get();
    if (commandManager == null) {
      this.getSLF4JLogger().error("The CommandService was null after initialization!");
      return false;
    }

    new MinecraftExceptionHandler<CommandSender>()
        .withArgumentParsingHandler()
        .withInvalidSenderHandler()
        .withInvalidSyntaxHandler()
        .withNoPermissionHandler()
        .withCommandExecutionHandler()
        .apply(commandManager, AudienceProvider.nativeAudience());

    this.injector.getInstance(MainCommand.class).register(commandManager);

    return true;
  }

  private void setupRestrictions() {
    final var loader = new SpigotRestrictionLoader(
        this.getSLF4JLogger(),
        Arrays.asList(this.getServer().getPluginManager().getPlugins()),
        List.of(R_PlotSquared_6.class, R_WorldGuard_7.class)
    );

    loader.load(this.injector.getInstance(SpigotRestrictionHelper.class));
  }

}
