package dev.tehbrian.yetanothersigneditor.inject;

import com.google.inject.AbstractModule;
import dev.tehbrian.yetanothersigneditor.config.LangConfig;
import dev.tehbrian.yetanothersigneditor.user.UserService;
import dev.tehbrian.restrictionhelper.spigot.SpigotRestrictionHelper;

public final class SingletonModule extends AbstractModule {

	@Override
	protected void configure() {
		this.bind(UserService.class).asEagerSingleton();
		this.bind(LangConfig.class).asEagerSingleton();
		this.bind(SpigotRestrictionHelper.class).asEagerSingleton();
	}

}
