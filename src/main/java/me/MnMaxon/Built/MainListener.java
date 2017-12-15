package me.MnMaxon.Built;

import me.MnMaxon.Utils.ItemUtils;
import me.MnMaxon.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;

import java.util.ArrayList;

public class MainListener implements Listener {
    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Selection.clear(e.getPlayer());
        Built.toDestroy.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (Built.toDestroy.contains(e.getPlayer().getUniqueId())) {
            if (e.getAction() == Action.LEFT_CLICK_AIR)
                e.getPlayer().sendMessage(ChatColor.AQUA + "Destroy mode cancelled");
            else {
                SavedArea sa = SavedArea.getIn(e.getClickedBlock().getLocation());
                if (sa == null)
                    e.getPlayer().sendMessage(ChatColor.RED + "Safezone not detected. Destroy mode cancelled.");
                else {
                    sa.delete();
                    e.getPlayer().sendMessage(ChatColor.GREEN + "Safezone destroyed!");
                }

            }
            Built.toDestroy.remove(e.getPlayer().getUniqueId());
        }
        if (e.getPlayer().isOp() && e.getItem() != null && e.getItem().getType() == Material.BLAZE_ROD && e.getItem().hasItemMeta()
                && e.getItem().getItemMeta().hasDisplayName() && e.getItem().getItemMeta().getDisplayName().equals(ChatColor.AQUA + "Built Wand")) {
            if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                Selection.get(e.getPlayer()).setLocOne(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatColor.GREEN + "Point 1 Set!");
            } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Selection.get(e.getPlayer()).setLocTwo(e.getClickedBlock().getLocation());
                e.getPlayer().sendMessage(ChatColor.GREEN + "Point 2 Set!");
            }
            e.setCancelled(true);
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK && e.getPlayer().isSneaking() && e.getClickedBlock().getState().getData() instanceof Directional) {
            e.setCancelled(true);
            Bukkit.broadcastMessage(((Directional) e.getClickedBlock().getState().getData()).getFacing() + ": " + e.getClickedBlock().getData());
//            e.getClickedBlock().setData(InfoBlock.rotateData(e.getClickedBlock().getType(), e.getClickedBlock().getData(), BlockFace.EAST));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (SavedArea.getIn(e.getBlock().getLocation()) != null) {
            e.getPlayer().sendMessage(ChatColor.RED + "You can not build in " + ChatColor.WHITE + "Built" + ChatColor.RED + " buildings!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockExplodeEvent e) {
        for (Block b : new ArrayList<>(e.blockList()))
            if (SavedArea.getIn(b.getLocation()) != null) e.blockList().remove(b);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (SavedArea.getIn(e.getEntity().getLocation()) != null && !(e.getEntity() instanceof LivingEntity))
            e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        if (SavedArea.getIn(e.getBlock().getLocation()) != null) {
            e.getPlayer().sendMessage(ChatColor.RED + "You can not build in " + ChatColor.WHITE + "Built" + ChatColor.RED + " buildings!");
            e.setCancelled(true);
            return;
        }
        if (!ItemUtils.isTrigger(e.getItemInHand())) return;
        for (Building b : Building.list()) {
            BlockFace matchBf = b.matches(e.getBlockPlaced());
            if (matchBf != null) {

                Utils.launchFireworks(e.getBlockPlaced().getLocation().add(0, 5, 0), 5);
                e.getPlayer().sendMessage(ChatColor.GREEN + "You have successfully built " + ChatColor.AQUA + b.getName().replace("_", " ") + ChatColor.GREEN + "!");
                e.getPlayer().giveExp(Built.mainConfig.getInt("Success Exp"));
                for (ItemStack is : Built.successItems) e.getPlayer().getInventory().addItem(is);

                e.getBlockPlaced().setType(Material.AIR);
                SavedArea.create(e.getBlock().getLocation(), matchBf, b, e.getPlayer());

                Building alt = Building.get(b.getName() + "-alt");
                if (alt != null)
                    if (alt.equalsDimensions(b)) alt.paste(b.getPasteLoc(e.getBlock().getLocation(), matchBf), matchBf);
                    else if (e.getPlayer().isOp()) {
                        e.getPlayer().sendMessage(ChatColor.RED + "The alt version could not be pasted because it is a different size than the original!");
                        e.getPlayer().sendMessage(ChatColor.RED + "Original: " + ChatColor.GOLD + b.getDimensions().toString());
                        e.getPlayer().sendMessage(ChatColor.RED + "Alternate: " + ChatColor.GOLD + alt.getDimensions());
                    }
                //TODO Check for perms
                return;
            }
        }
        e.setCancelled(true);
        e.getPlayer().sendMessage(ChatColor.RED + "That is an invalid building!");
    }
}
