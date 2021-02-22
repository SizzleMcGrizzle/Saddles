package me.sizzlemcgrizzle.saddles;

import de.craftlancer.core.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class HorseMount extends AbstractHorseMount {
    
    private Horse.Color color;
    private Material armor;
    private HorseColorEditorMenu menu;
    
    public HorseMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel, Horse.Color color, Material armor) {
        super(mountID, ownerID, speedLevel, jumpLevel);
        
        this.color = color;
        this.armor = armor;
    }
    
    public HorseMount(Map<String, Object> map) {
        super(map);
        
        this.color = Horse.Color.valueOf((String) map.get("color"));
        this.armor = Material.valueOf((String) map.get("armor"));
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("color", color.name());
        map.put("armor", armor.name());
        
        return map;
    }
    
    @Override
    protected void setAttributes() {
        super.setAttributes();
        
        if (getMount() == null)
            return;
        
        ItemStack armorItem = new ItemBuilder(armor == Material.AIR ? Material.BLACK_STAINED_GLASS_PANE : armor)
                .setDisplayName(armor == Material.AIR ? "§8§oInvisible armor... spooky" : null).build();
        getMount().getInventory().setArmor(armorItem);
        getMount().setColor(color);
    }
    
    @Override
    protected double calculateJump() {
        return getJumpLevel() * 0.22;
    }
    
    @Override
    protected double calculateSpeed() {
        return getSpeedLevel() * 0.12;
    }
    
    @Override
    public void spawn(Player player) {
        super.spawn(player);
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_AMBIENT, 1F, 1F);
    }
    
    @Override
    public Horse getMount() {
        return super.getMount() == null ? null : (Horse) super.getMount();
    }
    
    public void display(Saddles plugin, Player player) {
        if (menu == null)
            menu = new HorseColorEditorMenu(plugin, this);
        
        menu.display(player);
    }
    
    public void setColor(Horse.Color color) {
        this.color = color;
    }
    
    public Horse.Color getColor() {
        return color;
    }
}
