package xyz.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;

/**
 * Guice module which provides the various configs.
 */
public class ConfigModule extends AbstractModule {

    /**
     * Binds the configs as eager singletons.
     */
    @Override
    protected void configure() {
        this.bind(LangConfig.class).asEagerSingleton();
    }

}
