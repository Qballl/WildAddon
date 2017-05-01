package me.qball.wildaddon;

import me.Qball.Wild.Utils.Region;
import me.Qball.Wild.Wild;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;


public class PortalEnter implements Listener {
    private Wild plugin = Wild.getInstance();
    private WildAddon addon;

    public PortalEnter(WildAddon addon) {
        this.addon = addon;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent e) {
        for (String name : plugin.portals.keySet()) {
            String portal = plugin.portals.get(name);
            String[] info = portal.split(":");
            if (e.getTo().getWorld().getName().equals(info[0])) {
                Wild.cancel.add(e.getPlayer().getUniqueId());
                String[] max = info[1].split(",");
                String[] min = info[2].split(",");
                Vector maxVec = new Vector(Integer.parseInt(max[0]), Integer.parseInt(max[1]), Integer.parseInt(max[2]));
                Vector minVec = new Vector(Integer.parseInt(min[0]), Integer.parseInt(min[1]), Integer.parseInt(min[2]));
                Region region = new Region(maxVec, minVec);
                Vector vec = new Vector(e.getTo().getBlockX(), e.getTo().getBlockY(), e.getTo().getBlockZ());
                if (region.contains(vec)) {
                    if(info.length>=4){
                        if(e.getPlayer().hasPermission("wild.wildtp.biome."+info[3].toLowerCase()))
                            plugin.biome.put(e.getPlayer().getUniqueId(), Biome.valueOf(info[3].toUpperCase()));
                    }
                    plugin.portalUsed.add(e.getPlayer().getUniqueId());
                    addon.checkPerms(e.getPlayer());
                    break;
                }
            } else {
                Wild.cancel.remove(e.getPlayer().getUniqueId());
            }
        }
    }

}
