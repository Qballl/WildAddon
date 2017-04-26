package me.qball.wildaddon;


import me.Qball.Wild.Wild;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class SignClick implements Listener {
    private WildAddon addon;

    public SignClick(WildAddon addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignClick(PlayerInteractEvent e) {
        Sign sign;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }
        if (e.getClickedBlock().getState() instanceof Sign) {
            sign = (Sign) e.getClickedBlock().getState();
            if (sign.getLine(1).equalsIgnoreCase("[§1Wild§0]") && sign.getLine(0).equalsIgnoreCase("§4====================")) {
                Wild.cancel.add(e.getPlayer().getUniqueId());
                addon.checkPerms(e.getPlayer());
            }
        }
    }
}
