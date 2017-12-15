package me.MnMaxon.Built;

import me.MnMaxon.Utils.ItemUtils;
import me.MnMaxon.Utils.MySQL;
import me.MnMaxon.Utils.SuperYaml;
import me.MnMaxon.WanderingNPC.WanderingNPC_Trait;
import me.MnMaxon.WanderingNPC.WonderingNPC_WapointProvider;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.trait.waypoint.Waypoints;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public final class Built extends JavaPlugin {
    public static String dataFolder;
    public static Built plugin;
    public static SuperYaml mainConfig;
    public static ArrayList<ItemStack> successItems = new ArrayList<>();
    public static MySQL db;
    public static final String TABLE = "Built";
    public static HashSet<UUID> toDestroy = new HashSet<>();
    public static boolean usingNPCs = false;

    public static void reloadConfigs() {
        //Config.yml
        plugin.saveDefaultConfig();
        mainConfig = new SuperYaml(dataFolder + "/config.yml");
        if (mainConfig.getConfigurationSection("") == null || mainConfig.getConfigurationSection("").getKeys(false).isEmpty()) {
            mainConfig.getFile().delete();
            plugin.saveDefaultConfig();
            mainConfig = new SuperYaml(dataFolder + "/config.yml");
        }
        ItemUtils.updateTrigger();

        SavedArea.clear();
        try {
            db = new MySQL(mainConfig.getString("MySQL.IP"), mainConfig.getString("MySQL.Database"),
                    mainConfig.getString("MySQL.Username"), mainConfig.getString("MySQL.Password"));
            db.executePreparedStatement("CREATE TABLE IF NOT EXISTS " + TABLE
                    + " (UUID varchar(64), Name VARCHAR(64), World VARCHAR(64), X VARCHAR(6), Y VARCHAR(6), Z VARCHAR(6), DimX VARCHAR(3), DimY VARCHAR(3), DimZ VARCHAR(3), Direction VARCHAR(5))");
            MySQL.ResultPack rp = db.executePreparedQuery("SELECT * FROM " + TABLE + ";");
            ResultSet rs = rp.getResultSet();
            try {
                while (rs.next())
                    SavedArea.register(rs.getString("UUID"), rs.getString("Name"), rs.getString("World"),
                            new Vector(rs.getInt("X"), rs.getInt("Y"), rs.getInt("Z")),
                            new Vector(rs.getInt("DimX"), rs.getInt("DimY"), rs.getInt("DimZ")), rs.getString("Direction"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        successItems.clear();
        for (String itemString : mainConfig.getStringList("Success Items"))
            try {
                String[] raw = itemString.split(" ");
                Material mat = Material.getMaterial(Integer.parseInt(raw[0]));
                int amount = 1;
                if (raw.length > 1) try {
                    amount = Integer.parseInt(raw[1]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (amount < 1) amount = 1;
                successItems.add(ItemUtils.easy(null, mat, 0, null, amount));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        //Schematics in /Buildings
        Building.clear();
        File folder = new File(dataFolder + "/Buildings");
        folder.mkdirs();
        for (File f : folder.listFiles()) if (f.getName().endsWith(".yml")) Building.register(f);
    }

    @Override
    public void onEnable() {
        plugin = this;
        dataFolder = this.getDataFolder().getAbsolutePath();
        if (Bukkit.getPluginManager().getPlugin("Citizens") == null) {
            Bukkit.getLogger().severe("NPCs cannot be used because Citizens is not installed!");
        } else {
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(WanderingNPC_Trait.class).withName("wanderingnpc"));
            Waypoints.registerWaypointProvider(WonderingNPC_WapointProvider.class, "wanderingnpc");
            usingNPCs = true;
        }
        reloadConfigs();
        getServer().getPluginManager().registerEvents(new MainListener(), this);
    }

    @SuppressWarnings("Contract")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("Built.Admin"))
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
        else if (args.length == 0 || args[0].equalsIgnoreCase("help")) Cmds.displayHelp(sender);
        else if (args[0].equalsIgnoreCase("trigger")) Cmds.trigger(sender, args);
        else if (args[0].equalsIgnoreCase("list")) Cmds.list(sender);
        else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) Cmds.reload(sender);
        else if (!(sender instanceof Player))
            sender.sendMessage(ChatColor.RED + "You need to be a player to do this!");
        else {
            Player p = (Player) sender;
            if (args[0].equalsIgnoreCase("save")) Cmds.save(p, args);
            else if (args[0].equalsIgnoreCase("break")) {
                p.sendMessage(ChatColor.GREEN + "Left Click on a block inside of a Built zone to destroy the safezone! Left click the air to disable this.");
                toDestroy.add(p.getUniqueId());
            } else if (args[0].equalsIgnoreCase("paste")) Cmds.paste(p, args);
            else if (args[0].equalsIgnoreCase("wand")) p.getInventory().addItem(ItemUtils.getWand());
            else Cmds.displayHelp(sender);
        }
        return true;
    }

    public static Vector rotate(Vector vec, BlockFace bf) {
        double x = vec.getX();
        double y = vec.getY();
        double z = vec.getZ();
        if (bf == BlockFace.NORTH) return new Vector(x, y, z);
        else if (bf == BlockFace.EAST) return new Vector(-z, y, x);
        else if (bf == BlockFace.SOUTH) return new Vector(-x, y, -z);
        else if (bf == BlockFace.WEST) return new Vector(z, y, -x);
        else return vec.clone();
    }
}