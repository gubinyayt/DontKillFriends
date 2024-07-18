package ru.gubin.dontkillfriends;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DontKillFriends extends JavaPlugin implements Listener, TabExecutor, TabCompleter {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        getLogger().info("DontKillFriends enabled!");
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("dontkillfriends").setExecutor(this);
        getCommand("dontkillfriends").setTabCompleter(this);
        getCommand("dkf").setExecutor(this);
        getCommand("dkf").setTabCompleter(this);
        saveDefaultConfig();
        config = getConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("DontKillFriends disabled!");
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Tameable) {
            Tameable pet = (Tameable) event.getEntity();
            if (pet.isTamed() && pet.getOwner() instanceof Player) {
                Player owner = (Player) pet.getOwner();
                if (event.getDamager() instanceof Player) {
                    Player damager = (Player) event.getDamager();
                    if (!damager.hasPermission("dontkillfriends.bypass") && !damager.equals(owner)) {
                        event.setCancelled(true);
                        damager.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("hit-message")));
                    } else if (damager.equals(owner) && !config.getBoolean("immortal-from-host")) {
                        event.setCancelled(false);
                    } else if (damager.equals(owner) && config.getBoolean("immortal-from-host")) {
                        event.setCancelled(true);
                    }
                }
                if (isImmortal(event.getCause())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Tameable) {
            Tameable pet = (Tameable) event.getEntity();
            if (pet.isTamed() && pet.getOwner() instanceof Player) {
                if (isImmortal(event.getCause())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private boolean isImmortal(EntityDamageEvent.DamageCause cause) {
        switch (cause) {
            case ENTITY_EXPLOSION:
                return config.getBoolean("immortal-from-explosion");
            case DROWNING:
                return config.getBoolean("immortal-from-drowning");
            case SUFFOCATION:
                return config.getBoolean("immortal-from-suffocation");
            case FIRE:
            case FIRE_TICK:
            case LAVA:
                return config.getBoolean("immortal-from-fire");
            case LIGHTNING:
                return config.getBoolean("immortal-from-lightning");
            case FALL:
                return config.getBoolean("immortal-from-fall");
            case DRAGON_BREATH:
                return config.getBoolean("immortal-from-dragon");
            case PROJECTILE:
                return config.getBoolean("immortal-from-projectile");
            case MAGIC:
                return config.getBoolean("immortal-from-magic");
            default:
                return false;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("dontkillfriends.use")) {
                player.sendMessage(ChatColor.RED + "You don't have permissions.");
                return true;
            }
            if (args.length == 0) {
                player.sendMessage(ChatColor.RED + "Usage: /" + label + " <version|author|reload>");
                return true;
            }
            if (args[0].equalsIgnoreCase("version")) {
                if (player.hasPermission("dontkillfriends.version")) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("plugin-version")));
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permissions.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("author")) {
                if (player.hasPermission("dontkillfriends.author")) {
                    player.sendMessage(ChatColor.GOLD + "Author: Gubin");
                    player.sendMessage(ChatColor.GOLD + "Author's discord: @gu.b");
                    player.sendMessage(ChatColor.GOLD + "Discord Group (RU): https://discord.gg/B6QCGttPtq");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permissions.");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("dontkillfriends.reload")) {
                    reloadConfig();
                    config = getConfig();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("reload-message")));
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permissions.");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (config.getBoolean("commandTabComplete")) {
            if (args.length == 1) {
                return Arrays.asList("version", "author", "reload");
            }
        }
        return new ArrayList<>();
    }
}
