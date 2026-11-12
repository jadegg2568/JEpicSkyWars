package io.github.jadegg2568;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.ThreadLocalRandom;

public class Util {

    public static void spawnRandomFirework(Location pos1, Location pos2) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());

        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        double x = random.nextDouble(minX, maxX + 1);
        double z = random.nextDouble(minZ, maxZ + 1);

        Location spawn = new Location(
                pos1.getWorld(),
                x,
                Math.max(pos1.getY(), pos2.getY()),
                z
        );

        if (spawn.getWorld() == null) return;

        Firework firework = spawn.getWorld().spawn(
                spawn.clone(),
                Firework.class
        );

        FireworkMeta meta = firework.getFireworkMeta();

        Color color = Color.fromRGB(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );

        Color fade = Color.fromRGB(
                random.nextInt(256),
                random.nextInt(256),
                random.nextInt(256)
        );

        FireworkEffect.Type[] types = FireworkEffect.Type.values();

        FireworkEffect effect = FireworkEffect.builder()
                .with(types[random.nextInt(types.length)])
                .withColor(color)
                .withFade(fade)
                .trail(random.nextBoolean())
                .flicker(random.nextBoolean())
                .build();

        meta.addEffect(effect);
        meta.setPower(1);

        firework.setFireworkMeta(meta);
    }
}
