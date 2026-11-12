package io.github.jadegg2568;

import com.destroystokyo.paper.Title;
import fr.mrmicky.fastboard.FastBoard;
import io.github.jadegg2568.task.EndTask;
import io.github.jadegg2568.task.IGameTask;
import io.github.jadegg2568.task.PlayingTask;
import io.github.jadegg2568.task.WaitingTask;
import io.papermc.paper.world.damagesource.FallLocationType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;

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

    private final HashSet<Cage> cages = new HashSet<>();
    private final List<BlockState> changes = new ArrayList<>();
    // players
    private final HashSet<Player> players = new HashSet<>();
    private final HashSet<Player> spectators = new HashSet<>();
    private final Map<Player, GameBoard> playerScoreboards = new HashMap<>();

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

        for (SpawnType type : SpawnType.getCageTypes()) {
            cages.add(new Cage(spawns.get(type)));
        }
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

    public void reload() {
        players.addAll(spectators);
        spectators.clear();
        inGame = false;

        List<Player> players2 = new ArrayList<>(players);
        for (Player p : players2) {
            leave(p);
        }

        resetChanges();

        for (Player player : players2) {
            join(player);
        }

        activeTask = new WaitingTask(plugin, this);
        activeTask.start();
    }

    public void preStart() {
        inGame = true;

        for (Player player : players) {
            resetPlayer(player);
            // find free cage and teleport
            Cage freeCage = null;
            for (Cage cage : cages) {
                if (!cage.hasPlayer()) {
                    freeCage = cage;
                }
            }
            if (freeCage != null) {
                freeCage.addPlayer(player);
                freeCage.build();
            }
        }
    }

    public void beginPlaying() {
        if (!loaded || inGame || players.size() < min || players.size() > max) {
            return;
        }

        for (Cage cage : cages) {
            cage.removePlayer();
            cage.delete();
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

        if (players.size() == 1) {
            Player winner = players.iterator().next();

            broadcast(
                    Component.text("------------------------------\n", TextColor.color(0x757575))
                            .append(Component.text("ПОБЕДИТЕЛЬ: ", TextColor.color(0xffd700)))
                            .append(Component.text(winner.getName(), TextColor.color(0x42ffe6)))
                            .append(Component.text("\n------------------------------", TextColor.color(0x757575))),
                    Sound.UI_TOAST_CHALLENGE_COMPLETE
            );
        }

        // start end task
        activeTask = new EndTask(plugin, this);
        activeTask.start();
    }

    public void resetChanges() {
        Collections.reverse(changes);
        for (BlockState state : changes) {
            state.update(true, false);
        }
        changes.clear();
    }

    public void join(Player p) {
        if (players.contains(p) || spectators.contains(p)) {
            return;
        }
        if (!inGame) {
            players.add(p);
            playerScoreboards.put(p, new GameBoard(p, this));
            p.teleport(getSpawn(SpawnType.WAITING));
            // broadcast
            TextComponent message = Component.text(p.getName(), TextColor.color(0xff9538))
                    .append(Component.text("зашёл. ", TextColor.color(0x42ffe6)));
            broadcast(message,
                    Sound.ENTITY_VILLAGER_NO);
        } else {
            // add to spectators
            resetPlayer(p);
            switchSpectator(p, true);
            p.teleport(getSpawn(SpawnType.SPECTATOR));
        }
    }

    public void leave(Player p) {
        boolean wasPlayer = players.remove(p);
        boolean wasSpectator = spectators.remove(p);
        GameBoard board = playerScoreboards.get(p);
        if (board != null) {
            board.delete();
            playerScoreboards.remove(p);
        }
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
        if (b) {
            players.remove(p);
            spectators.add(p);
            p.teleport(getSpawn(SpawnType.SPECTATOR));
        } else {
            spectators.remove(p);
            players.add(p);
        }
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

    public boolean hasPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public boolean isInGame() {
        return inGame;
    }

    public boolean isPlaying(Player player) {
        return inGame && players.contains(player);
    }

    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    public boolean isFull() {
        return !inGame && players.size() >= max;
    }

    public Set<Player> getPlayers() { return players; }

    public Location getSpawn(SpawnType type) {
        return spawns.get(type);
    }

    public boolean isWithinArena(Location location) {
        Location pos1 = spawns.get(SpawnType.BORDER_POS_1);
        Location pos2 = spawns.get(SpawnType.BORDER_POS_2);
        if (location == null || pos1 == null || pos2 == null
                || !w.equals(location.getWorld())
                || !w.equals(pos1.getWorld())
                || !w.equals(pos2.getWorld())) {
            return false;
        }

        return isBetween(location.getBlockX(), pos1.getBlockX(), pos2.getBlockX())
                && isBetween(location.getBlockY(), pos1.getBlockY(), pos2.getBlockY())
                && isBetween(location.getBlockZ(), pos1.getBlockZ(), pos2.getBlockZ());
    }

    private boolean isBetween(int coordinate, int first, int second) {
        return coordinate >= Math.min(first, second) && coordinate <= Math.max(first, second);
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

    public List<BlockState> getChanges() {
        return changes;
    }

    public Set<Cage> getCages() {
        return cages;
    }

    public Map<Player, GameBoard> getPlayerScoreboards() {
        return playerScoreboards;
    }
}
