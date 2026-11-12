package io.github.jadegg2568.task;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.SpawnType;
import io.github.jadegg2568.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ThreadLocalRandom;

public final class EndTask implements IGameTask {
    private final Plugin plugin;
    private final Game game;
    private BukkitTask task;

    private int seconds = 0;

    public EndTask(Plugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public void start() {
        if (isRunning()) return;

        seconds = 0;

        game.broadcast(
                Component.text("Игра завершена.", TextColor.color(0x42ffe6)),
                Sound.ENTITY_PLAYER_LEVELUP
        );

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (seconds >= 10) {
                    cancel();
                    game.reload();
                    return;
                }

                for (int i = 0; i < 10; i++) {
                    Util.spawnRandomFirework(game.getSpawn(SpawnType.BORDER_POS_1), game.getSpawn(SpawnType.BORDER_POS_2));
                }

                seconds++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

        task = null;
    }

    @Override
    public boolean isRunning() {
        return task != null && !task.isCancelled();
    }

    @Override
    public int getTime() {
        return 10 - seconds;
    }
}