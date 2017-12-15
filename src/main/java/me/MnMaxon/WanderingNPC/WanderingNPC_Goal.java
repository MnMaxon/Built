package me.MnMaxon.WanderingNPC;

import me.MnMaxon.Built.SavedArea;
import net.citizensnpcs.api.ai.TeleportStuckAction;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.astar.pathfinder.MinecraftBlockExaminer;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class WanderingNPC_Goal extends BehaviorGoalAdapter {
    private boolean forceFinish;
    private final NPC npc;
    private final Random random = new Random();


    private WanderingNPC_Goal(NPC npc) {
        this.npc = npc;
    }

    private Location findRandomPosition() {
        Location base = this.npc.getEntity().getLocation();
        Location found = null;

        WanderingNPC_Trait trait;

        if (!npc.hasTrait(WanderingNPC_Trait.class)) {
            //Npc has not been assigned a location
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "NPC [" + npc.getId() + "/" + npc.getName() + "] has not been setup to use the WanderingNPC path provider");
            npc.despawn(DespawnReason.PLUGIN);
            return base;
        } else trait = npc.getTrait(WanderingNPC_Trait.class);

        int x = base.getBlockX() + this.random.nextInt(2 * trait.PathFindingDistance) - trait.PathFindingDistance;
        int y = base.getBlockY() + this.random.nextInt(2 * trait.ClimbingDistance) - trait.ClimbingDistance;
        int z = base.getBlockZ() + this.random.nextInt(2 * trait.PathFindingDistance) - trait.PathFindingDistance;
        Block block = base.getWorld().getBlockAt(x, y - 1, z);


//        if (trait.AllowedBlockTypes.size() > 0 && !trait.AllowedBlockTypes.contains(block.getType().toString()))  bAllow = false;
        if (MinecraftBlockExaminer.canStandOn(block) && SavedArea.getIn(block.getLocation().add(0.0D, 1.0D, 0.0D)) != null
                && !(base.getWorld().getBlockAt(x, y, z)).isLiquid()) {
            found = block.getLocation().add(0.0D, 1.0D, 0.0D);
            trait.lastValidLocation = found;
        }
        if (found == null) trait.Pathfailures++;
        else trait.Pathfailures = 0;

        return found;
    }

    @EventHandler
    public void onFinish(NavigationCompleteEvent event) {
        if (event.getNPC() == this.npc) {
            this.forceFinish = true;
            WanderingNPC_Trait trait = event.getNPC().getTrait(WanderingNPC_Trait.class);
            //[11-07-15] Added random wait for the npc
            if (trait.minWait > 0 || trait.maxWait > 0) {
                //NPC needs to wait a random amount of time before next move.
                Calendar oTmpCal = Calendar.getInstance();
                oTmpCal.setTime(new Date());
                oTmpCal.add(Calendar.SECOND, (int) ((Math.random() * ((trait.maxWait - trait.minWait) + 1) + trait.minWait)));
                trait.lastFinish = oTmpCal.getTime();
            } else trait.lastFinish = new Date(100);
        }
    }

    public void reset() {
        this.forceFinish = false;
    }

    public BehaviorStatus run() {
        if (!this.npc.getNavigator().isNavigating() || this.forceFinish) return BehaviorStatus.SUCCESS;
        else return BehaviorStatus.RUNNING;
    }

    public boolean shouldExecute() {
        WanderingNPC_Trait trait;
        if (!npc.hasTrait(WanderingNPC_Trait.class)) {
            //Npc has not been assigned a location
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "NPC [" + npc.getId() + "/" + npc.getName() + "] has not been setup to use the WanderingNPC path provider");
            npc.despawn(DespawnReason.PLUGIN);
            return false;
        } else if (!this.npc.isSpawned()) return false;
        trait = npc.getTrait(WanderingNPC_Trait.class);
        for (Player plrEntity : Bukkit.getOnlinePlayers())
            if (plrEntity.getWorld() == npc.getEntity().getWorld() && plrEntity.getLocation().distance(npc.getEntity().getLocation()) < trait.PausePlayerClose)
                return false;
        if (this.npc.getNavigator().getTargetAsLocation() != null && this.npc.getNavigator().isNavigating()
                & this.npc.getNavigator().getTargetAsLocation().distance(this.npc.getEntity().getLocation()) > 3)
            return false;

        if (trait.lastFinish != null) {
            if (trait.lastFinish.after(new Date())) return false;
        } else trait.lastFinish = new Date(100);

        Location dest = findRandomPosition();
        if (dest == null) return false;
        this.npc.data().set("swim", true);
        this.npc.getNavigator().getLocalParameters().useNewPathfinder(false);
        this.npc.getNavigator().getLocalParameters().distanceMargin(2);
        this.npc.getNavigator().getLocalParameters().pathDistanceMargin(2);
        this.npc.getNavigator().getLocalParameters().avoidWater(true);
        this.npc.getNavigator().getLocalParameters().stuckAction(TeleportStuckAction.INSTANCE);
        this.npc.getNavigator().setTarget(dest);
        return true;
    }

    public static WanderingNPC_Goal createWithNPC(NPC npc) {
        return new WanderingNPC_Goal(npc);
    }
}
