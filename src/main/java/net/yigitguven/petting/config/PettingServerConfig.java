package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH_PERCENTAGE;
    public static final ModConfigSpec.BooleanValue HOSTILE_MOBS_ONLY;
    public static final ModConfigSpec.DoubleValue UNSUCCESSFUL_TAMING_CHANCE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("taming");

        MAX_HEALTH_PERCENTAGE = builder
                .translation("petting.configuration.taming.maxHealthPercentage")
                .defineInRange("maxHealthPercentage", 30.0, 0.0, 100.0);

        HOSTILE_MOBS_ONLY = builder
                .translation("petting.configuration.taming.hostileMobsOnly")
                .define("hostileMobsOnly", true);

        UNSUCCESSFUL_TAMING_CHANCE = builder
                .translation("petting.configuration.taming.unsuccessfulTamingChance")
                .defineInRange("unsuccessfulTamingChance", 33.3, 0.0, 100.0);

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

    public static double getUnsuccessfulTamingChance() {
        return UNSUCCESSFUL_TAMING_CHANCE.get();
    }

    public static double getUnsuccessfulTamingRatio() {
        return UNSUCCESSFUL_TAMING_CHANCE.get() / 100.0;
    }
}
