package dev.tehbrian.yetanothersigneditor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.agna.paper.UpdateChecker;
import dev.tehbrian.agna.paper.configurate.ConfigLoader;
import dev.tehbrian.agna.paper.configurate.ConfigLoader.Loadable;
import dev.tehbrian.mayi.paper.PaperMayi;
import dev.tehbrian.mayi.paper.PaperRestrictionLoader;
import dev.tehbrian.mayi.paper.restrictions.R_PlotSquared_6_7;
import dev.tehbrian.mayi.paper.restrictions.R_WorldGuard_7;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.inject.PluginModule;
import dev.tehbrian.yetanothersigneditor.inject.SingletonModule;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.Arrays;
import java.util.List;

import static dev.tehbrian.agna.paper.PluginUtils.disableSelf;
import static dev.tehbrian.agna.paper.PluginUtils.registerListeners;
import static org.incendo.cloud.execution.ExecutionCoordinator.simpleCoordinator;
import static org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper.simpleSenderMapper;

/**
 * The main class for the YetAnotherSignEditor plugin.
 */
public final class YetAnotherSignEditor extends JavaPlugin {

	private static final int BSTATS_PLUGIN_ID = 31709;

	private @MonotonicNonNull PaperCommandManager<Source> commandManager = null;
	private @MonotonicNonNull Injector injector = null;

	@Override
	public void onEnable() {
		try {
			this.injector = Guice.createInjector(
					new PluginModule(this),
					new SingletonModule()
			);
		} catch (final Exception e) {
			this.getSLF4JLogger().error("Something went wrong while creating the injector. Disabling plugin");
			disableSelf(this);
			this.getSLF4JLogger().error("Printing stack trace. Please send this to the developers", e);
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

		// initialize bStats.
		Metrics _ = new Metrics(this, BSTATS_PLUGIN_ID);

		new UpdateChecker(this, "yetanothersigneditor").checkForUpdates();
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

		this.commandManager = PaperCommandManager
				.builder(simpleSenderMapper())
				.executionCoordinator(simpleCoordinator())
				.buildOnEnable(this);

		MinecraftExceptionHandler.create(Source::source)
				.defaultArgumentParsingHandler()
				.defaultInvalidSenderHandler()
				.defaultInvalidSyntaxHandler()
				.defaultNoPermissionHandler()
				.defaultCommandExecutionHandler()
				.registerTo(this.commandManager);

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
