package me.MnMaxon.Built;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by MnMaxon on 8/8/2016.  Aren't I great?
 */
public class SavedArea {
    private static HashMap<World, ArrayList<SavedArea>> savedAreas = new HashMap<>();
    private final UUID uuid;
    private final String name;
    private Location min;
    private Location max;
    private BlockFace bf;

    private SavedArea(UUID uuid, String name, Location min, Location max, BlockFace bf) {
        this.uuid = uuid;
        this.name = name;
        this.min = min;
        this.max = max;
        this.bf = bf;
    }

    public static ArrayList<SavedArea> list(World world) {
        if (!savedAreas.containsKey(world)) return new ArrayList<>();
        return savedAreas.get(world);
    }

    public static void clear() {savedAreas.clear();}

    public static void register(String uuid, String name, String world, Vector locVectior, Vector dimensions, String direction) {
        World w = Bukkit.getWorld(UUID.fromString(world));
        if (w == null) return;
        ArrayList<SavedArea> list = new ArrayList<>();
        if (savedAreas.containsKey(w)) list = savedAreas.get(w);
        int x = locVectior.getBlockX();
        int y = locVectior.getBlockY();
        int z = locVectior.getBlockZ();
        int dimX = dimensions.getBlockX();
        int dimY = dimensions.getBlockY();
        int dimZ = dimensions.getBlockZ();
        list.add(new SavedArea(UUID.fromString(uuid), name, new Location(w, x, y, z), new Location(w, x + dimX, y + dimY, z + dimZ), BlockFace.valueOf(direction)));
        savedAreas.put(w, list);
    }

    public Location getMin() {return min;}

    public Location getMax() {return max;}

    public String getName() {return name;}

    public OfflinePlayer getPlayer() {return Bukkit.getOfflinePlayer(uuid);}

    public boolean isIn(Location loc) {
        return min.getBlockX() <= loc.getBlockX() && loc.getBlockX() < max.getBlockX()
                && min.getBlockZ() <= loc.getBlockZ() && loc.getBlockZ() < max.getBlockZ()
                && min.getBlockY() <= loc.getBlockY() && loc.getBlockY() < max.getBlockY();
    }

    public static SavedArea getIn(Location loc) {
        for (SavedArea sa : list(loc.getWorld())) if (sa.isIn(loc)) return sa;
        return null;
    }

    public static void create(Location loc, final BlockFace bf, final Building b, final Player p) {
        loc = b.getPasteLoc(loc, bf);
        final Vector modifier = Built.rotate(b.getDimensions(), bf);
        if (modifier.getBlockX() < 0) loc.add(modifier.getBlockX() + 1, 0, 0);
        if (modifier.getBlockZ() < 0) loc.add(0, 0, modifier.getBlockZ() + 1);
        modifier.setX(Math.abs(modifier.getBlockX()));
        modifier.setY(Math.abs(modifier.getBlockY()));
        modifier.setZ(Math.abs(modifier.getBlockZ()));

        register(p.getUniqueId().toString(), b.getName(), loc.getWorld().getUID().toString(), loc.toVector(), modifier, bf.name());

        final Location finalLoc = loc;
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Built.plugin, new Runnable() {
            @Override
            public void run() {
                Built.db.executePreparedStatement("INSERT INTO " + Built.TABLE + " (UUID, Name, World, X, Y, Z, DimX, DimY, DimZ, Direction)" +
                        " VALUES ('" + p.getUniqueId() + "', '" + b.getName() + "', '" + finalLoc.getWorld().getUID() + "', '" + finalLoc.getBlockX()
                        + "', '" + finalLoc.getBlockY() + "', '" + finalLoc.getBlockZ() + "', '" + modifier.getBlockX() + "', '" + modifier.getBlockY() + "', '" + modifier.getBlockZ() + "', '" + bf.name() + "');");
            }
        });
    }

    public void delete() {
        list(min.getWorld()).remove(this);
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Built.plugin, new Runnable() {
            @Override
            public void run() {
                Built.db.executePreparedStatement("DELETE FROM " + Built.TABLE + " WHERE X='" + min.getBlockX() + "' AND Z='" + min.getBlockZ() + "';");
            }
        });
    }
}
