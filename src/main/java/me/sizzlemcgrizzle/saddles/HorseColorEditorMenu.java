package me.sizzlemcgrizzle.saddles;

import de.craftlancer.core.gui.GUIInventory;
import de.craftlancer.core.util.ItemBuilder;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;

public class HorseColorEditorMenu {
    private Saddles plugin;
    private HorseMount mount;
    private GUIInventory inventory;
    
    public HorseColorEditorMenu(Saddles plugin, HorseMount mount) {
        this.plugin = plugin;
        this.mount = mount;
    }
    
    public void display(Player player) {
        if (inventory == null)
            createInventory();
        
        player.openInventory(inventory.getInventory());
    }
    
    private void createInventory() {
        this.inventory = new GUIInventory(plugin, "§5Mount Editor", 1);
        inventory.fill(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName("§8").build());
        
        setColorItem(1, Horse.Color.WHITE, Material.SMOOTH_QUARTZ);
        setColorItem(2, Horse.Color.BLACK, Material.BLACK_CONCRETE);
        setColorItem(3, Horse.Color.GRAY, Material.GRAY_CONCRETE);
        setColorItem(4, Horse.Color.BROWN, Material.BROWN_CONCRETE);
        setColorItem(5, Horse.Color.DARK_BROWN, Material.BROWN_TERRACOTTA);
        setColorItem(6, Horse.Color.CREAMY, Material.BROWN_CONCRETE_POWDER);
        setColorItem(7, Horse.Color.CHESTNUT, Material.SPRUCE_PLANKS);
        
    }
    
    private void setColorItem(int slot, Horse.Color color, Material material) {
        inventory.setItem(slot, new ItemBuilder(material).setDisplayName("§f§l" + color.name().replace("_", " "))
                .addLore("", "§7Click to set mount color to " + color.name().replace("_", " "))
                .setEnchantmentGlow(mount.getColor() == color).build());
        inventory.setClickAction(slot, player -> {
            if (!player.hasPermission("saddles.editor")) {
                MessageUtil.sendMessage(plugin, player, MessageLevel.INFO, "You do not have permission to use this feature.");
                return;
            }
            
            mount.setColor(color);
            mount.setAttributes();
            player.playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 0.5F, 1f);
            createInventory();
            display(player);
        });
    }
}
