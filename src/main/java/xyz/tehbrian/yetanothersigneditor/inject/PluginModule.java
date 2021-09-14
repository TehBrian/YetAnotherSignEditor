package xyz.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.logging.log4j.Logger;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;

import java.nio.file.Path;

/**
 * Guice module which provides bindings for the plugin's instances.
 */
public final class PluginModule extends AbstractModule {

    private final YetAnotherSignEditor yetAnotherSignEditor;

    /**
     * @param yetAnotherSignEditor YetAnotherSignEditor reference
     */
    public PluginModule(final @NonNull YetAnotherSignEditor yetAnotherSignEditor) {
        this.yetAnotherSignEditor = yetAnotherSignEditor;
    }

    @Override
    protected void configure() {
        this.bind(YetAnotherSignEditor.class).toInstance(this.yetAnotherSignEditor);
        this.bind(JavaPlugin.class).toInstance(this.yetAnotherSignEditor);
    }

    /**
     * Provides the plugin's Log4J logger.
     *
     * @return the plugin's Log4J logger
     */
    @Provides
    public @NonNull Logger provideLog4JLogger() {
        return this.yetAnotherSignEditor.getLog4JLogger();
    }

    /**
     * Provides the plugin's data folder.
     *
     * @return the data folder
     */
    @Provides
    @Named("dataFolder")
    public @NonNull Path provideDataFolder() {
        return this.yetAnotherSignEditor.getDataFolder().toPath();
    }

}
