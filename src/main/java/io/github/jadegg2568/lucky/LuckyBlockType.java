package io.github.jadegg2568.lucky;

import org.bukkit.Material;

public enum LuckyBlockType {

    YELLOW(Material.YELLOW_STAINED_GLASS, LuckyDropRegistry.YELLOW),
    RED(Material.RED_STAINED_GLASS, LuckyDropRegistry.RED),
    BLUE(Material.BLUE_STAINED_GLASS, LuckyDropRegistry.BLUE),
    GREEN(Material.GREEN_STAINED_GLASS, LuckyDropRegistry.GREEN);

    private final Material material;
    private final LuckyDropRegistry drops;

    LuckyBlockType(Material material, LuckyDropRegistry drops) {
        this.material = material;
        this.drops = drops;
    }

    public Material getMaterial() {
        return material;
    }

    public LuckyDropRegistry getDrops() {
        return drops;
    }
}