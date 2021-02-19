package me.sizzlemcgrizzle.saddles;

import de.craftlancer.core.gui.GUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MountEditorMenu {
    private Saddles plugin;
    private MountData data;
    private GUIInventory inventory;
    
    public MountEditorMenu(Saddles plugin, MountData data) {
        this.plugin = plugin;
        this.data = data;
    }
    
    public void display(Player player) {
        if (inventory == null)
            createInventory();
        
        player.openInventory(inventory.getInventory());
    }
    
    private void createInventory() {
        this.inventory = new GUIInventory(plugin, "§5Mount Editor", 2);
        inventory.fill(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("§8").build());
        
        setColorItem(0, Horse.Color.WHITE, Material.SMOOTH_QUARTZ);
        setColorItem(1, Horse.Color.BLACK, Material.BLACK_CONCRETE);
        setColorItem(2, Horse.Color.GRAY, Material.GRAY_CONCRETE);
        setColorItem(3, Horse.Color.BROWN, Material.BROWN_CONCRETE);
        setColorItem(4, Horse.Color.DARK_BROWN, Material.BROWN_TERRACOTTA);
        setColorItem(5, Horse.Color.CREAMY, Material.BROWN_CONCRETE_POWDER);
        setColorItem(6, Horse.Color.CHESTNUT, Material.SPRUCE_PLANKS);
        
        setArmorItem(9, Material.LEATHER_HORSE_ARMOR);
        setArmorItem(10, Material.IRON_HORSE_ARMOR);
        setArmorItem(11, Material.GOLDEN_HORSE_ARMOR);
        setArmorItem(12, Material.DIAMOND_HORSE_ARMOR);
        
        setRemoveArmorItem();
        setRemoveColorItem();
        
    }
    
    private void setColorItem(int slot, Horse.Color color, Material material) {
        inventory.setItem(slot, new ItemBuilder(material).setDisplayName("§f§l" + color.name().replace("_", " "))
                .addLore("", "§7Click to set mount color to " + color.name().replace("_", " "))
                .setEnchantmentGlow(data.getCustomColor() == color).build());
        inventory.setClickAction(slot, player -> {
            data.setCustomColor(color);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1f);
            createInventory();
            display(player);
        });
    }
    
    private void setArmorItem(int slot, Material material) {
        String name = material.name().replaceAll("_HORSE_ARMOR", "");
        inventory.setItem(slot, new ItemBuilder(material).setDisplayName("§f§l" + name)
                .addLore("", "§7Click to set mount color to " + name)
                .setEnchantmentGlow(data.getCustomArmor() != null && data.getCustomArmor().getType() == material).build());
        inventory.setClickAction(slot, player -> {
            data.setCustomArmor(new ItemStack(material));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1f);
            createInventory();
            display(player);
        });
    }
    
    private void setRemoveArmorItem() {
        inventory.setItem(17, new ItemBuilder(Material.BARRIER).setDisplayName("§4Remove custom armor").build());
        inventory.setClickAction(17, player -> {
            data.setCustomArmor(null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1f);
            createInventory();
            display(player);
        });
    }
    
    private void setRemoveColorItem() {
        inventory.setItem(8, new ItemBuilder(Material.BARRIER).setDisplayName("§4Remove custom color").build());
        inventory.setClickAction(8, player -> {
            data.setCustomColor(null);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5F, 1f);
            createInventory();
            display(player);
        });
    }
}
