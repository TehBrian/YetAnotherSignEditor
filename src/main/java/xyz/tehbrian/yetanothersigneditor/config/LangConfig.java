package xyz.tehbrian.yetanothersigneditor.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import dev.tehbrian.tehlib.paper.configurate.AbstractLangConfig;

import java.nio.file.Path;

public final class LangConfig extends AbstractLangConfig<YamlConfigurateWrapper> {

  /**
   * @param dataFolder the data folder
   */
  @Inject
  public LangConfig(final @Named("dataFolder") Path dataFolder) {
    super(new YamlConfigurateWrapper(dataFolder.resolve("lang.yml")));
  }

}
