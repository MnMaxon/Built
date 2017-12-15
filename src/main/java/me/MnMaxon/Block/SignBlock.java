package me.MnMaxon.Block;

import me.MnMaxon.Built.Built;
import me.MnMaxon.Utils.SuperYaml;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import java.util.Arrays;
import java.util.List;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class SignBlock extends InfoBlock {
    private final List<String> lines;

    public SignBlock(Sign state) {
        super(state.getBlock());
        lines = Arrays.asList(state.getLines());
    }

    public SignBlock(Material mat, byte data, String bonus) {
        this(mat, data, Arrays.asList(bonus.split("&&")));
    }

    public SignBlock(Material mat, byte data, List<String> lines) {
        super(mat, data);
        this.lines = lines;
    }

    @Override
    public  boolean saveBonus(String path, SuperYaml yml) {
        String bonusInfo = "";
        for (String s : lines) bonusInfo += "&&" + s;
        yml.set(path,bonusInfo.replaceFirst("&&", ""));
        return true;
    }

    @Override
    public void set(final Block b, BlockFace bf) {
        super.set(b, bf);
        Bukkit.getScheduler().scheduleSyncDelayedTask(Built.plugin, new Runnable() {
            @Override
            public void run() {
                if (b.getState() instanceof Sign) {
                    Sign sign = (Sign) b.getState();
                    sign.update();
                    for (int i = 0; i < lines.size(); i++) {
                        sign.setLine(i, lines.get(i));
                        sign.getLines()[i] = lines.get(i);
                        sign.update();
                    }
                    sign.update();
                }
            }
        }, 2L);
    }
}
