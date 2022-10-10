package xyz.tehbrian.yetanothersigneditor.user;

import dev.tehbrian.tehlib.paper.user.PaperUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.tehbrian.yetanothersigneditor.util.Permissions;

import java.util.Objects;
import java.util.UUID;

public final class User extends PaperUser {

  private boolean editEnabled;
  private boolean formatEnabled;
  private User.FormattingType formattingType = FormattingType.LEGACY;

  /**
   * @param uuid the unique identifier of the user
   */
  public User(final UUID uuid) {
    super(uuid);

    final Player player = Objects.requireNonNull(this.getPlayer());
    this.editEnabled = player.hasPermission(Permissions.EDIT);
    this.formatEnabled = player.hasPermission(Permissions.FORMAT);
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

  public boolean formatEnabled() {
    return this.formatEnabled;
  }

  public void formatEnabled(final boolean formatEnabled) {
    this.formatEnabled = formatEnabled;
  }

  public boolean toggleFormatEnabled() {
    this.formatEnabled(!this.formatEnabled());
    return this.formatEnabled();
  }

  public User.FormattingType formattingType() {
    return this.formattingType;
  }

  public void formattingType(final User.FormattingType formattingType) {
    this.formattingType = formattingType;
  }

  public enum FormattingType {
    LEGACY,
    MINIMESSAGE
  }

}
