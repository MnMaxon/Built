package me.MnMaxon.Built;

import me.MnMaxon.Block.BlockManager;
import me.MnMaxon.Block.CitizenBlock;
import me.MnMaxon.Block.InfoBlock;
import me.MnMaxon.Block.PaintingBlock;
import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

/**
 * Created by MnMaxon on 8/3/2016.  Aren't I great?
 */
public class Building {
    private static ArrayList<Building> buildings = new ArrayList<>();
    final private HashMap<BlockFace, HashMap<InfoBlock, Vector>> blockMap = new HashMap<>();
    private final String name;
    private InfoBlock dragonEgg = null;
    private final Vector dimensions;


    static void register(File f) {
        String name = f.getName().replace(".yml", "");
        Building old = Building.get(name);
        if (old != null) old.unregister();

        SuperYaml cfg = new SuperYaml(f.getPath());
        HashMap<InfoBlock, Vector> blockMap = new HashMap<>();
        Vector dimensions = cfg.getVector("Dimensions");

        int y = 0;
        while (cfg.getString(y + "") != null) {
            int x = 0;
            int z = 0;
            for (String s : cfg.getString(y + "").split(">", -1)) {
                try {
                    String[] raw = s.split(":", -1);
                    Byte data = 0;
                    if (raw.length > 1) data = Byte.parseByte(raw[1]);
                    String[] deepRaw = raw[0].split(",", -1);
                    Material mat = Material.matchMaterial(deepRaw[0]);
                    blockMap.put(BlockManager.getInfoBlock(mat, data, cfg, raw[0]), new Vector(x, y, z));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (z == dimensions.getZ() - 1) {
                    x++;
                    z = 0;
                } else z++;
            }
            y++;
        }
        buildings.add(new Building(name, blockMap, dimensions));
    }

    public static Building get(String name) {
        for (Building b : buildings) if (name.equalsIgnoreCase(b.getName())) return b;
        return null;
    }


    private Building(String name, HashMap<InfoBlock, Vector> blockMap, Vector dimensions) {
        this.name = name;
        this.dimensions = dimensions;
        this.blockMap.put(BlockFace.NORTH, blockMap);
        for (BlockFace bf : Arrays.asList(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST))
            this.blockMap.put(bf, new HashMap<InfoBlock, Vector>());
        for (Map.Entry<InfoBlock, Vector> entry : blockMap.entrySet()) {
            for (BlockFace bf : Arrays.asList(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST))
                this.blockMap.get(bf).put(entry.getKey(), Built.rotate(entry.getValue(), bf));

            if (entry.getKey().getMaterial() == Material.DRAGON_EGG) dragonEgg = entry.getKey();
        }
    }


    public void paste(Location loc, BlockFace bf) {
        ArrayList<Map.Entry<InfoBlock, Vector>> addAfter = new ArrayList<>();
        for (Map.Entry<InfoBlock, Vector> entry : blockMap.get(bf).entrySet()) {
            if (entry.getKey().getMaterial() == Material.TORCH || entry.getKey() instanceof PaintingBlock || entry.getKey() instanceof CitizenBlock)
                addAfter.add(entry);
            else entry.getKey().set(loc.clone().add(entry.getValue()).getBlock(), bf);
        }

        for (Map.Entry<InfoBlock, Vector> entry : addAfter)
            entry.getKey().set(loc.clone().add(entry.getValue()).getBlock(), bf);
    }

    public BlockFace matches(Block b) {
        if (dragonEgg == null) return null;
        for (BlockFace bf : Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST))
            if (matches(b, bf)) return bf;
        return null;
    }

    private boolean matches(Block b, BlockFace bf) {
        Vector startingVec = getEggLoc(bf);
        if (startingVec == null) return false;
        for (Map.Entry<InfoBlock, Vector> entry : blockMap.get(bf).entrySet())
            if (!entry.getKey().equals(b.getLocation().add(entry.getValue()).subtract(startingVec).getBlock(), bf))
                return false;
        return true;
    }

    public Vector getEggLoc(BlockFace bf) {
        if (dragonEgg == null) return null;
        return blockMap.get(bf).get(dragonEgg);
    }

    public Location getPasteLoc(Location startingLoc, BlockFace bf) {return startingLoc.subtract(getEggLoc(bf));}

    static void clear() {
        buildings.clear();
    }

    public void unregister() {
        buildings.remove(this);
    }

    public String getName() {
        return name;
    }

    public static List<Building> list() {
        return new ArrayList<>(buildings);
    }

    public boolean equalsDimensions(Building b) {
        return b.getDimensions().equals(getDimensions());
    }

    public Vector getDimensions() {
        return dimensions;
    }
}
