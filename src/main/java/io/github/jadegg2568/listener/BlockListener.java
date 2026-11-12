package io.github.jadegg2568.listener;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameService;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public final class BlockListener implements Listener {
    private final GameService gameService;

    public BlockListener(GameService gameService) {
        this.gameService = gameService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Game game = getGameByPlayer(event.getPlayer());
        if (game != null && !game.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Game game = getGameByPlayer(event.getPlayer());
        if (game != null && !game.isPlaying(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isGameWorld(event.getBlock().getWorld())) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {

        if (isGameWorld(event.getLocation().getWorld())) {
            event.blockList().clear();
        }
    }

    private Game getGameByPlayer(Player p) {
        Game game = gameService.getGame();
        return game != null && game.isLoaded() && game.hasPlayer(p) ? game : null;
    }

    private boolean isGameWorld(World w) {
        Game game = gameService.getGame();
        return game != null && game.isLoaded() && game.getW() == w;
    }
}
