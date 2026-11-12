package io.github.jadegg2568;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class GameService {
    private final EpicSkyWars main;
    private Game game;

    public Game getGame() {
        return game;
    }

    public GameService(EpicSkyWars main) {
        this.main = main;
        try {
            loadGame();
        } catch (Exception e) {
            main.getLogger().warning("-----------------------\n" +
                    "Failed to start the game instance:\n\n" +
                    "" + e.getMessage() +
                    "\n\nso you should set all required locations using /epicsw setspawn.");
        }
    }

    private void loadGame() {
        ConfigurationSection section = main.getConfig().getConfigurationSection("game");
        if (section == null) {
            throw new IllegalStateException("Invalid configuration at game: expected a section");
        }

        String world = getRequiredString(section, "world");
        String name = getRequiredString(section, "name");
        int min = getRequiredInt(section, "min");
        int max = getRequiredInt(section, "max");
        int waitingTime = getRequiredInt(section, "waitingTime");
        int gameTime = getRequiredInt(section, "gameDuration");

        World w = Bukkit.createWorld(new WorldCreator(world));
        if (w == null) {
            throw new IllegalStateException("Unable to load game world: " + world);
        }

        Map<SpawnType, Location> spawns = loadSpawns(section);
        Game loadedGame = new Game(main, w, name,
                min, max,
                waitingTime, gameTime, spawns);
        game = loadedGame;
        try {
            game.load();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load game: " + e.getMessage());
        }
    }

    private Map<SpawnType, Location> loadSpawns(ConfigurationSection gameSection) {
        Map<SpawnType, Location> spawns = new EnumMap<>(SpawnType.class);
        ConfigurationSection locations = gameSection.getConfigurationSection("locations");
        if (locations == null) {
            return spawns;
        }

        for (SpawnType type : SpawnType.values()) {
            String key = type.name().toLowerCase(Locale.ROOT);
            Location spawn = locations.getLocation(key);
            if (spawn == null) {
                continue;
            }

            spawns.put(type, spawn);
        }
        return spawns;
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
