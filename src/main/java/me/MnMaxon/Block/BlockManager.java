package me.MnMaxon.Block;

import me.MnMaxon.Built.Built;
import me.MnMaxon.Utils.SuperYaml;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.List;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class BlockManager {
    public static List<Material> inventories;

    public static InfoBlock getInfoBlock(Block b) {
        BlockState state = b.getState();
        if (state != null)
            if (inventories.contains(b.getType()) && state instanceof InventoryHolder)
                return new InventoryBlock(b, (InventoryHolder) state);
            else if (state instanceof Sign) return new SignBlock((Sign) state);
        for (Entity ent : b.getWorld().getNearbyEntities(b.getLocation(), 1, 1, 1)) {
            if (ent.getLocation().getBlock().equals(b)) {
                if (ent instanceof Painting) return new PaintingBlock((Painting) ent);
                if (Built.usingNPCs) {
                    NPC npc = CitizensAPI.getNPCRegistry().getNPC(ent);
                    if (npc != null) return new CitizenBlock(b, npc);
                }
            }
        }
        return new InfoBlock(b);
    }

    public static InfoBlock getInfoBlock(Material mat, byte data, SuperYaml config, String path) {
        Object bonus = config.get(path);
        if (bonus == null) return new InfoBlock(mat, data);
        if (bonus instanceof String) {
            if (inventories.contains(mat)) return new InventoryBlock(mat, data, (String) bonus);
            else if (mat.name().contains("SIGN")) return new SignBlock(mat, data, (String) bonus);
            else if (mat == Material.PAINTING) return new PaintingBlock((String) bonus);
        }
        return new CitizenBlock(config, path);
    }

    public static byte rotateData(Material material, byte data, BlockFace bf) {
        int rotates = 0;
        if (bf == BlockFace.EAST) rotates = 1;
        else if (bf == BlockFace.SOUTH) rotates = 2;
        else if (bf == BlockFace.WEST) rotates = 3;
        if (material.equals(Material.SIGN_POST)) {
            data += 4 * rotates;
            while (data >= 16) data -= 16;
        } else while (rotates > 0) {
            rotates--;
            if (material.name().contains("STAIR")) {
                data = nextData(data, Arrays.asList(0, 2, 1, 3), Arrays.asList(4, 0));
            } else if (material.name().contains("TRAP_DOOR")) {
                data = nextData(data, Arrays.asList(0, 3, 1, 2), Arrays.asList(12, 8, 4, 0));
            } else if (material.name().contains("DOOR")) data = nextData(data, Arrays.asList(0, 3, 2, 9));
            else if (material == Material.TORCH) data = nextData(data, Arrays.asList(1, 3, 2, 4));
            else data = nextData(data, Arrays.asList(2, 5, 3, 4));
        }
        return data;
    }

    private static byte nextData(byte b, List<Integer> list) {
        int place = 0;
        for (int i : list) {
            place++;
            if (i == b) break;
        }
        if (place >= list.size()) place = 0;
        return list.get(place).byteValue();
    }

    private static byte nextData(byte b, List<Integer> list, List<Integer> modifier) {
        for (int mod : modifier) {
            if (b < mod) continue;
            return (byte) (nextData((byte) (b - mod), list) + mod);
        }
        return nextData(b, list);
    }

    static {
        inventories = Arrays.asList(Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE, Material.DISPENSER, Material.DROPPER, Material.HOPPER, Material.BREWING_STAND);
    }
}
