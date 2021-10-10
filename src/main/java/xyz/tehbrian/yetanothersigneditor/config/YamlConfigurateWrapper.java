package xyz.tehbrian.yetanothersigneditor.config;

import dev.tehbrian.tehlib.core.configurate.ConfigurateWrapper;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;

public class YamlConfigurateWrapper extends ConfigurateWrapper<YamlConfigurationLoader> {

    /**
     * @param filePath the file path for the config
     */
    public YamlConfigurateWrapper(final @NonNull Path filePath) {
        super(filePath, YamlConfigurationLoader.builder()
                .path(filePath)
                .build());
    }

    /**
     * @param filePath the file path for the config
     * @param loader   the loader
     */
    public YamlConfigurateWrapper(final @NonNull Path filePath, final @NonNull YamlConfigurationLoader loader) {
        super(filePath, loader);
    }

}
