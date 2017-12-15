package me.MnMaxon.Block;

import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Directional;

/**
 * Created by MnMaxon on 8/3/2016.  Aren't I great?
 */
public class InfoBlock {
    private final byte data;
    private Material material;

    InfoBlock(Block b) {
        material = b.getType();
        data = b.getData();
    }

    InfoBlock(Material mat, byte data) {
        this.material = mat;
        this.data = data;
    }

    public boolean saveBonus(String path, SuperYaml yml) {return false;}

    public boolean equals(Block b, BlockFace bf) {
        if (getMaterial() == Material.DRAGON_EGG) return true;
        if (b.getType() != material) return false;
        byte data = this.data;
        if (b.getState().getData() instanceof Directional) data = BlockManager.rotateData(b.getType(), data, bf);
        return b.getData() == data;
    }

    public void set(final Block b, BlockFace bf) {
        b.setType(material, false);
        byte data = getData();
        if (b.getState().getData() instanceof Directional) data = BlockManager.rotateData(material, data, bf);
        b.setData(data, false);
    }

    public Material getMaterial() {
        return material;
    }

    public byte getData() {
        return data;
    }
}
