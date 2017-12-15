package me.MnMaxon.Utils;

import me.MnMaxon.Built.Built;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by MnMaxon on 8/6/2016.  Aren't I great?
 */
public class ItemUtils {
    private static ItemStack trigger;

    public static String toString(ItemStack is) {
        if (is == null) return "";
        try {
            String s = is.getType().name() + ";";
            s += is.getAmount() + ";";
            s += is.getDurability() + ";";
            ItemMeta im = is.getItemMeta();
            if (im != null && im.hasDisplayName()) s += im.getDisplayName();
            s += ";";
            if (im != null && im.hasLore()) {
                boolean first = true;
                for (String loreMessage : im.getLore()) {
                    if (!first) s += "&&";
                    s += loreMessage;
                    first = false;
                }
            }
            s += ";";
            if (im != null && im.hasEnchants()) {
                boolean first = true;
                for (Map.Entry<Enchantment, Integer> entry : im.getEnchants().entrySet()) {
                    if (!first) s += "&&";
                    s += entry.getKey() + "," + entry.getValue();
                    first = false;
                }
            }
            return s;
        } catch (Exception ignored) {
            return "";
        }
    }

    public static ItemStack fromString(String s) {
        try {
            if (s == null || s.equals("")) return null;
            String[] raw = s.split(";", -1);
            if (raw.length != 6) return null;
            ItemStack is = new ItemStack(Material.matchMaterial(raw[0]), Integer.parseInt(raw[1]), Short.parseShort(raw[2]));
            ItemMeta im = is.getItemMeta();
            if (im == null) return is;
            im.setDisplayName(raw[3]);
            im.setLore(Arrays.asList(raw[4].split("&&")));
            for (String enchString : raw[5].split("&&", -1)) {
                String[] enchSplit = enchString.split(",", -1);
                if (enchSplit.length < 2) continue;
                is.addEnchantment(Enchantment.getByName(enchSplit[0]), Integer.parseInt(enchSplit[1]));
            }
            is.setItemMeta(im);
            return is;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static ItemStack easy(String name, Material material, int durability, List<String> lore, int amount) {
        ItemStack is = new ItemStack(material);
        if (durability > 0) is.setDurability((short) durability);
        if (amount > 1) is.setAmount(amount);
        if (is.getItemMeta() != null) {
            ItemMeta im = is.getItemMeta();
            if (name != null) im.setDisplayName(name);
            if (lore != null) im.setLore(lore);
            is.setItemMeta(im);
        }
        return is;
    }

    public static boolean isTrigger(ItemStack is) {
        ItemStack trigger = getTrigger();
        if (is == null || is.getType() != trigger.getType()) return false;
        if (!is.hasItemMeta()) return !trigger.hasItemMeta();
        String isName = is.getItemMeta().getDisplayName();
        String triggerName = trigger.getItemMeta().getDisplayName();
        if (isName == null) return triggerName == null;
        else if (triggerName == null) return false;
        return triggerName.equals(isName);
    }

    public static void updateTrigger() {
        String name = Built.mainConfig.getString("Trigger Block.Name");
        if (name != null) name = ChatColor.translateAlternateColorCodes('&', name);
        String typeString = Built.mainConfig.getString("Trigger Block.Type");
        Material mat = null;
        if (typeString != null) mat = Material.matchMaterial(typeString.replace(" ", "_").toUpperCase());
        if (mat == null) mat = Material.DRAGON_EGG;
        List<String> lore = new ArrayList<>();
        try {
            for (String s : Built.mainConfig.getStringList("Trigger Block.Type"))
                lore.add(ChatColor.translateAlternateColorCodes('&', s));
        } catch (Exception ignored) {
        }
        trigger = easy(name, mat, 0, lore, 1);
    }

    public static ItemStack getWand() {
        return easy(ChatColor.AQUA + "Built Wand", Material.BLAZE_ROD, 0, Arrays.asList(ChatColor.DARK_GRAY + "Left/Right Click to Select Points",
                ChatColor.DARK_GRAY + "For commands, type /Build"), 1);
    }

    public static ItemStack getTrigger() {
        return trigger;
    }
}
