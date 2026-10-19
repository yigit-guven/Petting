package net.yigitguven.petting.data;

public enum CombatMode {
    DEFENSIVE,
    AGGRESSIVE,
    PASSIVE,
    WANDER,
    GUARD;

    public CombatMode next() {
        CombatMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static CombatMode byName(String name) {
        if (name != null) {
            for (CombatMode mode : values()) {
                if (mode.name().equalsIgnoreCase(name)) {
                    return mode;
                }
            }
        }
        return DEFENSIVE;
    }
}
