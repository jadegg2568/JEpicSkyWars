package io.github.jadegg2568.lucky;

import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.function.Consumer;

public final class LuckyDrop {
    private final String id;
    private final int weight;
    private final Consumer<Player> action;

    public LuckyDrop(String id, int weight, Consumer<Player> action) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Lucky drop id cannot be blank");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Lucky drop weight must be positive");
        }
        this.id = id;
        this.weight = weight;
        this.action = Objects.requireNonNull(action, "action");
    }

    public String getId() {
        return id;
    }

    public int getWeight() {
        return weight;
    }

    public void execute(Player player) {
        action.accept(Objects.requireNonNull(player, "player"));
    }
}
