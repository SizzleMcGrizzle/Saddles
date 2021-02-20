package me.sizzlemcgrizzle.saddles;

import de.craftlancer.core.util.MessageLevel;
import de.craftlancer.core.util.MessageUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MountData implements ConfigurationSerializable {
    
    private UUID id;
    private UUID lastOwner;
    
    private long ticksRidden;
    private int level;
    private Horse.Color color;
    private ItemStack customArmor;
    private Horse.Color customColor;
    
    private MountEditorMenu menu;
    private Horse horse;
    
    public MountData(UUID id, UUID lastOwner) {
        this.id = id;
        this.lastOwner = lastOwner;
        this.ticksRidden = Config.REQUIRED_TICKS[0];
        level = 0;
        setAttributes();
    }
    
    public MountData(Map<String, Object> map) {
        this.id = UUID.fromString((String) map.get("id"));
        this.lastOwner = UUID.fromString((String) map.get("owner"));
        this.ticksRidden = Long.parseLong((String) map.get("ticks"));
        
        this.customArmor = map.containsKey("armor") ? (ItemStack) map.get("armor") : null;
        this.customColor = map.containsKey("color") ? Horse.Color.valueOf((String) map.get("color")) : null;
        
        calculateLevel();
        setAttributes();
    }
    
    private void calculateLevel() {
        if (ticksRidden >= Config.REQUIRED_TICKS[Config.REQUIRED_TICKS.length - 1])
            this.level = 29;
        else
            for (int i = 0; i < Config.REQUIRED_TICKS.length - 1; i++) {
                if (ticksRidden >= Config.REQUIRED_TICKS[i] && ticksRidden < Config.REQUIRED_TICKS[i + 1]) {
                    this.level = i;
                    break;
                }
            }
    }
    
    public void openEditor(Saddles plugin, Player player) {
        if (menu == null)
            menu = new MountEditorMenu(plugin, this);
        
        menu.display(player);
    }
    
    private void setAttributes() {
        int calcLevel = level + 4;
        
        double speed = calcLevel / 30D;
        //double jump = calcLevel / 25D;
        double jump = Math.log(calcLevel) / 3;
        
        if (customColor == null)
            switch ((level) / 10) {
                case 0:
                    color = Horse.Color.BROWN;
                    break;
                case 1:
                    color = Horse.Color.BLACK;
                    break;
                case 2:
                    color = Horse.Color.WHITE;
                    break;
            }
        else
            color = customColor;
        
        if (horse != null) {
            horse.setOwner(Bukkit.getPlayer(lastOwner));
            horse.setJumpStrength(jump);
            horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(speed);
            horse.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(0);
            horse.setColor(color);
            horse.setCustomName(ChatColor.GREEN + Bukkit.getOfflinePlayer(lastOwner).getName() + "'s mount §7(" + (level + 1) + "/30)");
            
            horse.getPersistentDataContainer().set(Saddles.SADDLE_KEY, PersistentDataType.STRING, "true");
            
            ItemStack saddle = new ItemStack(Material.SADDLE);
            ItemMeta meta = saddle.getItemMeta();
            meta.getPersistentDataContainer().set(Saddles.SADDLE_KEY, PersistentDataType.STRING, "true");
            saddle.setItemMeta(meta);
            
            horse.getInventory().setSaddle(saddle);
            
            if (customArmor == null)
                switch ((level) / 10) {
                    case 0:
                        horse.getInventory().setArmor(new ItemStack(Material.IRON_HORSE_ARMOR));
                        break;
                    case 1:
                        horse.getInventory().setArmor(new ItemStack(Material.GOLDEN_HORSE_ARMOR));
                        break;
                    case 2:
                        horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
                        break;
                }
            else
                horse.getInventory().setArmor(customArmor);
        }
    }
    
    public void setTicksRidden(long ticksRidden) {
        this.ticksRidden = ticksRidden;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        
        map.put("id", id.toString());
        map.put("owner", lastOwner.toString());
        map.put("ticks", String.valueOf(ticksRidden));
        if (customColor != null)
            map.put("color", customColor.toString());
        if (customArmor != null)
            map.put("armor", customArmor);
        
        return map;
    }
    
    public Horse.Color getCustomColor() {
        return customColor;
    }
    
    public ItemStack getCustomArmor() {
        return customArmor;
    }
    
    public void setCustomArmor(ItemStack customArmor) {
        this.customArmor = customArmor;
        if (horse != null)
            setAttributes();
    }
    
    public void setCustomColor(Horse.Color customColor) {
        this.customColor = customColor;
        if (horse != null)
            setAttributes();
    }
    
    public boolean spawn(Player player) {
        if (horse != null)
            horse.remove();
        
        this.lastOwner = player.getUniqueId();
        this.horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
        
        setAttributes();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1F, 1F);
        horse.addPassenger(player);
        
        return true;
    }
    
    public Horse getHorse() {
        return horse;
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getLastOwner() {
        return lastOwner;
    }
    
    public int getLevel() {
        return level;
    }
    
    public long getTicksRidden() {
        return ticksRidden;
    }
    
    public void increaseTicks(Saddles saddles) {
        ticksRidden++;
        if (ticksRidden % 20 == 0) {
            int prev = level;
            calculateLevel();
            displayActionBar();
            if (level != prev) {
                setAttributes();
                if (Bukkit.getPlayer(lastOwner) != null)
                    MessageUtil.sendMessage(saddles, Bukkit.getPlayer(lastOwner), MessageLevel.SUCCESS, "Your mount has leveled up! (" + (level + 1) + "/30)");
            }
        }
    }
    
    public String formatTimeRidden() {
        long seconds = (ticksRidden / 20);
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60));
        return String.format("%dh:%02dm:%02ds", h, m, s);
    }
    
    public String formatLevel() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < Config.REQUIRED_TICKS.length; i++)
            if (i % (Config.REQUIRED_TICKS.length / 5) == 0 && level >= i)
                builder.append("✰");
        return builder.toString();
    }
    
    private void displayActionBar() {
        Player player = Bukkit.getPlayer(lastOwner);
        
        if (player == null || !player.isOnline())
            return;
        
        if (level >= Config.REQUIRED_TICKS.length - 1) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§6§lMAXIMUM LEVEL - " + Config.REQUIRED_TICKS.length));
            return;
        }
        
        double progress = (ticksRidden - Config.REQUIRED_TICKS[level]) / ((double) (Config.REQUIRED_TICKS[level + 1] - Config.REQUIRED_TICKS[level]));
        
        ComponentBuilder progressBar = new ComponentBuilder("[ ").color(ChatColor.DARK_GRAY).bold(true);
        
        progressBar.append("Mount Level " + (level + 1) + " -> " + (level + 2) + " ").color(ChatColor.DARK_PURPLE).bold(true);
        for (int i = 0; i < 24; ++i) {
            progressBar.append("|");
            
            if ((double) ((float) i / 24.0F) >= progress)
                progressBar.color(ChatColor.RED);
            else
                progressBar.color(ChatColor.GREEN);
        }
        progressBar.append(" " + String.format("%.2f", progress * 100) + "%").color(ChatColor.DARK_PURPLE).bold(true);
        
        progressBar.append(" ]").color(ChatColor.DARK_GRAY).bold(true);
        
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(progressBar.create()));
    }
}
