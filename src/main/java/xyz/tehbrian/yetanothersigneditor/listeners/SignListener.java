package xyz.tehbrian.yetanothersigneditor.listeners;

import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import xyz.tehbrian.yetanothersigneditor.YetAnotherSignEditor;
import xyz.tehbrian.yetanothersigneditor.util.MessageUtils;

import java.util.Objects;

public class SignListener implements Listener {

    private final YetAnotherSignEditor main;

    public SignListener(YetAnotherSignEditor main) {
        this.main = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!event.getPlayer().hasPermission("yase.edit")) return;
        if (!main.getPlayerDataManager().getPlayerData(player).hasEditEnabled()) return;

        if (!(Tag.SIGNS.isTagged(player.getInventory().getItemInMainHand().getType()))) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (player.getGameMode() == GameMode.ADVENTURE) return;
        if (player.isSneaking()) return;

        BlockState blockState = Objects.requireNonNull(event.getClickedBlock()).getState();
        if (!(blockState instanceof Sign)) return;
        Sign sign = (Sign) blockState;

        String[] lines = sign.getLines();
        for (int l = 0; l < lines.length; l++) {
            sign.setLine(l, lines[l].replace('§', '&'));
        }

        sign.update();
        player.openSign(sign);

        event.setCancelled(true);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();

        if (!event.getPlayer().hasPermission("yase.color")) return;
        if (!main.getPlayerDataManager().getPlayerData(player).hasColorEnabled()) return;

        String[] lines = event.getLines();
        for (int l = 0; l < lines.length; l++) {
            event.setLine(l, MessageUtils.color(lines[l]));
        }
    }
}
