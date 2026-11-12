package io.github.jadegg2568.task;

import io.github.jadegg2568.Game;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class WaitingTask implements IGameTask {
    private final Plugin plugin;
    private final Game game;
    private BukkitTask task;
    private int time;
    private boolean starting = false;

    public WaitingTask(Plugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.time = -1;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (game.getPlayers().size() >= game.getMin()) {
                    if (!starting) {
                        game.broadcast(Component.text("Стартуем...", TextColor.color(0x42ffe6)),
                                Sound.ENTITY_VILLAGER_NO);
                        time = game.getWaitingTime();
                        starting = true;
                    }

                    if (time <= 0) {
                        game.preStart();
                        WaitingTask.this.cancel();
                        return;
                    }
                    if (time <= 5 || time % 10 == 0) {
                        game.broadcast(Component.text("Игра начнётся через " + time + " сек.", TextColor.color(0x42ffe6)),
                                Sound.BLOCK_NOTE_BLOCK_HAT);
                    }
                    time--;
                } else {
                    if (starting) {
                        game.broadcast(Component.text("Недостаточно игроков!", TextColor.color(0x42ffe6)),
                                Sound.ENTITY_VILLAGER_NO);
                        time = -1;
                        starting = false;
                    }
                }
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
