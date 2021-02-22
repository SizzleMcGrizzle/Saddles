package me.sizzlemcgrizzle.saddles;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieHorse;

import java.util.Map;
import java.util.UUID;

public class ZombieHorseMount extends AbstractHorseMount {
    public ZombieHorseMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel) {
        super(mountID, ownerID, speedLevel, jumpLevel);
    }
    
    public ZombieHorseMount(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    public void spawn(Player player) {
        super.spawn(player);
        
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_HORSE_AMBIENT, 1F, 1F);
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
    public ZombieHorse getMount() {
        return (ZombieHorse) super.getMount();
    }
}
