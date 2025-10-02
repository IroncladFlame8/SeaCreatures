package me.shreyjain.seaCreatures.creature;

import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable definition parsed from config.yml representing a fishable sea creature.
 */
public class SeaCreatureDefinition {
    private final String id;
    private final EntityType type;
    private final int weight;
    private final boolean custom;
    private final String displayName; // legacy color codes supported
    private final boolean glowing;
    private final List<PotionEffect> potionEffects;
    private final Map<EquipmentSlotSimple, String> equipmentMaterials; // Material enum name per slot
    private final List<String> commands;
    private final Integer minY;
    private final Integer maxY;
    private final Set<String> biomes; // Uppercase names
    private final Set<String> worlds; // Exact world names
    private final double fishingXp; // AuraSkills fishing XP reward

    public SeaCreatureDefinition(String id,
                                 EntityType type,
                                 int weight,
                                 boolean custom,
                                 String displayName,
                                 boolean glowing,
                                 List<PotionEffect> potionEffects,
                                 Map<EquipmentSlotSimple, String> equipmentMaterials,
                                 List<String> commands,
                                 Integer minY,
                                 Integer maxY,
                                 Set<String> biomes,
                                 Set<String> worlds,
                                 double fishingXp) {
        this.id = id;
        this.type = type;
        this.weight = weight <= 0 ? 1 : weight;
        this.custom = custom;
        this.displayName = displayName;
        this.glowing = glowing;
        this.potionEffects = potionEffects;
        this.equipmentMaterials = equipmentMaterials;
        this.commands = commands;
        this.minY = minY;
        this.maxY = maxY;
        this.biomes = biomes;
        this.worlds = worlds;
        this.fishingXp = fishingXp;
    }

    public String getId() { return id; }
    public EntityType getType() { return type; }
    public int getWeight() { return weight; }
    public boolean isCustom() { return custom; }
    public String getDisplayName() { return displayName; }
    public boolean isGlowing() { return glowing; }
    public List<PotionEffect> getPotionEffects() { return potionEffects; }
    public Map<EquipmentSlotSimple, String> getEquipmentMaterials() { return equipmentMaterials; }
    public List<String> getCommands() { return commands; }
    public Integer getMinY() { return minY; }
    public Integer getMaxY() { return maxY; }
    public Set<String> getBiomes() { return biomes; }
    public Set<String> getWorlds() { return worlds; }
    public double getFishingXp() { return fishingXp; }

    public enum EquipmentSlotSimple { HAND, OFF_HAND, HEAD, CHEST, LEGS, FEET }
}
