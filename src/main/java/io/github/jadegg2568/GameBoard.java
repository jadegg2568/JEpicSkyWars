package io.github.jadegg2568;

import fr.mrmicky.fastboard.FastBoard;
import io.github.jadegg2568.Game;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class GameBoard {

    private final FastBoard board;
    private final Game game;
    private int previousTime = 0;

    public GameBoard(Player player, Game game) {
        this.board = new FastBoard(player);
        this.game = game;

        update();
    }

    public void update(String... lines) {
        int time = game.getTime() == -1 ? previousTime : game.getTime();
        previousTime = time;


        board.updateTitle(
                ChatColor.AQUA.toString() + ChatColor.BOLD + "EPIC SKYWARS"
        );

        board.updateLines(lines);
    }

    public void delete() {
        board.delete();
    }
}