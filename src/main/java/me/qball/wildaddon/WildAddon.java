package me.qball.wildaddon;

import me.Qball.Wild.Utils.*;
import me.Qball.Wild.Wild;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WildAddon extends JavaPlugin implements Listener {
    private static Wild wild;
    private Economy econ = null;
    private int cost;
    private String costMSG;
    private String strCost;
    private String costMsg;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getPluginManager().registerEvents(new SignClick(this), this);
        this.getServer().getPluginManager().registerEvents(new PortalEnter(this), this);
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        setupEconomy();
        Plugin plug = Bukkit.getPluginManager().getPlugin("Wild");
        if (plug != null && plug.isEnabled()) {
            wild = (Wild) plug;
            cost = wild.getConfig().getInt("Cost");
            costMsg = wild.getConfig().getString("Costmsg");
            strCost = String.valueOf(wild.getConfig().getInt("Cost"));
            costMSG = costMsg.replaceAll("\\{cost}", strCost);

        }


    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer()
                .getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Plugin) this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWildCommand(PlayerCommandPreprocessEvent e) {
        String[] command = e.getMessage().split(" ");
        if (command[0].equalsIgnoreCase("/wild")||
                e.getMessage().toLowerCase().startsWith("/wilderness")||
                e.getMessage().toLowerCase().startsWith("/rtp")) {
            if(command.length>=2){
                try{
                    if(e.getPlayer().hasPermission("wild.wildtp.biome."+command[1])) {
                        Biome biome = Biome.valueOf(command[1].toUpperCase());
                        Wild.getInstance().biome.put(e.getPlayer().getUniqueId(), biome);
                        checkPerms(e.getPlayer());
                        e.setCancelled(true);
                    }else{
                        checkPerms(e.getPlayer());
                        e.setCancelled(true);
                    }
                }catch(IllegalArgumentException ex){
                    e.getPlayer().sendMessage(ChatColor.RED+"Invalid biome type");
                }
            }
            checkPerms(e.getPlayer());
            e.setCancelled(true);
        }
    }

    private void saveLoc(Player p) {
        WildTpBack wildTpBack = new WildTpBack();
        wildTpBack.saveLoc(p, p.getLocation());
    }

   public void checkPerms(Player p) {
        if(!p.hasPermission("wild.wildtp")){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', wild.getConfig().getString("NoPerm")));
            return;
        }
        if (p.hasPermission("wild.wildtp.cost.bypass") && p.hasPermission("wild.wildtp.cooldown.bypass")) {
            saveLoc(p);
            getWorld(p);
        } else if (p.hasPermission("wild.wildtp.cost.bypass") && !p.hasPermission("wild.wildtp.cooldown.bypass")) {
            if (Wild.check(p)) {
                saveLoc(p);
                getWorld(p);
            }else {
                int cooldown = this.getConfig().getInt("Cooldown");
                String cool = String.valueOf(cooldown);
                String strCoolMsg = wild.getConfig().getString("Cooldownmsg");
                String coolMsg = "";
                if (cool.contains("{cool}"))
                    coolMsg = strCoolMsg.replaceAll("\\{cool}", cool);
                else if (cool.contains("{rem}")) {
                    coolMsg = cool.replaceAll("\\{rem}", Wild.getRem(p));
                }

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', coolMsg));
            }
        } else if (!p.hasPermission("wild.wildtp.cost.bypass") && p.hasPermission("wild.wildtp.cooldown.bypass")) {
            saveLoc(p);
            if (econ.getBalance(p) >= cost) {

                EconomyResponse r = econ.withdrawPlayer(p, cost);
                if (r.transactionSuccess()) {
                    getWorld(p);
                    if(wild.getConfig().getBoolean("DoCostMsg"))
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', costMSG));
                } else {
                    p.sendMessage(ChatColor.RED + "Something has gone wrong sorry but we will be unable to teleport you :( ");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You do not have enough money to use this command");
            }
        } else if (!p.hasPermission("wild.wildtp.cost.bypass") && !p.hasPermission("wild.wildtp.cooldown.bypass")) {
            if (Wild.check(p)) {
                saveLoc(p);
                if (econ.getBalance(p) >= cost) {

                    EconomyResponse r = econ.withdrawPlayer(p, cost);
                    if (r.transactionSuccess()) {
                        getWorld(p);
                        if(wild.getConfig().getBoolean("DoCostMsg"))
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', costMSG));
                    } else {
                        p.sendMessage(ChatColor.RED + "Something has gone wrong sorry but we will be unable to teleport you :( ");
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You do not have enough money to use this command");
                }
            } else {
                int cooldown = this.getConfig().getInt("Cooldown");
                String cool = String.valueOf(cooldown);
                String strCoolMsg = wild.getConfig().getString("Cooldownmsg");
                String coolMsg = "";
                if (cool.contains("{cool}"))
                    coolMsg = strCoolMsg.replaceAll("\\{cool}", cool);
                else if (cool.contains("{rem}")) {
                    coolMsg = cool.replaceAll("\\{rem}", Wild.getRem(p));
                }

                p.sendMessage(ChatColor.translateAlternateColorCodes('&', coolMsg));
            }
        }

    }

    private void getWorld(Player p) {
        GetRandomLocation random = new GetRandomLocation(wild);
        for(String oldWorld : this.getConfig().getStringList("WorldTo")) {
            String[] spilt = oldWorld.split(":");
            Checks check = new Checks(wild);
            if (p.getLocation().getWorld().getName().equals(spilt[0])) {
                String info = getWorldInformation(spilt[1]);
                Location loc = random.getRandomLoc(info, p);
                loc.setY(check.getSolidBlock(loc.getBlockX(), loc.getBlockZ(), spilt[1], p));
                wild.random(p, loc);
                return;
            }
        }
            String info = getWorldInformation(p.getWorld().getName());
            Location loc = random.getRandomLoc(info, p);
            wild.random(p, loc);
    }

    private String getWorldInformation(String world) {
        WorldInfo wInfo = new WorldInfo(wild);
        String minX = String.valueOf(wInfo.getMinX(world));
        String maxX = String.valueOf(wInfo.getMaxX(world));
        String minZ = String.valueOf(wInfo.getMinZ(world));
        String maxZ = String.valueOf(wInfo.getMaxZ(world));
        return world + ":" + minX + ":" + maxX + ":" + minZ + ":" + maxZ;
    }

}
