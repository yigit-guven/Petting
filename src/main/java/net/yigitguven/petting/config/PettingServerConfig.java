package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingServerConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue MAX_HEALTH_PERCENTAGE;
    public static final ModConfigSpec.BooleanValue HOSTILE_MOBS_ONLY;
    public static final ModConfigSpec.DoubleValue UNSUCCESSFUL_TAMING_CHANCE;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> UNTAMEABLE_ENTITIES;
    public static final ModConfigSpec.IntValue MAX_PET_COUNT;
    public static final ModConfigSpec.BooleanValue OWNER_CAN_ATTACK_PETS;
    public static final ModConfigSpec.BooleanValue PETS_CAN_ATTACK_PETS;
    public static final ModConfigSpec.BooleanValue DISABLE_BUILT_IN_HOSTILITY;
    public static final ModConfigSpec.BooleanValue ENABLE_COLLAR_SLOT;
    public static final ModConfigSpec.EnumValue<net.yigitguven.petting.data.CombatMode> DEFAULT_COMBAT_MODE;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_RIDING;
    public static final ModConfigSpec.BooleanValue RIDING_USE_WHITELIST;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> RIDING_WHITELIST;
    public static final ModConfigSpec.ConfigValue<java.util.List<? extends String>> RIDING_BLACKLIST;
    public static final ModConfigSpec.IntValue DEFAULT_FOLLOW_DISTANCE;
    public static final ModConfigSpec.IntValue MIN_FOLLOW_DISTANCE;
    public static final ModConfigSpec.IntValue MAX_FOLLOW_DISTANCE;
    public static final ModConfigSpec.IntValue DEFAULT_TELEPORT_DISTANCE;
    public static final ModConfigSpec.IntValue MIN_TELEPORT_DISTANCE;
    public static final ModConfigSpec.IntValue MAX_TELEPORT_DISTANCE;
    public static final ModConfigSpec.BooleanValue DEFAULT_TELEPORT_ENABLED;
    public static final ModConfigSpec.BooleanValue DEFAULT_PREFER_RANGED_ATTACK;
    public static final ModConfigSpec.BooleanValue DEFAULT_DEFEND_OTHER_PETS;
    public static final ModConfigSpec.BooleanValue DEFAULT_RETALIATE_WHEN_HURT;

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

        builder.push("friendlyFire");

        OWNER_CAN_ATTACK_PETS = builder
                .translation("petting.configuration.friendlyFire.ownerCanAttackPets")
                .define("ownerCanAttackPets", true);

        PETS_CAN_ATTACK_PETS = builder
                .translation("petting.configuration.friendlyFire.petsCanAttackPets")
                .define("petsCanAttackPets", false);

        builder.pop();

        builder.push("behavior");

        DISABLE_BUILT_IN_HOSTILITY = builder
                .translation("petting.configuration.behavior.disableBuiltInHostility")
                .define("disableBuiltInHostility", true);

        ENABLE_COLLAR_SLOT = builder
                .translation("petting.configuration.behavior.enableCollarSlot")
                .define("enableCollarSlot", true);

        builder.pop();

        builder.push("riding");

        ENABLE_PET_RIDING = builder
                .translation("petting.configuration.riding.enablePetRiding")
                .define("enablePetRiding", true);

        RIDING_USE_WHITELIST = builder
                .translation("petting.configuration.riding.useWhitelist")
                .define("useWhitelist", false);

        RIDING_WHITELIST = builder
                .translation("petting.configuration.riding.whitelist")
                .defineListAllowEmpty("whitelist", java.util.List.of(), () -> "", obj -> obj instanceof String);

        RIDING_BLACKLIST = builder
                .translation("petting.configuration.riding.blacklist")
                .defineListAllowEmpty("blacklist", java.util.List.of(), () -> "", obj -> obj instanceof String);

        builder.pop();

        builder.push("collar");

        DEFAULT_COMBAT_MODE = builder
                .translation("petting.configuration.collar.defaultCombatMode")
                .defineEnum("defaultCombatMode", net.yigitguven.petting.data.CombatMode.AGGRESSIVE);

        DEFAULT_FOLLOW_DISTANCE = builder
                .translation("petting.configuration.collar.defaultFollowDistance")
                .defineInRange("defaultFollowDistance", 4, 1, 32);

        MIN_FOLLOW_DISTANCE = builder
                .translation("petting.configuration.collar.minFollowDistance")
                .defineInRange("minFollowDistance", 2, 1, 16);

        MAX_FOLLOW_DISTANCE = builder
                .translation("petting.configuration.collar.maxFollowDistance")
                .defineInRange("maxFollowDistance", 32, 4, 64);

        DEFAULT_TELEPORT_DISTANCE = builder
                .translation("petting.configuration.collar.defaultTeleportDistance")
                .defineInRange("defaultTeleportDistance", 20, 5, 128);

        MIN_TELEPORT_DISTANCE = builder
                .translation("petting.configuration.collar.minTeleportDistance")
                .defineInRange("minTeleportDistance", 8, 5, 32);

        MAX_TELEPORT_DISTANCE = builder
                .translation("petting.configuration.collar.maxTeleportDistance")
                .defineInRange("maxTeleportDistance", 64, 16, 200);

        DEFAULT_TELEPORT_ENABLED = builder
                .translation("petting.configuration.collar.defaultTeleportEnabled")
                .define("defaultTeleportEnabled", true);

        DEFAULT_PREFER_RANGED_ATTACK = builder
                .translation("petting.configuration.collar.defaultPreferRangedAttack")
                .define("defaultPreferRangedAttack", true);

        DEFAULT_DEFEND_OTHER_PETS = builder
                .translation("petting.configuration.collar.defaultDefendOtherPets")
                .define("defaultDefendOtherPets", true);

        DEFAULT_RETALIATE_WHEN_HURT = builder
                .translation("petting.configuration.collar.defaultRetaliateWhenHurt")
                .define("defaultRetaliateWhenHurt", true);

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

    public static boolean canOwnerAttackPets() {
        return SPEC.isLoaded() ? OWNER_CAN_ATTACK_PETS.get() : true;
    }

    public static boolean canPetsAttackPets() {
        return SPEC.isLoaded() ? PETS_CAN_ATTACK_PETS.get() : false;
    }

    public static boolean shouldDisableBuiltInHostility() {
        return SPEC.isLoaded() ? DISABLE_BUILT_IN_HOSTILITY.get() : true;
    }

    public static net.yigitguven.petting.data.CombatMode getDefaultCombatMode() {
        return SPEC.isLoaded() ? DEFAULT_COMBAT_MODE.get() : net.yigitguven.petting.data.CombatMode.AGGRESSIVE;
    }

    public static boolean isCollarSlotEnabled() {
        return SPEC.isLoaded() ? ENABLE_COLLAR_SLOT.get() : true;
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

    public static boolean isPetRidingEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_RIDING.get() : true;
    }

    public static boolean isPetRidingUseWhitelist() {
        return SPEC.isLoaded() ? RIDING_USE_WHITELIST.get() : false;
    }

    public static java.util.List<? extends String> getRidingWhitelist() {
        return SPEC.isLoaded() ? RIDING_WHITELIST.get() : java.util.List.of();
    }

    public static java.util.List<? extends String> getRidingBlacklist() {
        return SPEC.isLoaded() ? RIDING_BLACKLIST.get() : java.util.List.of();
    }

    public static boolean isPetRidingAllowed(net.minecraft.world.entity.EntityType<?> entityType) {
        if (!isPetRidingEnabled()) {
            return false;
        }
        if (!SPEC.isLoaded()) {
            return true;
        }
        net.minecraft.resources.Identifier id = net.minecraft.world.entity.EntityType.getKey(entityType);
        if (id == null) {
            return false;
        }
        String idString = id.toString();
        if (RIDING_USE_WHITELIST.get()) {
            for (String entry : RIDING_WHITELIST.get()) {
                if (idString.equals(entry)) {
                    return true;
                }
            }
            return false;
        } else {
            for (String entry : RIDING_BLACKLIST.get()) {
                if (idString.equals(entry)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static int getDefaultFollowDistance() {
        return SPEC.isLoaded() ? DEFAULT_FOLLOW_DISTANCE.get() : 4;
    }

    public static int getMinFollowDistance() {
        return SPEC.isLoaded() ? MIN_FOLLOW_DISTANCE.get() : 2;
    }

    public static int getMaxFollowDistance() {
        return SPEC.isLoaded() ? MAX_FOLLOW_DISTANCE.get() : 32;
    }

    public static int getDefaultTeleportDistance() {
        return SPEC.isLoaded() ? DEFAULT_TELEPORT_DISTANCE.get() : 20;
    }

    public static int getMinTeleportDistance() {
        return SPEC.isLoaded() ? MIN_TELEPORT_DISTANCE.get() : 8;
    }

    public static int getMaxTeleportDistance() {
        return SPEC.isLoaded() ? MAX_TELEPORT_DISTANCE.get() : 64;
    }

    public static boolean isDefaultTeleportEnabled() {
        return SPEC.isLoaded() ? DEFAULT_TELEPORT_ENABLED.get() : true;
    }

    public static boolean isDefaultPreferRangedAttack() {
        return SPEC.isLoaded() ? DEFAULT_PREFER_RANGED_ATTACK.get() : true;
    }

    public static boolean isDefaultDefendOtherPets() {
        return SPEC.isLoaded() ? DEFAULT_DEFEND_OTHER_PETS.get() : true;
    }

    public static boolean isDefaultRetaliateWhenHurt() {
        return SPEC.isLoaded() ? DEFAULT_RETALIATE_WHEN_HURT.get() : true;
    }
}
