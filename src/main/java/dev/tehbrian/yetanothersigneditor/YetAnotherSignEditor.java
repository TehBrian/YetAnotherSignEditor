package dev.tehbrian.yetanothersigneditor;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import dev.tehbrian.restrictionhelper.spigot.SpigotRestrictionLoader;
import dev.tehbrian.restrictionhelper.spigot.restrictions.R_PlotSquared_6_7;
import dev.tehbrian.restrictionhelper.spigot.restrictions.R_WorldGuard_7;
import dev.tehbrian.tehlib.paper.TehPlugin;
import dev.tehbrian.tehlib.paper.configurate.ConfigLoader;
import dev.tehbrian.tehlib.paper.configurate.ConfigLoader.Loadable;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.inject.PluginModule;
import dev.tehbrian.yetanothersigneditor.inject.SingletonModule;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * The main class for the YetAnotherSignEditor plugin.
 */
public final class YetAnotherSignEditor extends TehPlugin {

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

		registerListeners(this.injector.getInstance(SignListener.class));
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
		final var loader = new SpigotRestrictionLoader(
				this.getSLF4JLogger(),
				Arrays.asList(this.getServer().getPluginManager().getPlugins()),
				List.of(R_PlotSquared_6_7.class, R_WorldGuard_7.class)
		);

		loader.load(this.injector.getInstance(SpigotRestrictionHelper.class));
	}

}
