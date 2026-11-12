package io.github.jadegg2568;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EpicSkyWars extends JavaPlugin {
    private GameService gameService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameService = new GameService(this);

        PluginCommand command = getCommand("epicsw");
        if (command == null) {
            throw new IllegalStateException("Command 'epicsw' is missing from plugin.yml");
        }
        EpicSkyWarsCommand executor = new EpicSkyWarsCommand(this, gameService);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GameService getGameService() {
        return gameService;
    }
}
