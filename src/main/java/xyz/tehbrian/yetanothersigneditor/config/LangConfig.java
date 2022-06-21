package xyz.tehbrian.yetanothersigneditor.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.tehbrian.tehlib.paper.configurate.AbstractLangConfig;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

public class LangConfig extends AbstractLangConfig<YamlConfigurateWrapper> {

    /**
     * @param dataFolder the data folder
     */
    @Inject
    public LangConfig(final @NonNull @Named("dataFolder") Path dataFolder) {
        super(new YamlConfigurateWrapper(dataFolder.resolve("lang.yml")));
    }

}
