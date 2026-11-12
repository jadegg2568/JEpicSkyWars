package io.github.jadegg2568.task;

import io.github.jadegg2568.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class PlayingTask implements IGameTask {
    private final Plugin plugin;
    private final Game game;
    private BukkitTask task;
    private int time;

    public PlayingTask(Plugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.time = game.getGameTime();
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (time <= 0) {
                    cancel();
                    game.end();
                    return;
                }
                if (time <= 5 || time % 60 == 0 && time != game.getGameTime()) {
                    game.broadcast(Component.text("До конца игры: " + time + " сек.", TextColor.color(0x42ffe6)),
                            Sound.BLOCK_NOTE_BLOCK_HAT);
                }
                time--;
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
        return time;
    }
}
