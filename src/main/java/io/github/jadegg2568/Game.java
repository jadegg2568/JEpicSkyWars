package io.github.jadegg2568;

import com.destroystokyo.paper.Title;
import io.github.jadegg2568.task.EndTask;
import io.github.jadegg2568.task.IGameTask;
import io.github.jadegg2568.task.PlayingTask;
import io.github.jadegg2568.task.WaitingTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Game {
    private final Plugin plugin;
    // config
    private final World w;
    private final String name;
    private final int min;
    private final int max;
    private final int waitingTime;
    private final int gameTime;
    private final Map<SpawnType, Location> spawns;

    // game logic
    private boolean loaded;
    private boolean inGame = false;
    private IGameTask activeTask;

    // players
    private final HashSet<Player> players = new HashSet<>();
    // not in game logic
    private final HashSet<Player> spectators = new HashSet<>();

    public Game(Plugin plugin, World w, String name, int min, int max, int waitingTime, int gameTime,
                Map<SpawnType, Location> spawns) {
        this.plugin = plugin;
        this.w = w;
        this.name = name;
        this.min = min;
        this.max = max;
        this.waitingTime = waitingTime;
        this.gameTime = gameTime;
        EnumMap<SpawnType, Location> spawnCopy = new EnumMap<>(SpawnType.class);
        spawnCopy.putAll(spawns);
        this.spawns = Collections.unmodifiableMap(spawnCopy);
    }

    public void load() {
        ArrayList<SpawnType> missingSpawns = new ArrayList<>();
        for (SpawnType type : SpawnType.values()) {
            Location spawn = spawns.get(type);
            if (spawn == null || !w.equals(spawn.getWorld())
                    || !Double.isFinite(spawn.getX())
                    || !Double.isFinite(spawn.getY())
                    || !Double.isFinite(spawn.getZ())) {
                missingSpawns.add(type);
            }
        }
        if (!missingSpawns.isEmpty()) {
            throw new IllegalStateException("Cannot load game; missing or invalid spawn locations: " + missingSpawns);
        }

        w.setGameRule(GameRules.ADVANCE_TIME, false);
        w.setGameRule(GameRules.ADVANCE_WEATHER, false);
        w.setGameRule(GameRules.SPAWN_MOBS, false);
        w.setTime(1000);
        w.setStorm(false);
        w.setThundering(false);
        inGame = false;
        loaded = true;

        activeTask = new WaitingTask(plugin, this);
        activeTask.start();
    }

    public int getTime() {
        return activeTask != null ? activeTask.getTime() : 0;
    }

    public void beginPlaying() {
        if (!loaded || inGame || players.size() < min || players.size() > max) {
            return;
        }
        inGame = true;

        // get mechanics ready
        for (Player player : players) {
            resetPlayer(player);
        }
        broadcast(Component.text("Игра началась!", TextColor.color(0x757575)), Sound.ENTITY_PLAYER_LEVELUP);

        // start game task
        activeTask = new PlayingTask(plugin, this);
        activeTask.start();
    }

    public void end() {
        if (!inGame) {
            return;
        }
        // start end task
        activeTask = new EndTask(plugin, this);
        activeTask.start();
    }

    public void finish() {
        for (Player player : players) {
            resetPlayer(player);
            player.teleport(getSpawn(SpawnType.WAITING));
        }
        for (Player spectator : spectators) {
            resetPlayer(spectator);
            spectator.teleport(getSpawn(SpawnType.WAITING));
        }
        players.addAll(spectators);
        spectators.clear();
        inGame = false;

        for (Player player : new ArrayList<>(players)) {
            player.kick(Component.text("Игра завершена.\nСпасибо за игру!\n\nMade By: JadeGG_\nJava Bukkit Coding\nEpicSkyWars Plugin", TextColor.color(0x35AF15)));
        }
        activeTask = null;
    }

    public void join(Player p) {
        if (!inGame) {
            players.add(p);
            // broadcast
            TextComponent message = Component.text(p.getName(), TextColor.color(0xff9538))
                    .append(Component.text("зашёл. ", TextColor.color(0x42ffe6)));
            broadcast(message,
                    Sound.ENTITY_VILLAGER_NO);
        } else {
            // add to spectators
            resetPlayer(p);
            switchSpectator(p, true);
        }
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public boolean isFull() {
        return !inGame && players.size() >= max;
    }

    public void leave(Player p) {
        boolean wasPlayer = players.remove(p);
        boolean wasSpectator = spectators.remove(p);
        if (!wasPlayer && !wasSpectator) {
            return;
        }

        if (wasPlayer) {
            Component message = Component.text(p.getName(), TextColor.color(0xff9538))
                    .append(Component.text(
                            inGame ? " отключился" : " вышел",
                            TextColor.color(inGame ? 0x757575 : 0x42ffe6)
                    ));
            broadcast(message, Sound.ENTITY_VILLAGER_NO);
            endIfAvailable();
        }

        if (p.isOnline()) {
            resetPlayer(p);
        }
    }

    public void endIfAvailable() {
        if (!inGame || players.size() > 1) {
            return;
        }
        end();
    }

    public void onDie(Player p, Player killer, boolean byVoid) {
        // broadcast
        if (killer != null) {
            broadcast(Component.text(p.getName() + " был убит " + killer.getName(), TextColor.color(0x757575)), Sound.ENTITY_PLAYER_LEVELUP);
        } else {
            broadcast(Component.text(p.getName() + (byVoid ? " выпал из мира" : " умер"), TextColor.color(0x757575)), Sound.ENTITY_PLAYER_LEVELUP);
        }

        // send him broadcast
        p.sendMessage(Component.text("Вы умерли! Удачи в следующей игре", TextColor.color(0x757575)));
        p.sendTitle(Title.builder().
                title(ChatColor.RED + "Вы проебали!")
                .subtitle("В прочем нам похуй, меняйте на себя сэнсэй.")
                .build());

        switchSpectator(p, true);
        endIfAvailable();
    }

    // make spectator
    public void switchSpectator(Player p, boolean b) {
        players.remove(p);
        spectators.add(p);
        p.setGameMode(b ? GameMode.SPECTATOR : GameMode.SURVIVAL);
    }

    // resets all player stats
    public void resetPlayer(Player p) {
        p.closeInventory();
        p.getInventory().clear();
        p.getInventory().setArmorContents(new ItemStack[4]);
        p.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        p.setHealth(Objects.requireNonNull(
                p.getAttribute(Attribute.MAX_HEALTH),
                "Player is missing the MAX_HEALTH attribute"
        ).getValue());
        p.setAbsorptionAmount(0);
        p.setFoodLevel(20);
        p.setSaturation(5);
        p.setExhaustion(0);
        p.setExp(0);
        p.setLevel(0);
        p.setTotalExperience(0);
        for (var effect : new HashSet<>(p.getActivePotionEffects())) {
            p.removePotionEffect(effect.getType());
        }
        p.setFireTicks(0);
        p.setFreezeTicks(0);
        p.setFallDistance(0);
        p.setFlying(false);
        p.setAllowFlight(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.setGlowing(false);
        p.resetCooldown();
    }

    public void broadcast(Component message, Sound sound) {
        Set<Player> recipients = new HashSet<>(players);
        recipients.addAll(spectators);
        for (Player player : recipients) {
            player.sendMessage(message);
            if (sound != null) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }

    public void broadcast(Component message) {
        broadcast(message, null);
    }

    public Location getSpawn(SpawnType type) {
        return spawns.get(type);
    }

    public boolean isLoaded() {
        return loaded;
    }

    public World getW() {
        return w;
    }

    public String getName() {
        return name;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public int getGameTime() {
        return gameTime;
    }
}
