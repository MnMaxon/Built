package me.MnMaxon.Utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by MnMaxon on 8/9/2016.  Aren't I great?
 */
public class Utils {
    public static void launchFireworks(Location loc, int amount) {
        Random r = new Random();
        List<Color> colorList = Arrays.asList(Color.WHITE, Color.RED, Color.GREEN, Color.OLIVE, Color.MAROON,
                Color.YELLOW, Color.TEAL, Color.GREEN, Color.FUCHSIA, Color.AQUA, Color.TEAL, Color.BLACK,
                Color.GRAY, Color.LIME, Color.NAVY, Color.ORANGE, Color.BLUE, Color.SILVER, Color.PURPLE);
        for (int i = 0; i < amount; i++) {
            Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(r.nextInt(2) + 2);
            FireworkEffect.Builder builder = FireworkEffect.builder();
            builder.trail(true);
            builder.flicker(r.nextBoolean());
            builder.with(FireworkEffect.Type.values()[r.nextInt(FireworkEffect.Type.values().length)]);
            builder.withColor(colorList.get(r.nextInt(colorList.size())), colorList.get(r.nextInt(colorList.size())));
            builder.withFade(colorList.get(r.nextInt(colorList.size())), colorList.get(r.nextInt(colorList.size())));
            meta.addEffect(builder.build());
            fw.setFireworkMeta(meta);
        }
    }
}
