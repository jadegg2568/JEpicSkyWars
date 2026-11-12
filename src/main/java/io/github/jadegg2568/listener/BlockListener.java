
package io.github.jadegg2568.listener;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameService;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.StructureGrowEvent;

public final class BlockListener implements Listener {
    private final GameService gameService;

    public BlockListener(GameService gameService) {
        this.gameService = gameService;
    }

    // BREAK
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame() || !game.isPlaying(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlock());
    }

    // PLACE + MULTI PLACE
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame() || !game.isPlaying(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        if (e instanceof BlockMultiPlaceEvent multi) {
            multi.getReplacedBlockStates().forEach(state -> saveBlock(game, state));
        } else {
            saveBlock(game, e.getBlockReplacedState());
        }
    }

    // EXPLOSIONS
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        e.blockList().forEach(block -> saveBlock(game, block));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        Game game = getGameByWorld(e.getLocation().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        e.blockList().forEach(block -> saveBlock(game, block));
    }

    // LIQUID FLOW
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLiquidFlow(BlockFromToEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getToBlock());
    }

    // BUCKETS
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Game game = getGameByWorld(e.getPlayer().getWorld());
        if (game == null) return;

        if (!game.isInGame() || !game.isPlaying(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlockClicked().getRelative(e.getBlockFace()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        Game game = getGameByWorld(e.getPlayer().getWorld());
        if (game == null) return;

        if (!game.isInGame() || !game.isPlaying(e.getPlayer())) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlockClicked());
    }

    // PLANTS / TREES
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockGrow(BlockGrowEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        saveBlock(game, e.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFertilize(BlockFertilizeEvent e) {
        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        e.getBlocks().forEach(state -> saveBlock(game, state.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e) {
        Game game = getGameByWorld(e.getWorld());
        if (game == null) return;

        if (!game.isInGame()) {
            e.setCancelled(true);
            return;
        }

        e.getBlocks().forEach(state -> saveBlock(game, state.getBlock()));
    }

    // UTILS
    private void saveBlock(Game game, Block block) {
        saveBlock(game, block.getState());
    }

    private void saveBlock(Game game, BlockState state) {
        boolean exists = game.getChanges().stream()
                .anyMatch(old -> old.getLocation().equals(state.getLocation()));

        if (!exists) {
            game.getChanges().add(state);
        }
    }

    private Game getGameByWorld(World world) {
        Game game = gameService.getGame();
        return game != null && game.isLoaded() && game.getW().equals(world) ? game : null;
    }
}