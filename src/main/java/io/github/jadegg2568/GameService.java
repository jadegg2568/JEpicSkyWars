package io.github.jadegg2568;

import org.bukkit.configuration.ConfigurationSection;

public final class GameService {
    private final EpicSkyWars plugin;
    private GameConfig game;

    public GameService(EpicSkyWars plugin) {
        this.plugin = plugin;
        loadGame();
    }

    public GameConfig getGame() {
        return game;
    }

    public void reload() {
        plugin.reloadConfig();
        loadGame();
    }

    private void loadGame() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("game");
        if (section == null) {
            throw new IllegalStateException("Invalid configuration at game: expected a section");
        }

        game = new GameConfig(
                getRequiredString(section, "world"),
                getRequiredString(section, "name"),
                getRequiredInt(section, "min"),
                getRequiredInt(section, "max"),
                getRequiredInt(section, "teamSize"),
                getRequiredInt(section, "teamCount")
        );
    }

    private String getRequiredString(ConfigurationSection section, String key) {
        String path = "game." + key;
        String value = section.getString(key);
        if (!section.isString(key) || value == null || value.isBlank()) {
            throw new IllegalStateException("Invalid configuration at " + path + ": expected a non-empty string");
        }
        return value;
    }

    private int getRequiredInt(ConfigurationSection section, String key) {
        String path = "game." + key;
        if (!section.isInt(key)) {
            throw new IllegalStateException("Invalid configuration at " + path + ": expected an integer");
        }
        return section.getInt(key);
    }
}
