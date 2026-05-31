package dev.tehbrian.yetanothersigneditor;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.agna.paper.configurate.ConfigLoader;
import dev.tehbrian.agna.paper.configurate.ConfigLoader.Loadable;
import dev.tehbrian.mayi.paper.PaperMayi;
import dev.tehbrian.mayi.paper.PaperRestrictionLoader;
import dev.tehbrian.mayi.paper.restrictions.R_PlotSquared_6_7;
import dev.tehbrian.mayi.paper.restrictions.R_WorldGuard_7;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.inject.PluginModule;
import dev.tehbrian.yetanothersigneditor.inject.SingletonModule;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static dev.tehbrian.agna.paper.PluginUtils.disableSelf;
import static dev.tehbrian.agna.paper.PluginUtils.registerListeners;

/**
 * The main class for the YetAnotherSignEditor plugin.
 */
public final class YetAnotherSignEditor extends JavaPlugin {

	private @MonotonicNonNull PaperCommandManager<CommandSender> commandManager = null;
	private @MonotonicNonNull Injector injector = null;

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
			disableSelf(this);
			this.getSLF4JLogger().error("Printing stack trace, please send this to the developers:", e);
			return;
		}

		if (!this.loadConfiguration()) {
			disableSelf(this);
			return;
		}
		if (!this.setupCommands()) {
			disableSelf(this);
			return;
		}

		this.setupRestrictions();

		registerListeners(this, this.injector.getInstance(SignListener.class));
	}

	/**
	 * Loads the plugin's configuration. If an exception is caught, logs the
	 * error and returns false.
	 *
	 * @return whether it was successful
	 */
	public boolean loadConfiguration() {
		return new ConfigLoader(this).load(List.of(
				Loadable.ofVersioned("lang.yml", this.injector.getInstance(LangConfig.class), 1)
		));
	}

	/**
	 * @return whether it was successful
	 */
	private boolean setupCommands() {
		if (this.commandManager != null) {
			throw new IllegalStateException("The CommandManager is already instantiated.");
		}

		try {
			this.commandManager = new PaperCommandManager<>(
					this,
					CommandExecutionCoordinator.simpleCoordinator(),
					Function.identity(),
					Function.identity()
			);
		} catch (final Exception e) {
			this.getSLF4JLogger().error("Failed to create the CommandManager.");
			this.getSLF4JLogger().error("Printing stack trace, please send this to the developers:", e);
			return false;
		}

		new MinecraftExceptionHandler<CommandSender>()
				.withArgumentParsingHandler()
				.withInvalidSenderHandler()
				.withInvalidSyntaxHandler()
				.withNoPermissionHandler()
				.withCommandExecutionHandler()
				.apply(this.commandManager, AudienceProvider.nativeAudience());

		this.injector.getInstance(MainCommand.class).register(this.commandManager);

		return true;
	}

	private void setupRestrictions() {
		final var loader = new PaperRestrictionLoader(
				this.getSLF4JLogger(),
				Arrays.asList(this.getServer().getPluginManager().getPlugins()),
				List.of(R_PlotSquared_6_7.class, R_WorldGuard_7.class)
		);

		loader.load(this.injector.getInstance(PaperMayi.class));
	}

}
