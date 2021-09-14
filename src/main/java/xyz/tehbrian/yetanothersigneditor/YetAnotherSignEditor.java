package xyz.tehbrian.yetanothersigneditor;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.tehbrian.tehlib.paper.TehPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionLoader;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_PlotSquared_6_1;
import xyz.tehbrian.restrictionhelper.spigot.restrictions.R_WorldGuard_7_0;
import xyz.tehbrian.yetanothersigneditor.commands.YaseCommand;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.inject.ConfigModule;
import xyz.tehbrian.yetanothersigneditor.inject.PluginModule;
import xyz.tehbrian.yetanothersigneditor.inject.RestrictionHelperModule;
import xyz.tehbrian.yetanothersigneditor.inject.UserModule;
import xyz.tehbrian.yetanothersigneditor.listeners.SignListener;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

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
                    new ConfigModule(),
                    new PluginModule(this),
                    new RestrictionHelperModule(),
                    new UserModule()
            );
        } catch (final Exception e) {
            this.getLogger().severe("Something went wrong while creating the Guice injector.");
            this.getLogger().severe("Disabling plugin.");
            this.getLogger().log(Level.SEVERE, "Printing stack trace, please send this to the developers:", e);
            this.setEnabled(false);
            return;
        }

        this.loadConfigs();
        this.setupRestrictions();

        registerListeners(
                this.injector.getInstance(SignListener.class)
        );

        final var yaseCommand = this.injector.getInstance(YaseCommand.class);
        this.registerCommand("yase", yaseCommand, yaseCommand);
    }

    /**
     * Loads the various plugin config files.
     */
    public void loadConfigs() {
        this.saveResource("lang.yml", false);

        this.injector.getInstance(LangConfig.class).load();
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
