package me.shreyjain.seaCreatures.listener;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;
import me.shreyjain.seaCreatures.SeaCreatures;
import me.shreyjain.seaCreatures.creature.SeaCreatureDefinition;
import me.shreyjain.seaCreatures.creature.SeaCreatureManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class FishingListener implements Listener {

    private final SeaCreatures plugin;
    private final SeaCreatureManager manager;

    public FishingListener(SeaCreatures plugin, SeaCreatureManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        Player player = event.getPlayer();
        AuraSkillsApi skillsApi = AuraSkillsApi.get();
        SkillsUser skillsUser = skillsApi.getUser(player.getUniqueId());
        int userFishingLevel = skillsUser.getSkillLevel(Skills.FISHING);

        int luckLevel = 0;
        ItemStack rod = player.getInventory().getItemInMainHand();
        if (rod.getType() == Material.FISHING_ROD) {
            luckLevel = rod.getEnchantmentLevel(Enchantment.LUCK_OF_THE_SEA);
        }
        double chance = manager.computeChancePercent(luckLevel);
        chance += manager.getAdditionalFishingLuckChance(player);
        if (chance < 0) chance = 0; else if (chance > 100) chance = 100;
        if (ThreadLocalRandom.current().nextDouble(100.0) > chance) return;

        Location spawnLoc = event.getHook().getLocation().clone();
        SeaCreatureDefinition def = manager.pickRandom(spawnLoc, userFishingLevel);
        if (def == null) return;

        Entity caught = event.getCaught();
        if (caught != null) caught.remove();

        LivingEntity spawned = manager.spawn(spawnLoc, def, player);
        if (spawned != null) {
            // Message
            String raw = def.getDisplayName() != null ? def.getDisplayName() : def.getType().name();
            String plain = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', raw));
            String msg = plugin.getConfig().getString("messages.catch", "&bYou fished up a &e%creature%&b!")
                    .replace("%creature%", plain);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

            // Fling / pull the spawned creature toward the player to simulate reeling it in
            try {
                Location playerMid = player.getLocation().add(0, 0.5, 0);
                Vector toPlayer = playerMid.toVector().subtract(spawned.getLocation().toVector());
                double distance = toPlayer.length();
                if (distance > 0.0001) {
                    toPlayer.normalize();
                    // Scale velocity: closer mobs get a gentle nudge, farther get a bit stronger pull; clamp max.
                    double speed = Math.min(1.2, 0.35 + distance * 0.15); // tunable
                    Vector velocity = toPlayer.multiply(speed);
                    // Add upward arc so it "pops" out of water.
                    velocity.setY(Math.min(0.9, Math.max(0.35, distance * 0.10)));
                    spawned.setVelocity(velocity);
                }
                // Optional: aggro if it's a hostile mob so it moves toward the player after landing.
                if (spawned instanceof Mob mob) {
                    mob.setTarget(player);
                }
            } catch (Throwable ignored) { }
        }
    }
}
