package io.github.jadegg2568.task;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameBoard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public final class PreStartTask implements IGameTask {
    private final Plugin plugin;
    private final Game game;
    private BukkitTask task;
    private int time;

    public PreStartTask(Plugin plugin, Game game) {
        this.plugin = plugin;
        this.game = game;
        this.time = 10;
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (GameBoard board : game.getPlayerScoreboards().values()) {
                    board.update(
                            "",
                            "Игроков: " + ChatColor.YELLOW +
                                    game.getPlayers().size() + "/" + game.getMax(),
                            "",
                            "Старт через " + ChatColor.YELLOW + time + ChatColor.WHITE + " сек",
                            "",
                            ChatColor.YELLOW + "play.jadegg.ru"
                    );
                }

                if (time <= 0) {
                    cancel();
                    game.end();
                    return;
                }
                if (time <= 5 || time == 10) {
                    game.broadcast(Component.text("Старт через " + time + " сек", TextColor.color(0x42ffe6)),
                            Sound.BLOCK_NOTE_BLOCK_HAT);
                }
                if (time == 0) {
                    game.beginPlaying();
                    PreStartTask.this.cancel();
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
