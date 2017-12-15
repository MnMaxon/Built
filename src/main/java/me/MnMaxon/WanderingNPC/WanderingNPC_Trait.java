package me.MnMaxon.WanderingNPC;

import me.MnMaxon.Built.Built;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Location;

import java.util.Date;

public class WanderingNPC_Trait extends Trait {
    Built plugin = null;

    @Persist
    int PathFindingDistance = 10;
    @Persist
    int ClimbingDistance = 1;
    @Persist
    int PausePlayerClose = 0;
    @Persist
    int minWait = 0;
    @Persist
    int maxWait = 0;

    Location lastValidLocation = null;

    public int Pathfailures = 0;
    public Date lastFinish;

    public WanderingNPC_Trait() {
        super("wanderingnpc");
        plugin = Built.plugin;
    }

    @Override
    public void onSpawn() {
    }
}
