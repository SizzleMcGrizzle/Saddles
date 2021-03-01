package me.sizzlemcgrizzle.saddles;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractHorseMount implements ConfigurationSerializable {
    private UUID mountID;
    private UUID ownerID;
    
    private AbstractHorse mount;
    private int speedLevel;
    private int jumpLevel;
    
    public AbstractHorseMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel) {
        this.mountID = mountID;
        this.ownerID = ownerID;
        this.speedLevel = speedLevel;
        this.jumpLevel = jumpLevel;
    }
    
    public AbstractHorseMount(Map<String, Object> map) {
        this.mountID = UUID.fromString((String) map.get("mountID"));
        this.ownerID = UUID.fromString((String) map.get("ownerID"));
        this.speedLevel = (int) map.get("speedLevel");
        this.jumpLevel = (int) map.get("jumpLevel");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("mountID", mountID.toString());
        map.put("ownerID", ownerID.toString());
        map.put("jumpLevel", jumpLevel);
        map.put("speedLevel", speedLevel);
        return map;
    }
    
    protected void setAttributes() {
        
        if (mount == null)
            return;
        
        mount.setOwner(Bukkit.getPlayer(ownerID));
        mount.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(0);
        mount.setCustomName(ChatColor.GREEN + Bukkit.getOfflinePlayer(ownerID).getName() + "'s mount");
        mount.getPersistentDataContainer().set(Saddles.SADDLE_MOUNT_KEY, PersistentDataType.STRING, mountID.toString());
        getMount().setJumpStrength(calculateJump());
        getMount().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(calculateSpeed());
        
        ItemStack saddle = new ItemStack(Material.SADDLE);
        ItemMeta meta = saddle.getItemMeta();
        meta.getPersistentDataContainer().set(Saddles.SADDLE_MOUNT_KEY, PersistentDataType.STRING, mountID.toString());
        saddle.setItemMeta(meta);
        
        mount.getInventory().setSaddle(saddle);
    }
    
    protected abstract double calculateJump();
    
    protected abstract double calculateSpeed();
    
    public void spawn(Player player) {
        remove();
        
        this.ownerID = player.getUniqueId();
        
        if (this instanceof DonkeyMount)
            this.mount = (Donkey) player.getWorld().spawnEntity(player.getLocation(), EntityType.DONKEY);
        else if (this instanceof ZombieHorseMount)
            this.mount = (ZombieHorse) player.getWorld().spawnEntity(player.getLocation(), EntityType.ZOMBIE_HORSE);
        else if (this instanceof SkeletonHorseMount)
            this.mount = (SkeletonHorse) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON_HORSE);
        else if (this instanceof LLamaMount)
            this.mount = (Llama) player.getWorld().spawnEntity(player.getLocation(), EntityType.LLAMA);
        else
            this.mount = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        
        setAttributes();
        mount.addPassenger(player);
    }
    
    public void remove() {
        if (mount != null)
            mount.remove();
    }
    
    public AbstractHorse getMount() {
        return mount;
    }
    
    public int getSpeedLevel() {
        return speedLevel;
    }
    
    public int getJumpLevel() {
        return jumpLevel;
    }
    
    public void setSpeedLevel(int speedLevel) {
        this.speedLevel = speedLevel;
    }
    
    public void setJumpLevel(int jumpLevel) {
        this.jumpLevel = jumpLevel;
    }
    
    public UUID getOwnerID() {
        return ownerID;
    }
    
    public UUID getMountID() {
        return mountID;
    }
    
    public String formatLevel(int level) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < level; i++)
            builder.append("✰");
        return builder.toString();
    }
}
