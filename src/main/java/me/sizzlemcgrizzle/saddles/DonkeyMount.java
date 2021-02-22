package me.sizzlemcgrizzle.saddles;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DonkeyMount extends AbstractHorseMount implements ChestedMount {
    
    private Map<Integer, ItemStack> inventory = new HashMap<>();
    
    public DonkeyMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel) {
        super(mountID, ownerID, speedLevel, jumpLevel);
    }
    
    public DonkeyMount(Map<String, Object> map) {
        super(map);
        
        for (int i = 2; i < 18; i++) {
            if (!map.containsKey(String.valueOf(i)))
                continue;
            
            inventory.put(i, (ItemStack) map.get(String.valueOf(i)));
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        inventory.forEach((slot, item) -> map.put(String.valueOf(slot), item));
        
        return map;
    }
    
    @Override
    protected void setAttributes() {
        super.setAttributes();
        
        if (getMount() == null)
            return;
        
        getMount().setCarryingChest(true);
        inventory.forEach((slot, item) -> getMount().getInventory().setItem(slot, item));
    }
    
    @Override
    protected double calculateJump() {
        return getJumpLevel() * 0.18;
    }
    
    @Override
    protected double calculateSpeed() {
        return getSpeedLevel() * 0.1;
    }
    
    @Override
    public void spawn(Player player) {
        super.spawn(player);
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DONKEY_AMBIENT, 1F, 1F);
    }
    
    @Override
    public Donkey getMount() {
        return super.getMount() == null ? null : (Donkey) super.getMount();
    }
    
    @Override
    public void remove() {
        if (getMount() != null) {
            setInventory(getMount().getInventory());
            getMount().remove();
        }
    }
    
    @Override
    public void setInventory(AbstractHorseInventory inventory) {
        
        this.inventory.clear();
        
        for (int i = 2; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() != Material.AIR)
                this.inventory.put(i, item);
        }
    }
}
