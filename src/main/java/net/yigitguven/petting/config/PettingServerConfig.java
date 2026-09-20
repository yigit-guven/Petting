package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH_PERCENTAGE;
    public static final ModConfigSpec.BooleanValue HOSTILE_MOBS_ONLY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("taming");

        MAX_HEALTH_PERCENTAGE = builder
                .comment("The maximum health percentage a mob must be at or below to be tamed (0.0 to 100.0).")
                .translation("petting.configuration.taming.maxHealthPercentage")
                .defineInRange("maxHealthPercentage", 30.0, 0.0, 100.0);

        HOSTILE_MOBS_ONLY = builder
                .comment("Whether the health percentage requirement applies only to hostile mobs or to all mobs.",
                        "If true, only hostile mobs need to be weakened to the health threshold to be tamed.",
                        "If false, all mobs (including passive and neutral mobs) must be weakened to the health threshold.")
                .translation("petting.configuration.taming.hostileMobsOnly")
                .define("hostileMobsOnly", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static double getMaxHealthPercentage() {
        return MAX_HEALTH_PERCENTAGE.get();
    }

    public static double getMaxHealthRatio() {
        return MAX_HEALTH_PERCENTAGE.get() / 100.0;
    }

    public static boolean isHostileMobsOnly() {
        return HOSTILE_MOBS_ONLY.get();
    }
}
