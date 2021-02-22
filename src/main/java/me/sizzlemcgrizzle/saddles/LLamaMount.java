package me.sizzlemcgrizzle.saddles;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LLamaMount extends AbstractHorseMount implements ChestedMount {
    
    private int strengthLevel;
    private Material carpet;
    
    private Map<Integer, ItemStack> inventory = new HashMap<>();
    
    public LLamaMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel) {
        super(mountID, ownerID, speedLevel, jumpLevel);
        
        this.strengthLevel = (int) (Math.random() * 5) + 1;
        List<Material> list = Arrays.stream(Material.values()).filter(m -> m.name().contains("_CARPET")).collect(Collectors.toList());
        this.carpet = list.get((int) (Math.random() * list.size()));
    }
    
    public LLamaMount(Map<String, Object> map) {
        super(map);
        
        this.strengthLevel = (int) map.get("strengthLevel");
        this.carpet = Material.valueOf((String) map.get("carpet"));
        
        for (int i = 2; i < 18; i++) {
            if (!map.containsKey(String.valueOf(i)))
                continue;
            
            inventory.put(i, (ItemStack) map.get(String.valueOf(i)));
        }
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        
        map.put("strengthLevel", strengthLevel);
        map.put("carpet", carpet.name());
        inventory.forEach((slot, item) -> map.put(String.valueOf(slot), item));
        
        return map;
    }
    
    @Override
    public void remove() {
        if (getMount() != null) {
            setInventory(getMount().getInventory());
            getMount().remove();
        }
    }
    
    @Override
    protected void setAttributes() {
        super.setAttributes();
        
        if (getMount() == null)
            return;
        
        ItemStack carpetItem = new ItemStack(carpet);
        ItemMeta meta = carpetItem.getItemMeta();
        meta.getPersistentDataContainer().set(Saddles.SADDLE_UUID_KEY, PersistentDataType.STRING, getMountID().toString());
        carpetItem.setItemMeta(meta);
        
        getMount().setCarryingChest(true);
        getMount().getInventory().setDecor(carpetItem);
        getMount().setStrength(strengthLevel);
        inventory.forEach((slot, item) -> getMount().getInventory().setItem(slot, item));
    }
    
    @Override
    public void spawn(Player player) {
        super.spawn(player);
        
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1F, 1F);
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
    public Llama getMount() {
        return (Llama) super.getMount();
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
