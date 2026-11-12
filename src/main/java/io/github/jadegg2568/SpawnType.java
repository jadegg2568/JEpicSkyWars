package io.github.jadegg2568;

import java.util.Arrays;
import java.util.List;

public enum SpawnType {
    WAITING,
    SPECTATOR,
    CAGE_1,
    CAGE_2,
    CAGE_3,
    CAGE_4,
    CAGE_5,
    CAGE_6,
    CAGE_7,
    CAGE_8,
    BORDER_POS_1,
    BORDER_POS_2;

    public static List<SpawnType> getCageTypes() {
        return Arrays.stream(values())
                .filter(type -> type.name().startsWith("CAGE_"))
                .toList();
    }
}
