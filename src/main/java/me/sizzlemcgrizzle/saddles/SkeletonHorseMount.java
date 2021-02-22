package me.sizzlemcgrizzle.saddles;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class SkeletonHorseMount extends AbstractHorseMount {
    public SkeletonHorseMount(UUID mountID, UUID ownerID, int speedLevel, int jumpLevel) {
        super(mountID, ownerID, speedLevel, jumpLevel);
    }
    
    public SkeletonHorseMount(Map<String, Object> map) {
        super(map);
    }
    
    @Override
    public void spawn(Player player) {
        super.spawn(player);
        
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SKELETON_HORSE_AMBIENT, 1F, 1F);
    }
    
    @Override
    protected double calculateJump() {
        return getJumpLevel() * 0.22;
    }
    
    @Override
    protected double calculateSpeed() {
        return getSpeedLevel() * 0.12;
    }
}
