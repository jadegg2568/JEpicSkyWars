package io.github.jadegg2568;

public record GameConfig(
        String world,
        String name,
        int min,
        int max,
        int teamSize,
        int teamCount
) {
}
