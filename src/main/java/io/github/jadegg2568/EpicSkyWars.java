package io.github.jadegg2568;

import io.github.jadegg2568.lucky.LuckyDropRegistry;
import io.github.jadegg2568.listener.BlockListener;
import io.github.jadegg2568.listener.PlayerListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EpicSkyWars extends JavaPlugin {
    private GameService gameService;
    private LuckyDropRegistry luckyDropRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameService = new GameService(this);
        luckyDropRegistry = LuckyDropRegistry.createDefault();

        PluginCommand command = getCommand("epicsw");
        if (command == null) {
            throw new IllegalStateException("Command 'epicsw' is missing from plugin.yml");
        }
        EpicSkyWarsCommand executor = new EpicSkyWarsCommand(this, gameService);
        command.setExecutor(executor);
        command.setTabCompleter(executor);

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new BlockListener(gameService), this);
        pluginManager.registerEvents(new PlayerListener(gameService), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GameService getGameService() {
        return gameService;
    }

    public LuckyDropRegistry getLuckyDropRegistry() {
        return luckyDropRegistry;
    }
}
