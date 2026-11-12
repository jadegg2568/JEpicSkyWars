package io.github.jadegg2568.task;

import io.github.jadegg2568.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class EndTask implements IGameTask {
    private final Plugin plugin;
    private final Game game;
    private BukkitTask task;

    public EndTask(Plugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        game.broadcast(Component.text("Игра завершена.", TextColor.color(0x42ffe6)), Sound.ENTITY_PLAYER_LEVELUP);
        task = new BukkitRunnable() {
            @Override
            public void run() {
                game.finish();
            }
        }.runTaskLater(plugin, 100L);
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
        return -1;
    }
}
