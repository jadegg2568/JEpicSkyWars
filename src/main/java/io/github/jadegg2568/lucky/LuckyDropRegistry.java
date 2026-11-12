package io.github.jadegg2568.lucky;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public final class LuckyDropRegistry {
    public static final LuckyDropRegistry YELLOW;
    public static final LuckyDropRegistry RED;
    public static final LuckyDropRegistry BLUE;
    public static final LuckyDropRegistry GREEN;

    static {
        YELLOW = new LuckyDropRegistry()
                .register(new LuckyDrop("food", 20, p ->
                        giveItems(p, "Food!", new ItemStack(Material.COOKED_BEEF, 8))))
                .register(new LuckyDrop("archer", 15, p ->
                        giveItems(p, "Archer kit!", new ItemStack(Material.BOW), new ItemStack(Material.ARROW, 16))))
                .register(new LuckyDrop("speed", 15, p ->
                        giveEffect(p, "Speed!", PotionEffectType.SPEED, 15, 1)))
                .register(new LuckyDrop("chicken", 10, p ->
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.CHICKEN)))
                .register(new LuckyDrop("blindness", 5, p ->
                        giveEffect(p, "Bad luck!", PotionEffectType.BLINDNESS, 5, 0)));

        RED = new LuckyDropRegistry()
                .register(new LuckyDrop("iron_kit", 20, p ->
                        giveItems(p, "Iron kit!", new ItemStack(Material.IRON_SWORD), new ItemStack(Material.IRON_CHESTPLATE))))
                .register(new LuckyDrop("golden_apple", 15, p ->
                        giveItems(p, "Golden apples!", new ItemStack(Material.GOLDEN_APPLE, 2))))
                .register(new LuckyDrop("golem", 10, p ->
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.IRON_GOLEM)))
                .register(new LuckyDrop("weakness", 5, p ->
                        giveEffect(p, "Weakness!", PotionEffectType.WEAKNESS, 10, 0)));

        BLUE = new LuckyDropRegistry()
                .register(new LuckyDrop("diamond_sword", 20, p ->
                        giveItems(p, "Diamond sword!", new ItemStack(Material.DIAMOND_SWORD))))
                .register(new LuckyDrop("ender_pearls", 15, p ->
                        giveItems(p, "Ender pearls!", new ItemStack(Material.ENDER_PEARL, 4))))
                .register(new LuckyDrop("wolf", 10, p ->
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF)))
                .register(new LuckyDrop("regeneration", 15, p ->
                        giveEffect(p, "Regeneration!", PotionEffectType.REGENERATION, 15, 1)))
                .register(new LuckyDrop("creeper", 5, p ->
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.CREEPER)));

        GREEN = new LuckyDropRegistry()
                .register(new LuckyDrop("netherite_sword", 15, p ->
                        giveItems(p, "Netherite sword!", new ItemStack(Material.NETHERITE_SWORD))))
                .register(new LuckyDrop("totem", 15, p ->
                        giveItems(p, "Totem!", new ItemStack(Material.TOTEM_OF_UNDYING))))
                .register(new LuckyDrop("god_speed", 10, p -> {
                    giveEffect(p, "God speed!", PotionEffectType.SPEED, 30, 1);
                    giveEffect(p, "Jump boost!", PotionEffectType.JUMP_BOOST, 30, 1);
                }))
                .register(new LuckyDrop("emeralds", 10, p ->
                        giveItems(p, "Emerald rain!", new ItemStack(Material.EMERALD, 32))))
                .register(new LuckyDrop("enderman", 5, p ->
                        p.getWorld().spawnEntity(p.getLocation(), EntityType.ENDERMAN)));
    }

    private final List<LuckyDrop> drops = new ArrayList<>();
    private long totalWeight;

    public LuckyDropRegistry register(LuckyDrop drop) {
        Objects.requireNonNull(drop, "drop");
        if (drops.stream().anyMatch(existing -> existing.getId().equals(drop.getId()))) {
            throw new IllegalArgumentException("Lucky drop already registered: " + drop.getId());
        }
        totalWeight = Math.addExact(totalWeight, drop.getWeight());
        drops.add(drop);
        return this;
    }

    public LuckyDrop dropRandom(Player player) {
        Objects.requireNonNull(player, "player");
        if (drops.isEmpty()) {
            throw new IllegalStateException("Cannot choose a lucky drop from an empty registry");
        }

        long selection = ThreadLocalRandom.current().nextLong(totalWeight);
        for (LuckyDrop drop : drops) {
            selection -= drop.getWeight();
            if (selection < 0) {
                drop.execute(player);
                return drop;
            }
        }
        throw new IllegalStateException("Lucky drop selection failed");
    }

    public List<LuckyDrop> getDrops() {
        return List.copyOf(drops);
    }

    public static LuckyDropRegistry createDefault() {
        return new LuckyDropRegistry()
                .register(new LuckyDrop("starter_food", 24, player ->
                        giveItems(player, "A snack for the journey!",
                                new ItemStack(Material.COOKED_BEEF, 8),
                                new ItemStack(Material.BAKED_POTATO, 8))))
                .register(new LuckyDrop("basic_blocks", 20, player ->
                        giveItems(player, "A few building blocks!",
                                new ItemStack(Material.OAK_PLANKS, 32),
                                new ItemStack(Material.COBBLESTONE, 16))))
                .register(new LuckyDrop("archer_kit", 14, player ->
                        giveItems(player, "An archer kit!",
                                new ItemStack(Material.BOW),
                                new ItemStack(Material.ARROW, 16))))
                .register(new LuckyDrop("iron_kit", 12, player ->
                        giveItems(player, "Some basic gear!",
                                new ItemStack(Material.IRON_SWORD),
                                new ItemStack(Material.IRON_CHESTPLATE))))
                .register(new LuckyDrop("golden_apple", 8, player ->
                        giveItems(player, "A golden apple!",
                                new ItemStack(Material.GOLDEN_APPLE))))
                .register(new LuckyDrop("ender_pearls", 6, player ->
                        giveItems(player, "A quick escape!",
                                new ItemStack(Material.ENDER_PEARL, 2))))
                .register(new LuckyDrop("speed_boost", 12, player ->
                        giveEffect(player, "Zoom time!", PotionEffectType.SPEED, 30, 1)))
                .register(new LuckyDrop("regeneration", 8, player ->
                        giveEffect(player, "A little regeneration.", PotionEffectType.REGENERATION, 10, 1)))
                .register(new LuckyDrop("jump_boost", 8, player ->
                        giveEffect(player, "Reach new heights!", PotionEffectType.JUMP_BOOST, 30, 1)))
                .register(new LuckyDrop("slow_falling", 6, player ->
                        giveEffect(player, "Float safely for a while!", PotionEffectType.SLOW_FALLING, 15, 0)))
                .register(new LuckyDrop("wool_shelter", 5, player -> {
                    buildShelter(player, Material.WHITE_WOOL, Material.OAK_PLANKS);
                    player.sendMessage(Component.text("A wool shelter appeared around you!"));
                }))
                .register(new LuckyDrop("glass_shelter", 3, player -> {
                    buildShelter(player, Material.GLASS, Material.GLOWSTONE);
                    player.sendMessage(Component.text("A little glass shelter appeared around you!"));
                }))
                .register(new LuckyDrop("stone_brick_shelter", 2, player -> {
                    buildShelter(player, Material.STONE_BRICKS, Material.BRICKS);
                    player.sendMessage(Component.text("A sturdy shelter appeared around you!"));
                }));
    }

    private static void giveItems(Player player, String message, ItemStack... items) {
        for (ItemStack item : items) {
            player.getInventory().addItem(item).values()
                    .forEach(overflow -> player.getWorld().dropItemNaturally(player.getLocation(), overflow));
        }
        player.sendMessage(Component.text(message));
    }

    private static void giveEffect(Player player, String message, PotionEffectType type, int seconds, int amplifier) {
        player.addPotionEffect(new PotionEffect(type, seconds * 20, amplifier));
        player.sendMessage(Component.text(message));
    }

    private static void buildShelter(Player player, Material wall, Material roof) {
        Location center = player.getLocation().getBlock().getLocation();
        int baseY = center.getBlockY();

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean perimeter = Math.abs(x) == 2 || Math.abs(z) == 2;
                if (perimeter && !(x == 0 && z == -2)) {
                    placeIfAir(center.getWorld().getBlockAt(
                            center.getBlockX() + x, baseY, center.getBlockZ() + z), wall);
                    placeIfAir(center.getWorld().getBlockAt(
                            center.getBlockX() + x, baseY + 1, center.getBlockZ() + z), wall);
                }
                placeIfAir(center.getWorld().getBlockAt(
                        center.getBlockX() + x, baseY + 3, center.getBlockZ() + z), roof);
            }
        }
    }

    private static void placeIfAir(Block block, Material material) {
        if (block.getType().isAir()) {
            block.setType(material);
        }
    }
}
