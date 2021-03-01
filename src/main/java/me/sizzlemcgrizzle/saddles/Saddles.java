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
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Saddles extends JavaPlugin implements Listener {
    
    public static NamespacedKey SADDLE_MOUNT_KEY;
    public static NamespacedKey SADDLE_LEGACY_KEY;
    public static NamespacedKey SADDLE_COLOR_KEY;
    public static NamespacedKey SADDLE_ARMOR_KEY;
    public static NamespacedKey SADDLE_TYPE_KEY;
    public static NamespacedKey SADDLE_SPEED_LEVEL_KEY;
    public static NamespacedKey SADDLE_JUMP_LEVEL_KEY;
    public static NamespacedKey SADDLE_UUID_KEY;
    
    //Mount UUID -> MountData
    private Map<UUID, AbstractHorseMount> mounts = new HashMap<>();
    private List<UUID> mountCooldowns = new ArrayList<>();
    
    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(AbstractHorseMount.class);
        ConfigurationSerialization.registerClass(HorseMount.class);
        ConfigurationSerialization.registerClass(DonkeyMount.class);
        
        Config.load(this);
        
        SADDLE_MOUNT_KEY = new NamespacedKey(this, "saddleMount");
        SADDLE_LEGACY_KEY = new NamespacedKey(this, "saddleSpawner");
        SADDLE_COLOR_KEY = new NamespacedKey(this, "saddleColor");
        SADDLE_ARMOR_KEY = new NamespacedKey(this, "saddleArmor");
        SADDLE_TYPE_KEY = new NamespacedKey(this, "saddleType");
        SADDLE_SPEED_LEVEL_KEY = new NamespacedKey(this, "saddleSpeedLevel");
        SADDLE_JUMP_LEVEL_KEY = new NamespacedKey(this, "saddleJumpLevel");
        SADDLE_UUID_KEY = new NamespacedKey(this, "saddleUUID");
        
        MessageUtil.register(this, new TextComponent(Config.PREFIX), ChatColor.WHITE, ChatColor.YELLOW, ChatColor.RED, ChatColor.DARK_RED, ChatColor.DARK_AQUA, ChatColor.GREEN);
        getCommand("saddles").setExecutor(new SaddlesCommandHandler(this));
        Bukkit.getPluginManager().registerEvents(this, this);
        
        File file = new File(getDataFolder(), "mounts.yml");
        
        if (!file.exists())
            saveResource(file.getName(), false);
        
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        ((List<AbstractHorseMount>) config.getList("mounts", new ArrayList<>())).forEach(m -> mounts.put(m.getMountID(), m));
        
        //Clear all existing horses with special tag upon start
        Bukkit.getWorlds().forEach(w -> w.getLivingEntities().stream().filter(e -> hasMountKey(e.getPersistentDataContainer())).forEach(Entity::remove));
    }
    
    @Override
    public void onDisable() {
        
        mounts.values().forEach(AbstractHorseMount::remove);
        
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
    
    public AbstractHorseMount getMount(ItemStack item, UUID lastOwner) {
        PersistentDataContainer c = item.getItemMeta().getPersistentDataContainer();
        UUID uuid = getSaddleUUID(c);
        
        if (mounts.containsKey(uuid))
            return mounts.get(uuid);
        
        EntityType type = getEntityType(c);
        
        AbstractHorseMount mount;
        switch (type) {
            case DONKEY:
                mount = new DonkeyMount(uuid, lastOwner, getSaddleSpeedLevel(c), getSaddleJumpLevel(c));
                break;
            case ZOMBIE_HORSE:
                mount = new ZombieHorseMount(uuid, lastOwner, getSaddleSpeedLevel(c), getSaddleJumpLevel(c));
                break;
            case SKELETON_HORSE:
                mount = new SkeletonHorseMount(uuid, lastOwner, getSaddleSpeedLevel(c), getSaddleJumpLevel(c));
                break;
            case LLAMA:
                mount = new LLamaMount(uuid, lastOwner, getSaddleSpeedLevel(c), getSaddleJumpLevel(c));
                break;
            default:
                mount = new HorseMount(uuid, lastOwner, getSaddleSpeedLevel(c), getSaddleJumpLevel(c), getSaddleColor(c), getSaddleArmor(c));
                break;
        }
        
        mounts.put(uuid, mount);
        return mount;
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
        
        AbstractHorseMount mount = getMount(item, player.getUniqueId());
        player.getInventory().setItemInMainHand(formatItemStack(item, player, mount));
        
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (mount instanceof HorseMount)
                ((HorseMount) mount).display(this, player);
            return;
        }
        
        Location location = player.getLocation();
        
        MessageUtil.sendMessage(this, player, MessageLevel.INFO, "Spawning your mount... stay still for " + (Config.MOUNT_SPAWN_DELAY / (double) 20) + " seconds...");
        
        new LambdaRunnable(() -> {
            if (player.getLocation().distanceSquared(location) < 1)
                mount.spawn(player);
            else
                MessageUtil.sendMessage(this, player, MessageLevel.WARNING, "You moved and your mount did not spawn!");
        }).runTaskLater(this, Config.MOUNT_SPAWN_DELAY);
    }
    
    private ItemStack formatItemStack(ItemStack item, Player lastOwner, AbstractHorseMount mount) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        
        lore.removeIf(s -> s.contains("§7Level: ") || s.contains("§7Time ridden: ") || s.contains("§7Last owner: "));
        
        if (lore.isEmpty() || lore.stream().noneMatch(line -> line.contains("Owner: "))) {
            lore.add("");
            lore.add(ChatColor.GRAY + "Mount type: " + ChatColor.GOLD + getEntityType(item.getItemMeta().getPersistentDataContainer()).name().toLowerCase().replace("_", " "));
            lore.add(ChatColor.GRAY + "Owner: " + ChatColor.GOLD + lastOwner.getName());
            lore.add(ChatColor.GRAY + "Speed: " + ChatColor.GOLD + mount.formatLevel(mount.getSpeedLevel()));
            lore.add(ChatColor.GRAY + "Jump: " + ChatColor.GOLD + mount.formatLevel(mount.getJumpLevel()));
        } else
            lore = lore.stream().map(s -> {
                if (s.contains("Owner: "))
                    s = ChatColor.GRAY + "Owner: " + ChatColor.GOLD + lastOwner.getName();
                else if (s.contains("Speed: "))
                    s = ChatColor.GRAY + "Speed: " + ChatColor.GOLD + mount.formatLevel(mount.getSpeedLevel());
                else if (s.contains("Jump: "))
                    s = ChatColor.GRAY + "Jump: " + ChatColor.GOLD + mount.formatLevel(mount.getJumpLevel());
                return s;
            }).collect(Collectors.toList());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryUse(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        
        if (!(inventory instanceof AbstractHorseInventory))
            return;
        
        AbstractHorseInventory horseInventory = (AbstractHorseInventory) inventory;
        
        if (horseInventory.getSaddle() == null)
            return;
        
        if (horseInventory instanceof LlamaInventory) {
            if (((LlamaInventory) horseInventory).getDecor() == null)
                return;
            else if (!hasMountKey(((LlamaInventory) horseInventory).getDecor().getItemMeta().getPersistentDataContainer()))
                return;
        } else if (!hasMountKey(horseInventory.getSaddle().getItemMeta().getPersistentDataContainer()))
            return;
        
        if (event.getClickedInventory() != null && !event.getClickedInventory().equals(event.getWhoClicked().getInventory()))
            if (event.getSlot() == 1 || event.getSlot() == 0)
                event.setCancelled(true);
        
        mounts.values().stream().filter(m -> m instanceof ChestedMount && m.getMount() != null)
                .filter(m -> m.getMount().getInventory().equals(horseInventory)).findFirst()
                .ifPresent(m -> new LambdaRunnable(() -> ((ChestedMount) m).setInventory(m.getMount().getInventory())).runTaskLater(this, 1));
        
        
    }
    
    @EventHandler
    public void onHorseDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof AbstractHorse))
            return;
        
        AbstractHorse horse = (AbstractHorse) event.getEntity();
        
        if (!hasMountKey(horse.getPersistentDataContainer()))
            return;
        
        event.setCancelled(true);
        
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
            mounts.values().stream().filter(e -> e.getMount() != null && e.getMount().equals(horse)).findFirst().ifPresent(AbstractHorseMount::remove);
    }
    
    //Set mount direction to where player is facing upon mount
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHorseRightClick(PlayerInteractEntityEvent e) {
        if (!(e.getRightClicked() instanceof AbstractHorse))
            return;
        
        AbstractHorse mount = (AbstractHorse) e.getRightClicked();
        
        if (!hasSaddleKey(mount.getPersistentDataContainer()) && !hasMountKey(mount.getPersistentDataContainer()))
            return;
        
        mount.remove();
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (player.getVehicle() == null)
            return;
        
        if (!hasMountKey(player.getVehicle().getPersistentDataContainer()))
            return;
        
        mounts.values().stream().filter(e -> e.getMount() != null && e.getMount().getUniqueId().equals(player.getVehicle().getUniqueId()))
                .findFirst().ifPresent(AbstractHorseMount::remove);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMountExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player))
            return;
        
        if (!(event.getVehicle() instanceof AbstractHorse))
            return;
        
        if (hasMountKey(event.getVehicle().getPersistentDataContainer()))
            mounts.values().stream()
                    .filter(e -> e.getMount() != null && e.getMount().equals(event.getVehicle()))
                    .forEach(AbstractHorseMount::remove);
    }
    
    public EntityType getEntityType(PersistentDataContainer container) {
        return container.has(SADDLE_TYPE_KEY, PersistentDataType.STRING) ? EntityType.valueOf(container.get(SADDLE_TYPE_KEY, PersistentDataType.STRING)) : EntityType.HORSE;
    }
    
    public boolean hasSaddleKey(PersistentDataContainer container) {
        return container.has(SADDLE_LEGACY_KEY, PersistentDataType.INTEGER)
                || container.has(SADDLE_UUID_KEY, PersistentDataType.STRING)
                || container.has(SADDLE_TYPE_KEY, PersistentDataType.STRING);
    }
    
    public int getSaddleSpeedLevel(PersistentDataContainer container) {
        if (container.has(SADDLE_SPEED_LEVEL_KEY, PersistentDataType.INTEGER) && container.get(SADDLE_SPEED_LEVEL_KEY, PersistentDataType.INTEGER) != -1)
            return container.get(SADDLE_SPEED_LEVEL_KEY, PersistentDataType.INTEGER);
        else
            return getRandomlevel();
    }
    
    public int getSaddleJumpLevel(PersistentDataContainer container) {
        if (container.has(SADDLE_JUMP_LEVEL_KEY, PersistentDataType.INTEGER) && container.get(SADDLE_JUMP_LEVEL_KEY, PersistentDataType.INTEGER) != -1)
            return container.get(SADDLE_JUMP_LEVEL_KEY, PersistentDataType.INTEGER);
        else
            return getRandomlevel();
    }
    
    private int getRandomlevel() {
        //0-9
        int random = (int) (Math.random() * 10);
        switch (random) {
            case 0:
                return 5;
            case 1:
            case 2:
                return 4;
            case 3:
            case 4:
                return 3;
            case 5:
            case 6:
                return 2;
            default:
                return 1;
        }
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
    
    public Horse.Color getSaddleColor(PersistentDataContainer container) {
        return container.has(SADDLE_COLOR_KEY, PersistentDataType.STRING) ?
                Horse.Color.valueOf(container.get(SADDLE_COLOR_KEY, PersistentDataType.STRING)) :
                Horse.Color.values()[(int) (Math.random() * Horse.Color.values().length)];
    }
    
    public Material getSaddleArmor(PersistentDataContainer container) {
        return container.has(SADDLE_ARMOR_KEY, PersistentDataType.STRING) ?
                Material.valueOf(container.get(SADDLE_ARMOR_KEY, PersistentDataType.STRING)) :
                Material.AIR;
    }
    
    public boolean hasMountKey(PersistentDataContainer container) {
        return container.has(SADDLE_MOUNT_KEY, PersistentDataType.STRING);
    }
}
