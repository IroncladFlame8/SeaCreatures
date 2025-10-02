package me.shreyjain.seaCreatures.listener;

import me.shreyjain.seaCreatures.creature.SeaCreatureManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class SeaCreatureKillListener implements Listener {

    private final SeaCreatureManager manager;

    public SeaCreatureKillListener(SeaCreatureManager manager) {
        this.manager = manager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) return;
        manager.handleDeath(entity, killer);
    }
}
