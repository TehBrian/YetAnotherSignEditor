package xyz.tehbrian.yetanothersigneditor.user;

import dev.tehbrian.tehlib.paper.user.PaperUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.tehbrian.yetanothersigneditor.Constants;

import java.util.Objects;
import java.util.UUID;

public final class User extends PaperUser {

    private boolean editEnabled;
    private boolean colorEnabled;
    private @NonNull Formatting formatting = Formatting.LEGACY;

    public User(final @NonNull UUID uuid) {
        super(uuid);

        final @NonNull Player player = Objects.requireNonNull(this.getPlayer());
        this.editEnabled = player.hasPermission(Constants.Permissions.EDIT);
        this.colorEnabled = player.hasPermission(Constants.Permissions.COLOR);
    }

    public @Nullable Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public boolean editEnabled() {
        return this.editEnabled;
    }

    public void editEnabled(final boolean editEnabled) {
        this.editEnabled = editEnabled;
    }

    public boolean toggleEditEnabled() {
        this.editEnabled(!this.editEnabled());
        return this.editEnabled();
    }

    public boolean colorEnabled() {
        return this.colorEnabled;
    }

    public void colorEnabled(final boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public boolean toggleColorEnabled() {
        this.colorEnabled(!this.colorEnabled());
        return this.colorEnabled();
    }

    public @NonNull Formatting formatting() {
        return this.formatting;
    }

    public void formatting(final @NonNull Formatting formatting) {
        this.formatting = formatting;
    }

    public enum Formatting {
        LEGACY,
        MINI_MESSAGE
    }

}
