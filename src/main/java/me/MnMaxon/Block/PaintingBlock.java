package me.MnMaxon.Block;

import me.MnMaxon.Built.Built;
import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.Art;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Painting;

import java.util.HashSet;
import java.util.Random;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class PaintingBlock extends InfoBlock {
    private static HashSet<Integer> toAdd = new HashSet<>();
    private final BlockFace direction;
    private final Art art;

    PaintingBlock(Painting p) {
        super(p.getLocation().getBlock());
        direction = p.getFacing();
        art = p.getArt();
    }

    PaintingBlock(String bonusInfo) {
        super(Material.AIR, (byte) 0);
        String[] raw = bonusInfo.split(";");
        art = Art.valueOf(raw[0]);
        direction = BlockFace.valueOf(raw[1]);
    }

    @Override
    public boolean saveBonus(String path, SuperYaml yml) {
        yml.set(path, art + ";" + direction);
        return true;
    }

    @Override
    public void set(Block b, BlockFace bf) {
        super.set(b, bf);
        int rotations = 0;
        if (bf == BlockFace.EAST) rotations = 1;
        else if (bf == BlockFace.SOUTH) rotations = 2;
        else if (bf == BlockFace.WEST) rotations = 3;
        BlockFace direc = direction;
        while (rotations > 0) {
            rotations--;
            if (direc == BlockFace.NORTH) direc = BlockFace.EAST;
            else if (direc == BlockFace.EAST) direc = BlockFace.SOUTH;
            else if (direc == BlockFace.SOUTH) direc = BlockFace.WEST;
            else if (direc == BlockFace.WEST) direc = BlockFace.NORTH;
        }
        BlockFace left;
        if (direc == BlockFace.NORTH) left = BlockFace.EAST;
        else if (direc == BlockFace.EAST) left = BlockFace.SOUTH;
        else if (direc == BlockFace.SOUTH) left = BlockFace.WEST;
        else left = BlockFace.NORTH;

        for (int i = art.getBlockHeight(); i > 1; i--) b = b.getRelative(BlockFace.DOWN);
        for (int i = art.getBlockWidth(); i > 1; i--) b = b.getRelative(left);

        addPainting(b.getLocation(), direc, art);
    }

    private static void addPainting(final Location loc, final BlockFace direc, final Art art) {
        final int r = new Random().nextInt();
        toAdd.add(r);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Built.plugin, new Runnable() {
            @Override
            public void run() {
                Painting p = loc.getWorld().spawn(loc, Painting.class);
                p.setFacingDirection(direc, true);
                p.setArt(art, true);
                toAdd.remove(r);
            }
        }, 5L * toAdd.size());
    }
}
