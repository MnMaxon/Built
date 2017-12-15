package me.MnMaxon.Built;

import me.MnMaxon.Block.BlockManager;
import me.MnMaxon.Block.InfoBlock;
import me.MnMaxon.Block.PaintingBlock;
import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by MnMaxon on 8/3/2016.  Aren't I great?
 */
public class Selection {
    private static HashMap<Player, Selection> selectionMap = new HashMap<>();
    private Location locOne = null;
    private Location locTwo = null;

    public void setLocOne(Location locOne) {this.locOne = locOne;}

    public void setLocTwo(Location locTwo) {
        this.locTwo = locTwo;
    }

    public static void clear(Player p) {selectionMap.remove(p);}

    public static Selection get(Player p) {
        if (!selectionMap.containsKey(p)) selectionMap.put(p, new Selection());
        return selectionMap.get(p);
    }

    public void save(String name) throws Throwable {
        if (locOne == null || locTwo == null)
            throw new Throwable(ChatColor.RED + "Both points are not set! Use /Built Wand");
        else if (!locOne.getWorld().equals(locTwo.getWorld()))
            throw new Throwable(ChatColor.RED + "Both points are not in the same world!");
        SuperYaml cfg = new SuperYaml(Built.dataFolder + "/Buildings/" + name + ".yml");
        for (String key : cfg.getConfigurationSection("").getKeys(false)) cfg.set(key, null);

        int minX = locOne.getBlockX();
        int maxX = locTwo.getBlockX();
        if (minX > maxX) {
            maxX = minX;
            minX = locTwo.getBlockX();
        }
        int minY = locOne.getBlockY();
        int maxY = locTwo.getBlockY();
        if (minY > maxY) {
            maxY = minY;
            minY = locTwo.getBlockY();
        }
        int minZ = locOne.getBlockZ();
        int maxZ = locTwo.getBlockZ();
        if (minZ > maxZ) {
            maxZ = minZ;
            minZ = locTwo.getBlockZ();
        }

        cfg.set("Dimensions.X", maxX - minX + 1);
        cfg.set("Dimensions.Y", maxY - minY + 1);
        cfg.set("Dimensions.Z", maxZ - minZ + 1);

        HashMap<Material, Integer> bonusMap = new HashMap<>();
        for (int yLine = 0; minY + yLine <= maxY; yLine++) {
            int curY = minY + yLine;
            String line = "";
            for (int curX = minX; curX <= maxX; curX++)
                for (int curZ = minZ; curZ <= maxZ; curZ++) {
                    InfoBlock ib = BlockManager.getInfoBlock(locOne.getWorld().getBlockAt(curX, curY, curZ));
                    String type = ib.getMaterial().name();
                    if (ib instanceof PaintingBlock) type = Material.PAINTING.name();
                    if (bonusMap.containsKey(ib.getMaterial()))
                        bonusMap.put(ib.getMaterial(), bonusMap.get(ib.getMaterial()) + 1);
                    else bonusMap.put(ib.getMaterial(), 1);

                    int amount = bonusMap.get(ib.getMaterial());
                    String path = type + "," + amount;
                    if (ib.saveBonus(path, cfg)) type = type + "," + bonusMap.get(ib.getMaterial());
                    else bonusMap.put(ib.getMaterial(), amount - 1);

                    if (ib.getData() != 0) type += ":" + ib.getData();
                    line += ">" + type;
                }
            cfg.set(yLine + "", line.replaceFirst(">", ""));
        }

        cfg.save();
        Building.register(cfg.getFile());
    }
}