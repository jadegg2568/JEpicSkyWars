package io.github.jadegg2568;

import org.bukkit.plugin.java.JavaPlugin;

public final class EpicSkyWars extends JavaPlugin {
    private GameService gameService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gameService = new GameService(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public GameService getGameService() {
        return gameService;
    }
}
