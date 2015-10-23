package me.HeyAwesomePeople.AutoRestart;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoRestart extends JavaPlugin implements CommandExecutor {
    public AutoRestart instance;

    SimpleDateFormat format = new SimpleDateFormat("HH");

    private File file = new File(this.getDataFolder() + File.separator + "config.yml");

    public int pause = 0;

    public BukkitTask task = null;

    @Override
    public void onEnable() {
        instance = this;

        if (!file.exists()) {
            saveDefaultConfig();
            saveConfig();
        }

        task = restartTimer();
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (!sender.hasPermission("autorestart.admin")) {
            sender.sendMessage(ChatColor.RED + "[AutoRestart] No permissions.");
            return false;
        }
        if (commandLabel.equalsIgnoreCase("autorestart")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.BLUE + "AutoRestart only has one command, /autorestart reload");
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    reloadConfig();
                    sender.sendMessage(ChatColor.GOLD + "Configuration for AutoRestart reloaded!");
                }
            }
        }
        return false;
    }

    public BukkitTask restartTimer() {
        return Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                Calendar now = Calendar.getInstance();
                now.setTime(new Date());

                if (pause > 0) {
                    pause--;
                    return;
                }

                for (Date d : getFormattableDates()) {
                    Calendar ref = Calendar.getInstance();
                    ref.setTime(d);
                    Calendar cal = Calendar.getInstance();
                    cal.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DATE),
                            ref.get(Calendar.HOUR_OF_DAY), ref.get(Calendar.MINUTE), ref.get(Calendar.SECOND));

                    if (now.get(Calendar.HOUR_OF_DAY) < cal.get(Calendar.HOUR_OF_DAY)) {
                        for (String s : getConfig().getConfigurationSection("warnings").getKeys(false)) {
                            // s will be 1, 2, 3 ect

                            long seconds = (cal.getTime().getTime() - now.getTime().getTime()) / 1000;
                            if (getConfig().getInt("warnings." + s + ".timeLeft") == seconds) {
                                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("warnings." + s + ".broadcast")));
                                pause = 12;
                            }
                        }
                    }

                    if ((now.get(Calendar.HOUR_OF_DAY) == cal.get(Calendar.HOUR_OF_DAY))) {
                        if ((now.get(Calendar.MINUTE) == 0) && (now.get(Calendar.SECOND) == 0)) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
                        }
                    }
                }
            }
        }, 20L, 3L);
    }

    private List<Date> getFormattableDates() {
        String[] split = getConfig().getString("restarts").split(" ");
        List<Date> formattableDates = new ArrayList<Date>();
        for (String string : split) {
            if (isFormattableDate(string)) {
                try {
                    formattableDates.add(format.parse(string));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return formattableDates;
    }

    public boolean isFormattableDate(String s) {
        try {
            format.parse(s);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /* getting */


}
