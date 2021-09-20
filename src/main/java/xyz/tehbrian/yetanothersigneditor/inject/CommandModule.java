package xyz.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import xyz.tehbrian.yetanothersigneditor.command.CommandService;

/**
 * Guice module which provides bindings for {@link CommandService}.
 */
public class CommandModule extends AbstractModule {

    /**
     * Binds {@link CommandService} as an eager singleton.
     */
    @Override
    protected void configure() {
        this.bind(CommandService.class).asEagerSingleton();
    }

}
