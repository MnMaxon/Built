package me.MnMaxon.Built;

import me.MnMaxon.Utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/**
 * Created by MnMaxon on 8/8/2016.  Aren't I great?
 */
public class Cmds {
    public static void trigger(CommandSender sender, String[] args) {
        Player p;
        if (args.length == 1) {
            if (sender instanceof Player) p = (Player) sender;
            else {
                sender.sendMessage(ChatColor.RED + "Use like /Built Trigger [Name]");
                return;
            }
        } else {
            p = Bukkit.getPlayer(args[1]);
            if (p == null) {
                sender.sendMessage(ChatColor.RED + args[0] + " is not online!");
                return;
            }
        }
        p.getInventory().addItem(ItemUtils.getTrigger());
        p.sendMessage(ChatColor.GREEN + "You have received a trigger block!");
        if (!sender.equals(p)) sender.sendMessage(ChatColor.GREEN + p.getName() + " has received a trigger block!");
    }

    public static void reload(CommandSender sender) {
        Built.reloadConfigs();
        sender.sendMessage(ChatColor.GREEN + "Configs reloaded!");
    }

    public static void save(Player p, String[] args) {
        if (args.length != 2) p.sendMessage(ChatColor.RED + "Use like: /Built Save [name]");
        else try {
            Selection.get(p).save(args[1]);
            p.sendMessage(ChatColor.GREEN + "Building saved as " + args[1] + ".yml");
        } catch (Throwable ex) {
            p.sendMessage(ex.getMessage());
        }
    }

    public static void paste(Player p, String[] args) {
        if (args.length != 2 && args.length != 3)
            p.sendMessage(ChatColor.RED + "Use like: /Built Paste [name] (N/W/S/E)");
        else {
            Building building = Building.get(args[1]);
            if (building == null)
                p.sendMessage(ChatColor.RED + "The building " + args[1] + " does not exist! You may want to look at /built list");
            else {
                Vector direc = p.getLocation().getDirection().multiply(2);
                direc.setY(0);
                BlockFace bf = BlockFace.NORTH;
                if (args.length == 3) {
                    String s = args[2].substring(0);
                    if (s.equalsIgnoreCase("e")) bf = BlockFace.EAST;
                    else if (s.equalsIgnoreCase("s")) bf = BlockFace.SOUTH;
                    else if (s.equalsIgnoreCase("w")) bf = BlockFace.WEST;
                }
                building.paste(p.getLocation().add(direc), bf);
                p.sendMessage(ChatColor.GREEN + building.getName() + " pasted!");
            }
        }
    }

    public static void list(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== " + Building.list().size() + " Buildings ===");
        for (Building b : Building.list()) sender.sendMessage(ChatColor.GOLD + " - " + b.getName());
    }

    public static void displayHelp(CommandSender s) {
        ChatColor c1 = ChatColor.WHITE;
        ChatColor c2 = ChatColor.AQUA;
        ArrayList<String> messages = new ArrayList<>();
        messages.add("Built Wand-Gives a Built Wand");
        messages.add("Built Save [name]-Saves a Building");
        messages.add("Built Trigger [name]-Gives a trigger block");
        messages.add("Built Paste [name]-Pastes a Building");
        messages.add("Built Break-Allows you to break safezones");
        messages.add("Built List-Displays a list of pasted buildings");
        messages.add("Built Reload-Reloads buildings");
        s.sendMessage(c1 + "==== " + c2 + Built.plugin.getName() + c1 + " ====");
        for (String message : messages) s.sendMessage(c2 + "/" + message.replace("-", c1 + " - "));
    }
}
