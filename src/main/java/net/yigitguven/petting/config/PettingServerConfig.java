package net.yigitguven.petting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.CombatMode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PettingServerConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("petting-server.json");

    public static class Data {
        // Taming
        public double maxHealthPercentage = 30.0;
        public boolean hostileMobsOnly = true;
        public double unsuccessfulTamingChance = 33.3;
        public List<String> untameableEntities = new ArrayList<>();
        public int maxPetCount = -1;

        // Friendly Fire
        public boolean ownerCanAttackPets = true;
        public boolean petsCanAttackPets = false;

        // Behavior
        public boolean disableBuiltInHostility = true;
        public boolean enablePetInventory = true;
        public boolean enableCollarSlot = true;
        public boolean enablePetSitting = true;
        public boolean forgiveAttacksOnSit = true;
        public boolean flyingPetsHoverWhenSitting = true;
        public boolean swimmingPetsStayStillWhenSitting = true;
        public boolean hideTamedBossBars = true;
        public boolean tamedMonstersPreventSleep = false;
        public boolean enablePetDeathMessages = true;
        public boolean enableEggConvertedFeedback = false;
        public boolean enablePassiveMobAttacks = false;
        public int passiveMobAttackDamage = 2;

        // Riding
        public boolean enablePetRiding = true;
        public boolean useWhitelist = false;
        public List<String> ridingWhitelist = new ArrayList<>();
        public List<String> ridingBlacklist = new ArrayList<>();
        public boolean allowRidingWithoutSaddle = true;
        public boolean riddenPetsAttackOnClick = true;
        public double flyingPetAscentSpeed = 0.35;
        public double flyingPetDescentSpeed = 0.35;
        public double flyingPetSpeedMultiplier = 1.0;
        public double swimmingPetSpeedMultiplier = 1.0;
        public int riddenAttackCooldownTicks = 10;

        // Collar defaults
        public CombatMode defaultCombatMode = CombatMode.AGGRESSIVE;
        public int defaultFollowDistance = 4;
        public int minFollowDistance = 2;
        public int maxFollowDistance = 32;
        public int defaultTeleportDistance = 20;
        public int minTeleportDistance = 8;
        public int maxTeleportDistance = 64;
        public boolean defaultTeleportEnabled = true;
        public boolean defaultPreferRangedAttack = true;
        public boolean defaultDefendOtherPets = true;
        public boolean defaultRetaliateWhenHurt = true;
        public boolean enableCollarRespawn = true;
        public boolean enableCollarModels = true;
    }

    private static Data data = new Data();

    public static void load() {
        File configFile = CONFIG_PATH.toFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                Data loaded = GSON.fromJson(reader, Data.class);
                if (loaded != null) {
                    data = loaded;
                    return;
                }
            } catch (Exception e) {
                Petting.LOGGER.error("Failed to load server config, using defaults", e);
            }
        }
        save();
    }

    public static void save() {
        try {
            File configFile = CONFIG_PATH.toFile();
            configFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            Petting.LOGGER.error("Failed to save server config", e);
        }
    }

    public static double getMaxHealthPercentage() {
        return data.maxHealthPercentage;
    }

    public static double getMaxHealthRatio() {
        return getMaxHealthPercentage() / 100.0;
    }

    public static double getUnsuccessfulTamingChance() {
        return data.unsuccessfulTamingChance;
    }

    public static double getUnsuccessfulTamingRatio() {
        return getUnsuccessfulTamingChance() / 100.0;
    }

    public static List<String> getUntameableEntities() {
        return data.untameableEntities != null ? data.untameableEntities : List.of();
    }

    public static boolean isUntameable(net.minecraft.world.entity.EntityType<?> type) {
        net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(type);
        if (id == null) return false;
        String idStr = id.toString();
        for (String entry : getUntameableEntities()) {
            if (entry.equalsIgnoreCase(idStr)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHostileMobsOnly() {
        return data.hostileMobsOnly;
    }

    public static int getMaxPetCount() {
        return data.maxPetCount;
    }

    public static boolean canOwnerAttackPets() {
        return data.ownerCanAttackPets;
    }

    public static boolean canPetsAttackPets() {
        return data.petsCanAttackPets;
    }

    public static boolean shouldDisableBuiltInHostility() {
        return data.disableBuiltInHostility;
    }

    public static boolean isPetInventoryEnabled() {
        return data.enablePetInventory;
    }

    public static boolean isCollarSlotEnabled() {
        return data.enableCollarSlot;
    }

    public static boolean isPetSittingEnabled() {
        return data.enablePetSitting;
    }

    public static boolean shouldForgiveAttacksOnSit() {
        return data.forgiveAttacksOnSit;
    }

    public static boolean isFlyingPetsHoverWhenSitting() {
        return data.flyingPetsHoverWhenSitting;
    }

    public static boolean isSwimmingPetsStayStillWhenSitting() {
        return data.swimmingPetsStayStillWhenSitting;
    }

    public static boolean shouldHideTamedBossBars() {
        return data.hideTamedBossBars;
    }

    public static boolean shouldTamedMonstersPreventSleep() {
        return data.tamedMonstersPreventSleep;
    }

    public static boolean isPetDeathMessagesEnabled() {
        return data.enablePetDeathMessages;
    }

    public static CombatMode getDefaultCombatMode() {
        return data.defaultCombatMode != null ? data.defaultCombatMode : CombatMode.AGGRESSIVE;
    }

    public static boolean isPetRidingEnabled() {
        return data.enablePetRiding;
    }

    public static boolean isPetRidingUseWhitelist() {
        return data.useWhitelist;
    }

    public static List<String> getRidingWhitelist() {
        return data.ridingWhitelist != null ? data.ridingWhitelist : List.of();
    }

    public static List<String> getRidingBlacklist() {
        return data.ridingBlacklist != null ? data.ridingBlacklist : List.of();
    }

    public static boolean isPetRidingAllowed(EntityType<?> entityType) {
        if (!isPetRidingEnabled()) {
            return false;
        }
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (id == null) {
            return false;
        }
        String idString = id.toString();
        if (data.useWhitelist) {
            return data.ridingWhitelist != null && data.ridingWhitelist.contains(idString);
        } else {
            return data.ridingBlacklist == null || !data.ridingBlacklist.contains(idString);
        }
    }

    public static int getDefaultFollowDistance() {
        return data.defaultFollowDistance;
    }

    public static int getMinFollowDistance() {
        return data.minFollowDistance;
    }

    public static int getMaxFollowDistance() {
        return data.maxFollowDistance;
    }

    public static int getDefaultTeleportDistance() {
        return data.defaultTeleportDistance;
    }

    public static int getMinTeleportDistance() {
        return data.minTeleportDistance;
    }

    public static int getMaxTeleportDistance() {
        return data.maxTeleportDistance;
    }

    public static boolean isDefaultTeleportEnabled() {
        return data.defaultTeleportEnabled;
    }

    public static boolean isDefaultPreferRangedAttack() {
        return data.defaultPreferRangedAttack;
    }

    public static boolean isDefaultDefendOtherPets() {
        return data.defaultDefendOtherPets;
    }

    public static boolean isDefaultRetaliateWhenHurt() {
        return data.defaultRetaliateWhenHurt;
    }

    public static boolean isAllowRidingWithoutSaddle() {
        return data.allowRidingWithoutSaddle;
    }

    public static boolean isRiddenPetsAttackOnClick() {
        return data.riddenPetsAttackOnClick;
    }

    public static double getFlyingPetAscentSpeed() {
        return data.flyingPetAscentSpeed;
    }

    public static double getFlyingPetDescentSpeed() {
        return data.flyingPetDescentSpeed;
    }

    public static double getFlyingPetSpeedMultiplier() {
        return data.flyingPetSpeedMultiplier;
    }

    public static double getSwimmingPetSpeedMultiplier() {
        return data.swimmingPetSpeedMultiplier;
    }

    public static int getRiddenAttackCooldownTicks() {
        return data.riddenAttackCooldownTicks;
    }

    public static boolean isCollarRespawnEnabled() {
        return data.enableCollarRespawn;
    }

    public static boolean isCollarModelsEnabled() {
        return data.enableCollarModels;
    }

    public static boolean isEggConvertedFeedbackEnabled() {
        return data.enableEggConvertedFeedback;
    }

    public static boolean isPassiveMobAttacksEnabled() {
        return data.enablePassiveMobAttacks;
    }

    public static int getPassiveMobAttackDamage() {
        return data.passiveMobAttackDamage;
    }
}
