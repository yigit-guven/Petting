package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH_PERCENTAGE;
    public static final ModConfigSpec.BooleanValue HOSTILE_MOBS_ONLY;
    public static final ModConfigSpec.DoubleValue UNSUCCESSFUL_TAMING_CHANCE;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> UNTAMEABLE_ENTITIES;
    public static final ModConfigSpec.IntValue MAX_PET_COUNT;

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

        UNTAMEABLE_ENTITIES = builder
                .translation("petting.configuration.taming.untameableEntities")
                .defineListAllowEmpty("untameableEntities", java.util.List.of(), () -> "", obj -> obj instanceof String);

        MAX_PET_COUNT = builder
                .translation("petting.configuration.taming.maxPetCount")
                .defineInRange("maxPetCount", -1, -1, Integer.MAX_VALUE);

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

    public static int getMaxPetCount() {
        return MAX_PET_COUNT.get();
    }

    public static boolean hasPetLimit() {
        return getMaxPetCount() >= 0;
    }

    public static boolean isUntameable(net.minecraft.world.entity.EntityType<?> entityType) {
        if (!SPEC.isLoaded()) {
            return false;
        }
        net.minecraft.resources.Identifier id = net.minecraft.world.entity.EntityType.getKey(entityType);
        if (id == null) {
            return false;
        }
        String idString = id.toString();
        for (String entry : UNTAMEABLE_ENTITIES.get()) {
            if (idString.equals(entry)) {
                return true;
            }
        }
        return false;
    }
}
