package me.shreyjain.seaCreatures;

import me.shreyjain.seaCreatures.command.SeaCreaturesCommand;
import me.shreyjain.seaCreatures.creature.SeaCreatureManager;
import me.shreyjain.seaCreatures.listener.FishingListener;
import me.shreyjain.seaCreatures.listener.SeaCreatureKillListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SeaCreatures extends JavaPlugin {

    private SeaCreatureManager creatureManager;

    @Override
    public void onEnable() {
        // Save default config if not present
        saveDefaultConfig();
        this.creatureManager = new SeaCreatureManager(this);
        this.creatureManager.reload();

        // Register events
        Bukkit.getPluginManager().registerEvents(new FishingListener(this, creatureManager), this);
        Bukkit.getPluginManager().registerEvents(new SeaCreatureKillListener(creatureManager), this);

        // Register command with single instance
        SeaCreaturesCommand cmd = new SeaCreaturesCommand(this, creatureManager);
        if (getCommand("seacreatures") != null) {
            getCommand("seacreatures").setExecutor(cmd);
            getCommand("seacreatures").setTabCompleter(cmd);
        } else {
            getLogger().severe("Command 'seacreatures' missing from plugin.yml");
        }

        getLogger().info("SeaCreatures enabled. Loaded " + creatureManager.getDefinitions().size() + " creature definitions.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SeaCreatures disabled.");
    }

    public SeaCreatureManager getCreatureManager() { return creatureManager; }
}
