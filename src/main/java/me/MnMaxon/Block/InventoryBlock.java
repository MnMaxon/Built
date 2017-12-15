package me.MnMaxon.Block;

import me.MnMaxon.Utils.ItemUtils;
import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class InventoryBlock extends InfoBlock {
    private ItemStack[] items = null;

    public InventoryBlock(Block b, InventoryHolder state) {
        super(b);
        items = state.getInventory().getContents();
    }

    public InventoryBlock(Material mat, byte data, String bonus) {
        super(mat, data);
        String[] rawItems = bonus.split(">", -1);
        items = new ItemStack[rawItems.length];
        for (int i = 0; i < rawItems.length; i++) {
            ItemStack is = ItemUtils.fromString(rawItems[i]);
            items[i] = is;
        }
    }

    public ItemStack[] getInventory() {return items;}

    @Override
    public boolean saveBonus(String path, SuperYaml yml) {
        String bonusInfo = "";
        for (ItemStack is : items) bonusInfo += ">" + ItemUtils.toString(is);
        yml.set(path, bonusInfo.replaceFirst(">", ""));
        return true;
    }

    @Override
    public void set(Block b, BlockFace bf) {
        super.set(b, bf);
        if (b.getState() instanceof InventoryHolder) {
            Inventory inv = ((InventoryHolder) b.getState()).getInventory();
            if (inv.getContents() != null && getInventory() != null && inv.getContents().length == getInventory().length)
                inv.setContents(getInventory());
        }
    }
}
