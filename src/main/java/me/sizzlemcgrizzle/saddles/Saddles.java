package me.sizzlemcgrizzle.saddles;

import de.craftlancer.core.LambdaRunnable;
import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import me.sizzlemcgrizzle.saddles.command.SaddlesCommandHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Saddles extends JavaPlugin implements Listener {
    
    public static NamespacedKey SADDLE_KEY;
    public static NamespacedKey SADDLE_UUID_KEY;
    
    //Stores the locations of mounted players to prevent standing still on mounts gaining xp.
    private Map<UUID, Location> lastLocations = new HashMap<>();
    //Mount UUID -> MountData
    private Map<UUID, MountData> mounts = new HashMap<>();
    private List<UUID> mountCooldowns = new ArrayList<>();
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(MountData.class);
        
        Config.load(this);
        
        SADDLE_KEY = new NamespacedKey(this, "saddleSpawner");
        SADDLE_UUID_KEY = new NamespacedKey(this, "saddleUUID");
        
        MessageUtil.register(this, new TextComponent(Config.PREFIX), ChatColor.WHITE, ChatColor.YELLOW, ChatColor.RED, ChatColor.DARK_RED, ChatColor.DARK_AQUA, ChatColor.GREEN);
        getCommand("saddles").setExecutor(new SaddlesCommandHandler(this));
        Bukkit.getPluginManager().registerEvents(this, this);
        
        File file = new File(getDataFolder(), "mounts.yml");
        
        if (!file.exists())
            saveResource(file.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        ((List<MountData>) config.getList("mounts", new ArrayList<>())).forEach(m -> mounts.put(m.getId(), m));
        
        new SaddleTickRunnable(tickID -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                
                if (p.getVehicle() == null)
                    return;
                
                if (tickID % 100 == 0)
                    lastLocations.put(p.getUniqueId(), p.getLocation());
                
                List<MountData> pMounts = mounts.values().stream()
                        .filter(m -> m.getHorse() != null)
                        .filter(m -> m.getLastOwner().equals(p.getUniqueId()))
                        .collect(Collectors.toList());
                
                if (pMounts.size() == 0)
                    return;
                
                for (MountData mountData : pMounts)
                    if (p.getVehicle() != null && p.getVehicle().equals(mountData.getHorse()))
                        if (!lastLocations.containsKey(p.getUniqueId()) || p.getLocation().distanceSquared(lastLocations.get(p.getUniqueId())) > 1)
                            mountData.increaseTicks(this);
            }
        }).runTaskTimer(this, 0, 1);
        
        //Clear all existing horses with special tag upon start
        Bukkit.getWorlds().forEach(w -> w.getLivingEntities().stream().filter(e -> hasSaddleKey(e.getPersistentDataContainer())).forEach(Entity::remove));
    }
    
    @Override
    public void onDisable() {
        
        File file = new File(getDataFolder(), "mounts.yml");
        
        if (!file.exists())
            saveResource(file.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        config.set("mounts", new ArrayList<>(mounts.values()));
        
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public MountData getMountData(ItemStack item, UUID lastOwner) {
        UUID uuid = getSaddleUUID(item.getItemMeta().getPersistentDataContainer());
        int baseLevel = getSaddleBaseLevel(item.getItemMeta().getPersistentDataContainer());
        
        return mounts.compute(uuid,
                (k, v) -> v == null ? new MountData(uuid, lastOwner, baseLevel) : v);
    }
    
    @EventHandler
    public void onSaddleUse(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.SADDLE)
            return;
        
        if (!hasSaddleKey(item.getItemMeta().getPersistentDataContainer()))
            return;
        
        //Check for cooldown
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR)
            if (mountCooldowns.contains(player.getUniqueId()))
                return;
            else {
                mountCooldowns.add(player.getUniqueId());
                new LambdaRunnable(() -> mountCooldowns.remove(player.getUniqueId())).runTaskLater(this, Config.MOUNT_SPAWN_DELAY);
            }
        
        //Get UUID to get saddle, if it exists get it, if not create a new UUID and add it
        if (!hasSaddleUUID(item.getItemMeta().getPersistentDataContainer()))
            setSaddleUUID(item, UUID.randomUUID());
        
        MountData data = getMountData(item, player.getUniqueId());
        player.getInventory().setItemInMainHand(formatItemStack(item, player, data));
        
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            Location location = player.getLocation();
            
            MessageUtil.sendMessage(this, player, MessageLevel.INFO, "Spawning your mount... stay still for " + (Config.MOUNT_SPAWN_DELAY / (double) 20) + " seconds...");
            
            new LambdaRunnable(() -> {
                if (player.getLocation().distanceSquared(location) < 1)
                    data.spawn(player);
                else
                    MessageUtil.sendMessage(this, player, MessageLevel.WARNING, "You moved and your mount did not spawn!");
            }).runTaskLater(this, Config.MOUNT_SPAWN_DELAY);
        } else if (player.hasPermission("saddles.editor"))
            data.openEditor(this, player);
    }
    
    private ItemStack formatItemStack(ItemStack item, Player lastOwner, MountData data) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        
        if (lore.isEmpty() || lore.stream().noneMatch(line -> line.contains("Last owner: "))) {
            lore.add("");
            lore.add(ChatColor.GRAY + "Last owner: " + ChatColor.GOLD + lastOwner.getName());
            lore.add(ChatColor.GRAY + "Time ridden: " + ChatColor.GOLD + data.formatTimeRidden());
            lore.add(ChatColor.GRAY + "Level: " + ChatColor.GOLD + data.formatLevel());
        } else
            lore = lore.stream().map(s -> {
                if (s.contains("Last owner: "))
                    s = ChatColor.GRAY + "Last owner: " + ChatColor.GOLD + lastOwner.getName();
                else if (s.contains("Time ridden: "))
                    s = ChatColor.GRAY + "Time ridden: " + ChatColor.GOLD + data.formatTimeRidden();
                else if (s.contains("Level: "))
                    s = ChatColor.GRAY + "Level: " + ChatColor.GOLD + data.formatLevel();
                return s;
            }).collect(Collectors.toList());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler
    public void onInventoryUse(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        
        if (!(inventory instanceof HorseInventory))
            return;
        
        HorseInventory horseInventory = (HorseInventory) inventory;
        
        if (!hasSaddleKey(horseInventory.getSaddle().getItemMeta().getPersistentDataContainer()))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void onInventoryUse(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        
        if (!(inventory instanceof HorseInventory))
            return;
        
        HorseInventory horseInventory = (HorseInventory) inventory;
        
        if (hasSaddleKey(horseInventory.getSaddle().getItemMeta().getPersistentDataContainer()))
            if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getWhoClicked().getInventory()))
                event.setCancelled(true);
    }
    
    @EventHandler
    public void onHorseDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Horse))
            return;
        
        Horse horse = (Horse) event.getEntity();
        
        if (!hasSaddleKey(horse.getPersistentDataContainer()))
            return;
        
        event.setCancelled(true);
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            horse.remove();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHorseRightClick(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Horse) {
            Player player = e.getPlayer();
            Horse horse = (Horse) e.getRightClicked();
            Location horseLoc = horse.getLocation().clone();
            horseLoc.setYaw(player.getLocation().getYaw());
            horseLoc.setDirection(player.getLocation().getDirection());
            horse.teleport(horseLoc);
        }
        
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMountExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player))
            return;
        
        if (!(event.getVehicle() instanceof Horse))
            return;
        
        if (hasSaddleKey(event.getVehicle().getPersistentDataContainer()))
            event.getVehicle().remove();
    }
    
    public boolean hasSaddleKey(PersistentDataContainer container) {
        return container.has(SADDLE_KEY, PersistentDataType.INTEGER);
    }
    
    public int getSaddleBaseLevel(PersistentDataContainer container) {
        return container.has(SADDLE_KEY, PersistentDataType.INTEGER) ? container.get(SADDLE_KEY, PersistentDataType.INTEGER) : 0;
    }
    
    public void setSaddleUUID(ItemStack item, UUID uuid) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(SADDLE_UUID_KEY, PersistentDataType.STRING, uuid.toString());
        item.setItemMeta(meta);
    }
    
    public boolean hasSaddleUUID(PersistentDataContainer container) {
        String id = container.get(SADDLE_UUID_KEY, PersistentDataType.STRING);
        return id != null && UUID.fromString(id) != null;
    }
    
    public UUID getSaddleUUID(PersistentDataContainer container) {
        String id = container.get(SADDLE_UUID_KEY, PersistentDataType.STRING);
        return id == null ? null : UUID.fromString(id);
    }
    
    public static class SaddleTickRunnable extends BukkitRunnable {
        
        private Consumer<Long> runnable;
        private long tickID;
        
        public SaddleTickRunnable(Consumer<Long> runnable) {
            this.runnable = runnable;
            this.tickID = 0;
        }
        
        @Override
        public void run() {
            runnable.accept(tickID);
            tickID++;
        }
    }
}
