package me.MnMaxon.Block;

import me.MnMaxon.Built.Built;
import me.MnMaxon.Utils.SuperYaml;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.util.Storage;
import net.citizensnpcs.api.util.YamlStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class CitizenBlock extends InfoBlock {
    private NPC savingNpc;
    private DataKey dataKey = null;

    CitizenBlock(Block b, NPC npc) {
        super(b);
        this.savingNpc = npc;
    }

    public CitizenBlock(SuperYaml config, String path) {
        super(Material.AIR, (byte) 0);
        if (!Built.usingNPCs) return;
        try {
            Storage storage = new YamlStorage(config.getFile());
            storage.load();
            dataKey = storage.getKey(path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean saveBonus(String path, SuperYaml yml) {
        if (savingNpc == null) return false;
        yml.save();
        YamlStorage storage = new YamlStorage(yml.getFile());
        storage.load();
        savingNpc.save(storage.getKey(path));
        this.dataKey = storage.getKey(path);
        storage.save();
        yml.reload();
        return true;
    }

    @Override
    public void set(Block b, BlockFace bf) {
        super.set(b, bf);
        if (!Built.usingNPCs || dataKey == null) return;
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "HLP");
        npc.load(dataKey);
        Location loc = b.getLocation().add(.5, .5, .5);
        npc.spawn(loc);
        npc.teleport(loc, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}