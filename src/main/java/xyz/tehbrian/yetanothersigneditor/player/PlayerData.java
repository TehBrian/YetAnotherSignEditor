package xyz.tehbrian.yetanothersigneditor.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;

    private boolean editEnabled = false;
    private boolean colorEnabled = false;

    public PlayerData(final UUID uuid) {
        this.uuid = uuid;

        if (getPlayer().hasPermission("yase.edit")) {
            editEnabled = true;
        }

        if (getPlayer().hasPermission("yase.color")) {
            colorEnabled = true;
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean hasEditEnabled() {
        return editEnabled;
    }

    public void setEditEnabled(boolean editEnabled) {
        this.editEnabled = editEnabled;
    }

    public boolean toggleEditEnabled() {
        setEditEnabled(!hasEditEnabled());
        return hasEditEnabled();
    }

    public boolean hasColorEnabled() {
        return colorEnabled;
    }

    public void setColorEnabled(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    public boolean toggleColorEnabled() {
        setColorEnabled(!hasColorEnabled());
        return hasColorEnabled();
    }
}
