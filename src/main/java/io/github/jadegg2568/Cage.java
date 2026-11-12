package io.github.jadegg2568;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Cage {

    private final Location spawn;
    private final List<BlockState> originalBlocks = new ArrayList<>();

    private Player p;
    private boolean built = false;

    public Cage(Location spawn) {
        this.spawn = spawn.clone();
    }

    public void addPlayer(Player player) {
        this.p = p;
        player.teleport(spawn);
    }

    public boolean hasPlayer() {
        return p != null;
    }

    public Player getPlayer() {
        return p;
    }

    public void removePlayer() {
        p = null;
    }

    public void build() {
        if (built || !hasPlayer()) return;

        Player player = getPlayer();
        if (player == null) return;

        Material material = player.hasPermission("jadegg.cage.red")
                ? Material.RED_STAINED_GLASS
                : Material.GLASS;

        int[][] offsets = {
                { 0, -1,  0 },
                { 0,  2,  0 },
                {-1,  0,  0 },
                { 1,  0,  0 },
                { 0,  0, -1 },
                { 0,  0,  1 },
                {-1,  1,  0 },
                { 1,  1,  0 },
                { 0,  1, -1 },
                { 0,  1,  1 }
        };

        for (int[] offset : offsets) {
            Block block = spawn.getWorld().getBlockAt(
                    spawn.getBlockX() + offset[0],
                    spawn.getBlockY() + offset[1],
                    spawn.getBlockZ() + offset[2]
            );

            originalBlocks.add(block.getState());
            block.setType(material, false);
        }

        built = true;
    }

    public void delete() {
        if (!built) return;

        for (BlockState state : originalBlocks) {
            state.update(true, false);
        }

        originalBlocks.clear();
        built = false;
    }

    public Location getSpawn() {
        return spawn.clone();
    }
}