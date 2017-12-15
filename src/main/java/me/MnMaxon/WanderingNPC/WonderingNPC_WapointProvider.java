package me.MnMaxon.WanderingNPC;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Goal;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.trait.waypoint.WaypointEditor;
import net.citizensnpcs.trait.waypoint.WaypointProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class WonderingNPC_WapointProvider implements WaypointProvider {
    private Goal currentGoal;
    private NPC npc;
    private volatile boolean paused;

    public WaypointEditor createEditor(CommandSender sender, CommandContext args) {
        return new WaypointEditor() {
            public void begin() {}

            public void end() {}
        };
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void load(DataKey key) {}

    public void onRemove() {
        this.npc.getDefaultGoalController().removeGoal(this.currentGoal);
    }

    public void onSpawn(NPC npc) {
        this.npc = npc;
        Bukkit.getLogger().severe("Spawning: (" + npc.getId() + ") " + npc.getName());
        if (!npc.hasTrait(WanderingNPC_Trait.class)) {
            //Npc has not been assigned a location
            Bukkit.getLogger().log(java.util.logging.Level.INFO, "NPC [" + npc.getId() + "/" + npc.getName() + "] auto adding the WanderingNPC trait as the waypoint provider was added");
            npc.addTrait(WanderingNPC_Trait.class);
        }

        if (this.currentGoal == null) {
            Bukkit.getLogger().severe("Creating Goal");
            this.currentGoal = WanderingNPC_Goal.createWithNPC(npc);
            CitizensAPI.registerEvents(this.currentGoal);
        } else Bukkit.getLogger().severe("Had Goal");
        npc.getDefaultGoalController().addGoal(this.currentGoal, 1);
    }

    public void save(DataKey key) {}

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}