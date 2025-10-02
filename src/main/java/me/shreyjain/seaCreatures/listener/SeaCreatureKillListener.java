package me.shreyjain.seaCreatures.listener;

import me.shreyjain.seaCreatures.creature.SeaCreatureManager;
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
        // Delegate full event so manager can process custom drops and XP (handles killer null internally)
        manager.handleDeath(event);
    }
}
