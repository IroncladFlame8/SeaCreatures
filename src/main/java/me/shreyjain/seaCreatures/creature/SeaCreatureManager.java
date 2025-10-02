package me.shreyjain.seaCreatures.creature;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.user.SkillsUser;
import me.shreyjain.seaCreatures.SeaCreatures;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Loads creature definitions and provides selection + spawning utilities.
 */
public class SeaCreatureManager {

    private final SeaCreatures plugin;
    private final List<SeaCreatureDefinition> definitions = new ArrayList<>();
    private double baseChancePercent;
    private double perLuckLevelBonus;
    private LogLevel logLevel = LogLevel.INFO;

    private boolean auraEnabled;
    private double defaultFishingXp; // default per creature if not overridden by "fishing-xp" in creature entry
    private double fishingLuckBonusPerLevel;
    private AuraSkillsApi auraSkillsApi;
    private final Map<UUID, SeaCreatureDefinition> spawnedCreatures = new HashMap<>();

    public SeaCreatureManager(SeaCreatures plugin) { this.plugin = plugin; }

    public void reload() {
        plugin.reloadConfig();
        definitions.clear();
        FileConfiguration cfg = plugin.getConfig();

        // Core chance + logging settings
        baseChancePercent = cfg.getDouble("chance.base-percent", 25.0);
        perLuckLevelBonus = cfg.getDouble("chance.per-luck-level-bonus", 2.0);
        logLevel = LogLevel.from(cfg.getString("logging", "INFO"));

        // AuraSkills integration settings FIRST so defaults are available while parsing creatures
        auraEnabled = cfg.getBoolean("auraskills.enabled", true) && Bukkit.getPluginManager().isPluginEnabled("AuraSkills");
        if (auraEnabled) {
            try { auraSkillsApi = AuraSkillsApi.get(); }
            catch (Throwable t) { auraEnabled = false; plugin.getLogger().warning("AuraSkills API not accessible; disabling integration."); }
        }
        defaultFishingXp = cfg.getDouble("auraskills.xp.default-per-creature", 10.0);
        fishingLuckBonusPerLevel = cfg.getDouble("auraskills.stats.fishing-luck-bonus-per-level", 0.1); // percent per stat level

        // Now parse creature definitions (so per-creature fishing-xp falls back to defaultFishingXp correctly)
        List<Map<?, ?>> list = cfg.getMapList("creatures");
        for (Map<?, ?> raw : list) {
            try {
                SeaCreatureDefinition def = parse(raw);
                if (def != null) definitions.add(def);
            } catch (Exception ex) {
                log(Level.WARNING, "Failed to parse creature: " + raw + " => " + ex.getMessage());
            }
        }
        log(Level.INFO, "Loaded " + definitions.size() + " sea creature definitions.");
    }

    private SeaCreatureDefinition parse(Map<?, ?> map) {
        Object idObj = map.containsKey("id") ? map.get("id") : UUID.randomUUID().toString();
        String id = String.valueOf(idObj);
        String typeName = String.valueOf(map.get("type"));
        EntityType type;
        try { type = EntityType.valueOf(typeName.toUpperCase(Locale.ROOT)); }
        catch (Exception e) { log(Level.WARNING, "Unknown entity type '" + typeName + "' for id=" + id); return null; }
        int weight = asInt(map.get("weight"), 1);
        boolean custom = asBool(map.get("custom"), false);
        String name = map.containsKey("display-name") ? String.valueOf(map.get("display-name")) : null;
        boolean glowing = asBool(map.get("glowing"), false);
        Integer minY = map.containsKey("min-y") ? asInt(map.get("min-y"), Integer.MIN_VALUE) : null;
        Integer maxY = map.containsKey("max-y") ? asInt(map.get("max-y"), Integer.MAX_VALUE) : null;
        Set<String> biomes = map.containsKey("biomes") ? toUpperSet(map.get("biomes")) : null;
        Set<String> worlds = map.containsKey("worlds") ? toStringSet(map.get("worlds")) : null;

        List<PotionEffect> potionEffects = new ArrayList<>();
        Object peRaw = map.get("potion-effects");
        if (peRaw instanceof List<?> peList) {
            for (Object lineObj : peList) {
                String line = String.valueOf(lineObj);
                String[] parts = line.split(":");
                if (parts.length >= 3) {
                    PotionEffectType typePE = PotionEffectType.getByName(parts[0].toUpperCase(Locale.ROOT));
                    if (typePE != null) {
                        int amp = parseInt(parts[1], 0);
                        int dur = parseInt(parts[2], 200);
                        potionEffects.add(new PotionEffect(typePE, dur, amp));
                    }
                }
            }
        }

        Map<SeaCreatureDefinition.EquipmentSlotSimple, String> equipment = new EnumMap<>(SeaCreatureDefinition.EquipmentSlotSimple.class);
        Object eqRaw = map.get("equipment");
        if (eqRaw instanceof Map<?,?> eqMap) {
            for (Map.Entry<?,?> entry : eqMap.entrySet()) {
                try {
                    SeaCreatureDefinition.EquipmentSlotSimple slot = SeaCreatureDefinition.EquipmentSlotSimple.valueOf(entry.getKey().toString().toUpperCase(Locale.ROOT));
                    equipment.put(slot, entry.getValue().toString().toUpperCase(Locale.ROOT));
                } catch (Exception ignored) { }
            }
        }

        List<String> commands = new ArrayList<>();
        Object cmdRaw = map.get("commands");
        if (cmdRaw instanceof List<?> cmdList) {
            for (Object c : cmdList) commands.add(String.valueOf(c));
        }

        double fishingXp = defaultFishingXp; // fallback to configured default
        if (map.containsKey("fishing-xp")) {
            try { fishingXp = Double.parseDouble(String.valueOf(map.get("fishing-xp"))); } catch (Exception ignored) {}
        }

        return new SeaCreatureDefinition(id, type, weight, custom, name, glowing, potionEffects, equipment, commands, minY, maxY, biomes, worlds, fishingXp);
    }

