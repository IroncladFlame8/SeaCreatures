package me.shreyjain.seaCreatures.command;

import me.shreyjain.seaCreatures.SeaCreatures;
import me.shreyjain.seaCreatures.creature.SeaCreatureManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SeaCreaturesCommand implements CommandExecutor, TabCompleter {

    private final SeaCreatures plugin;
    private final SeaCreatureManager manager;

    public SeaCreaturesCommand(SeaCreatures plugin, SeaCreatureManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "SeaCreatures " + ChatColor.GRAY + "- Loaded definitions: " + manager.getDefinitions().size());
            sender.sendMessage(ChatColor.YELLOW + "/" + label + " reload" + ChatColor.GRAY + " - reload config");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("seacreatures.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission.");
                return true;
            }
            long start = System.currentTimeMillis();
            manager.reload();
            long took = System.currentTimeMillis() - start;
            sender.sendMessage(ChatColor.GREEN + "SeaCreatures reloaded (" + manager.getDefinitions().size() + " defs, " + took + "ms).");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Unknown subcommand. Try /" + label + " reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("seacreatures.reload")) out.add("reload");
        }
        return out;
    }
}

