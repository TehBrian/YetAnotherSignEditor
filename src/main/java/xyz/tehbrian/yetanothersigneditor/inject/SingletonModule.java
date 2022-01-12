package xyz.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import xyz.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;
import xyz.tehbrian.yetanothersigneditor.command.CommandService;
import xyz.tehbrian.yetanothersigneditor.config.LangConfig;
import xyz.tehbrian.yetanothersigneditor.user.UserService;

public final class SingletonModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(CommandService.class).asEagerSingleton();
        this.bind(UserService.class).asEagerSingleton();
        this.bind(LangConfig.class).asEagerSingleton();
        this.bind(SpigotRestrictionHelper.class).asEagerSingleton();
    }

}