    private int asInt(Object o, int def) { return o instanceof Number n ? n.intValue() : parseInt(String.valueOf(o), def); }
    private boolean asBool(Object o, boolean def) { return o == null ? def : Boolean.parseBoolean(String.valueOf(o)); }
    private int parseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception e) { return def; } }

    private Set<String> toUpperSet(Object o) { if (!(o instanceof List<?> list)) return null; return list.stream().map(v -> v.toString().toUpperCase(Locale.ROOT)).collect(Collectors.toSet()); }
    private Set<String> toStringSet(Object o) { if (!(o instanceof List<?> list)) return null; return list.stream().map(Object::toString).collect(Collectors.toSet()); }

    public List<SeaCreatureDefinition> getDefinitions() { return Collections.unmodifiableList(definitions); }

    public double computeChancePercent(int luckLevel) { double v = baseChancePercent + perLuckLevelBonus * luckLevel; return Math.max(0, Math.min(100, v)); }

    public SeaCreatureDefinition pickRandom(Location loc) {
        if (definitions.isEmpty()) return null;
        World world = loc.getWorld();
        int y = loc.getBlockY();
        Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        List<SeaCreatureDefinition> filtered = definitions.stream().filter(d -> {
            if (d.getMinY() != null && y < d.getMinY()) return false;
            if (d.getMaxY() != null && y > d.getMaxY()) return false;
            if (d.getWorlds() != null && !d.getWorlds().contains(world.getName())) return false;
            if (d.getBiomes() != null && !d.getBiomes().contains(biome.name().toUpperCase(Locale.ROOT))) return false;
            return true;
        }).toList();
        if (filtered.isEmpty()) return null;
        int total = filtered.stream().mapToInt(SeaCreatureDefinition::getWeight).sum();
        int r = ThreadLocalRandom.current().nextInt(total) + 1;
        int cumulative = 0;
        for (SeaCreatureDefinition def : filtered) { cumulative += def.getWeight(); if (r <= cumulative) return def; }
        return filtered.get(filtered.size() - 1);
    }

    public LivingEntity spawn(Location loc, SeaCreatureDefinition def, Player fisher) {
        Entity entity = loc.getWorld().spawnEntity(loc, def.getType());
        if (!(entity instanceof LivingEntity living)) return null;
        if (def.getDisplayName() != null) { String legacy = ChatColor.translateAlternateColorCodes('&', def.getDisplayName()); living.setCustomName(legacy); living.setCustomNameVisible(true); }
        living.setGlowing(def.isGlowing());
        for (PotionEffect effect : def.getPotionEffects()) living.addPotionEffect(effect);
        if (!def.getEquipmentMaterials().isEmpty() && living instanceof Mob mob) {
            EntityEquipment equip = mob.getEquipment();
            def.getEquipmentMaterials().forEach((slot, matName) -> {
                try { Material material = Material.valueOf(matName); ItemStack stack = new ItemStack(material); switch (slot) { case HAND -> equip.setItemInMainHand(stack); case OFF_HAND -> equip.setItemInOffHand(stack); case HEAD -> equip.setHelmet(stack); case CHEST -> equip.setChestplate(stack); case LEGS -> equip.setLeggings(stack); case FEET -> equip.setBoots(stack);} } catch (Exception ignored) { }
            });
        }
        for (String cmd : def.getCommands()) { String finalCmd = cmd.replace("%player%", fisher.getName()); Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd); }
        spawnedCreatures.put(living.getUniqueId(), def);
        log(Level.FINE, "Spawned sea creature id=" + def.getId() + " for " + fisher.getName());
        return living;
    }

    public void handleDeath(Entity entity, Player killer) {
        if (!auraEnabled || auraSkillsApi == null) return;
        SeaCreatureDefinition def = spawnedCreatures.remove(entity.getUniqueId());
        if (def == null) return; // not one of ours
        if (killer == null) return;
        try {
            SkillsUser user = auraSkillsApi.getUser(killer.getUniqueId());
            if (user != null) {
                double xp = def.getFishingXp();
                if (xp > 0) {
                    user.addSkillXp(Skills.FISHING, xp);
                    killer.sendMessage(ChatColor.GREEN + "+" + xp + " Fishing XP");
                }
            }
        } catch (Throwable ignored) { }
    }

    private void log(Level level, String msg) { if (logLevel == LogLevel.NONE) return; if (logLevel == LogLevel.INFO && level == Level.FINE) return; plugin.getLogger().log(level, msg); }

    public double getAdditionalFishingLuckChance(Player player) {
        if (!auraEnabled || auraSkillsApi == null) return 0.0;
        try {
            SkillsUser user = auraSkillsApi.getUser(player.getUniqueId());
            if (user == null) return 0.0;
            double statLevel = user.getStatLevel(Stats.LUCK);
            return statLevel * fishingLuckBonusPerLevel; // already percent units added before clamp
        } catch (Throwable t) {
            return 0.0;
        }
    }

    enum LogLevel { INFO, DEBUG, NONE; static LogLevel from(String s){ try { return LogLevel.valueOf(s.toUpperCase(Locale.ROOT)); } catch (Exception e){ return INFO; } } }

    public boolean isAuraEnabled() { return auraEnabled; }
}
