package xyz.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;

import java.nio.file.Path;

@SuppressWarnings("unused")
public final class PluginModule extends AbstractModule {

  private final YetAnotherSignEditor yetAnotherSignEditor;

  public PluginModule(final YetAnotherSignEditor yetAnotherSignEditor) {
    this.yetAnotherSignEditor = yetAnotherSignEditor;
  }

  @Override
  protected void configure() {
    this.bind(YetAnotherSignEditor.class).toInstance(this.yetAnotherSignEditor);
    this.bind(JavaPlugin.class).toInstance(this.yetAnotherSignEditor);
  }

  /**
   * @return the plugin's SLF4J logger
   */
  @Provides
  public Logger provideSLF4JLogger() {
    return this.yetAnotherSignEditor.getSLF4JLogger();
  }

  /**
   * @return the plugin's data folder
   */
  @Provides
  @Named("dataFolder")
  public Path provideDataFolder() {
    return this.yetAnotherSignEditor.getDataFolder().toPath();
  }

}
