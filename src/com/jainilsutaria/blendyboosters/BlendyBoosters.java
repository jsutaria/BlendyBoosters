package com.jainilsutaria.blendyboosters;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.permission.Permission;

public class BlendyBoosters extends JavaPlugin implements Listener{
	FileConfiguration config = this.getConfig();
	public static Permission permission;
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		getServer().getPluginManager().registerEvents(this, this); //On Player Join
		BukkitScheduler scheduler = getServer().getScheduler();
		RegisteredServiceProvider<Permission>permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		permission = permissionProvider.getProvider();
        scheduler.runTaskTimerAsynchronously(this, new Runnable() {
            @Override
            public void run() {
				List<String> finished = new ArrayList<String>();
        		for (String key : config.getKeys(false)) {
        			if (config.getInt(key + ".timeLeft") != 0) {
        				if(config.getInt(key + ".timeLeft") == 1) finished.add(key);
        				config.set(key + ".timeLeft", config.getInt(key+".timeLeft") - 1);
        				saveConfig();
        				if (config.getInt(key + ".timeLeft") == 0) {
    						for (Player p : getServer().getOnlinePlayers()) {
    						    try {
    						    	permission.playerRemove(p, config.getString(key + ".node"));
    						    } catch (Exception e) {}
    						}
        				}
        			} else {
        				for (Player p : getServer().getOnlinePlayers()) {
						    try {
						    	permission.playerRemove(p, config.getString(key + ".node"));
						    } catch (Exception e) {}
						}
        			}
        		}
        		for(String booster : finished) {
					for (Player p : getServer().getOnlinePlayers()) {
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
								"&8[&2BlendyBoosters&8] &3The booster &4" + booster.replaceAll("_", " ") +
								" &3has now expired!"));
					}
        		}
            }
        }, 0L, 60 * 20L);
	}
	
	
	@Override 
	public void onDisable() {
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (cmd.getName().equals("booster")) {
			if (args.length == 0) {

				sender.sendMessage(ChatColor.LIGHT_PURPLE + "-=-=-=-=Boosters=-=-=-=-");
				sender.sendMessage(ChatColor.RED + "Red boosters are currently enabled");
				for (String key : config.getKeys(false)) {
					if (config.getInt(key + ".timeLeft") != 0) {
						sender.sendMessage(ChatColor.RED + key);
						sender.sendMessage("  " + ChatColor.AQUA + config.getInt(key + ".timeLeft") + " minutes remaining");
					} else {
						sender.sendMessage(ChatColor.DARK_AQUA + key);
					}
					if (sender.isOp() || sender instanceof ConsoleCommandSender) {
						sender.sendMessage("  " + ChatColor.BLUE + config.getString(key + ".node"));
					}
				}
				return true;
			} 
			if (!sender.hasPermission("booster.add")) return true;
			if (args.length >= 2) {
				if (config.getKeys(false).contains(args[0])) {
					try {
						if (Integer.parseInt(args[1]) <= 0 ) {
							sender.sendMessage("Error, must be greater than 0 minutes");
							return true;
						}
						config.set(args[0] + ".timeLeft", config.getInt(args[0] + ".timeLeft") + Integer.parseInt(args[1]));
						saveConfig();
						for (Player p : getServer().getOnlinePlayers()) {
							permission.playerAdd(p, config.getString(args[0] + ".node"));
						}
						if (args.length == 3) {
							for (Player p : getServer().getOnlinePlayers()) {
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', 
										"&8[&2BlendyBoosters&8] &3Thank you to &a" + args[2] + 
												("&3 for donating to give everybody a &a" + args[0] + 
												" booster&3 for " + args[1] + " minutes!")).replaceAll("_", " "));
							}
						}
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage("Error, not a number");
						return true;
					}
				} else {
					sender.sendMessage("Error, no booster found");
					return true;
				}
			} else {
				return false;
			}
		}
		return false;
	}
	
	@EventHandler
    public void onPlayerJoinEvent(final PlayerJoinEvent event) {
		BukkitScheduler sch = getServer().getScheduler();
		sch.runTaskLater(this, new Runnable() {
			@Override
			public void run() {
				event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "-=-=-=-=Current Boosters=-=-=-=-");
				for (String key : config.getKeys(false)) {
					if (config.getInt(key + ".timeLeft") != 0) {
						permission.playerAdd(event.getPlayer(), config.getString(key + ".node"));
						event.getPlayer().sendMessage(ChatColor.RED + key + " booster");
						event.getPlayer().sendMessage("  " + ChatColor.AQUA + config.getInt(key + ".timeLeft") + " minutes remaining");
					}
					
					
					
				}
			}
		}, 20L);
	}	
	@EventHandler
    public void onPlayerQuitEvent(final PlayerQuitEvent event) {
		for(String key : config.getKeys(false)) {
			permission.playerRemove(event.getPlayer(), config.getString(key + ".node"));
		}
	}	
}
