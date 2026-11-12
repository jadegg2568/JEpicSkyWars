
package io.github.jadegg2568.listener;

import io.github.jadegg2568.Game;
import io.github.jadegg2568.GameService;
import io.github.jadegg2568.lucky.LuckyBlockType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collection;

public final class BlockListener implements Listener {
    private final GameService gameService;

    public BlockListener(GameService gameService) {
        this.gameService = gameService;
    }

    // BREAK
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);

        Game game = getGameByWorld(e.getBlock().getWorld());
        if (game == null || !game.isInGame()) return;

        Player player = e.getPlayer();
        if (!game.isPlaying(player)) return;

        Block block = e.getBlock();

        // save source state
        saveBlock(game, block);

        // check if the block is lucky block
        LuckyBlockType luckyType = Arrays.stream(LuckyBlockType.values())
                .filter(type -> type.getMaterial() == block.getType())
                .findFirst()
                .orElse(null);

        if (luckyType != null) {
            block.setType(Material.AIR, false);
            luckyType.getDrops().dropRandom(player); // drop random loot
            return;
        }

        // standard block breaking
        Collection<ItemStack> drops = block.getDrops(
                player.getInventory().getItemInMainHand(),
                player
        );

        int exp = e.getExpToDrop();
        Location location = block.getLocation().add(0.5, 0.5, 0.5);

        block.setType(Material.AIR, false);

        drops.forEach(item -> block.getWorld().dropItemNaturally(location, item));

        if (exp > 0) {
            block.getWorld().spawn(location, ExperienceOrb.class,
                    orb -> orb.setExperience(exp));
        }
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

        if (!game.isWithinArena(e.getBlock().getLocation())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Component.text("НВы не можете выходить за карту!", TextColor.color(0xFF0F00)));
            return;
        }

        if (e instanceof BlockMultiPlaceEvent multi) {
            if (multi.getReplacedBlockStates().stream()
                    .anyMatch(state -> !game.isWithinArena(state.getLocation()))) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Component.text("НВы не можете выходить за карту!", TextColor.color(0xFF0F00)));
                return;
            }
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