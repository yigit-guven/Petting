package net.yigitguven.petting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.yigitguven.petting.Petting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;

public class PettingClientConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("petting-client.json");

    public static class Data {
        // Detailed positive feedback toggles
        public boolean enableTamedSuccessFeedback = false;
        public boolean enablePetHealedFeedback = false;
        public boolean enablePetSittingFeedback = false;
        public boolean enablePetStandingFeedback = false;
        public boolean enableCollarBoundFeedback = false;
        public boolean enableMountControlledFeedback = false;

        // Detailed negative feedback toggles
        public boolean enableTamingFailedFeedback = false;
        public boolean enableTooHealthyFeedback = false;
        public boolean enableAlreadyVanillaTamedFeedback = false;
        public boolean enableAlreadyTamedOtherFeedback = false;
        public boolean enablePetFullHealthFeedback = false;
        public boolean enablePetLimitReachedFeedback = false;
        public boolean enableSaddleRequiredFeedback = false;
        public boolean enableMountUncontrolledFeedback = false;

        // Pet death feedback toggles
        public boolean enablePetDeathMessages = false;
        public boolean enablePetDiedFeedback = false;
        public boolean enablePetRespawnedFeedback = false;
        public boolean enablePetRespawnFailedFeedback = false;
        public boolean enableEggConvertedFeedback = false;

        // Visual toggles
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
                Petting.LOGGER.error("Failed to load client config, using defaults", e);
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
            Petting.LOGGER.error("Failed to save client config", e);
        }
    }

    public static boolean isCollarModelsEnabled() {
        return data.enableCollarModels;
    }

    public static boolean isTamedSuccessFeedbackEnabled() {
        return data.enableTamedSuccessFeedback;
    }

    public static boolean isPetHealedFeedbackEnabled() {
        return data.enablePetHealedFeedback;
    }

    public static boolean isPetSittingFeedbackEnabled() {
        return data.enablePetSittingFeedback;
    }

    public static boolean isPetStandingFeedbackEnabled() {
        return data.enablePetStandingFeedback;
    }

    public static boolean isCollarBoundFeedbackEnabled() {
        return data.enableCollarBoundFeedback;
    }

    public static boolean isMountControlledFeedbackEnabled() {
        return data.enableMountControlledFeedback;
    }

    public static boolean isTamingFailedFeedbackEnabled() {
        return data.enableTamingFailedFeedback;
    }

    public static boolean isTooHealthyFeedbackEnabled() {
        return data.enableTooHealthyFeedback;
    }

    public static boolean isAlreadyVanillaTamedFeedbackEnabled() {
        return data.enableAlreadyVanillaTamedFeedback;
    }

    public static boolean isAlreadyTamedOtherFeedbackEnabled() {
        return data.enableAlreadyTamedOtherFeedback;
    }

    public static boolean isPetFullHealthFeedbackEnabled() {
        return data.enablePetFullHealthFeedback;
    }

    public static boolean isPetLimitReachedFeedbackEnabled() {
        return data.enablePetLimitReachedFeedback;
    }

    public static boolean isSaddleRequiredFeedbackEnabled() {
        return data.enableSaddleRequiredFeedback;
    }

    public static boolean isMountUncontrolledFeedbackEnabled() {
        return data.enableMountUncontrolledFeedback;
    }

    public static boolean isPetDeathMessagesEnabled() {
        return data.enablePetDeathMessages;
    }

    public static boolean isPetDiedFeedbackEnabled() {
        return data.enablePetDeathMessages || data.enablePetDiedFeedback;
    }

    public static boolean isPetRespawnedFeedbackEnabled() {
        return data.enablePetDeathMessages || data.enablePetRespawnedFeedback;
    }

    public static boolean isPetRespawnFailedFeedbackEnabled() {
        return data.enablePetDeathMessages || data.enablePetRespawnFailedFeedback;
    }

    public static boolean isEggConvertedFeedbackEnabled() {
        return data.enableEggConvertedFeedback;
    }

    public static boolean isFeedbackMessageEnabled(String key) {
        return switch (key) {
            case "petting.action.tamed_success" -> isTamedSuccessFeedbackEnabled();
            case "petting.action.pet_healed" -> isPetHealedFeedbackEnabled();
            case "petting.action.pet_sitting" -> isPetSittingFeedbackEnabled();
            case "petting.action.pet_standing" -> isPetStandingFeedbackEnabled();
            case "petting.action.collar_bound" -> isCollarBoundFeedbackEnabled();
            case "petting.action.mount_controlled" -> isMountControlledFeedbackEnabled();

            case "petting.action.taming_failed" -> isTamingFailedFeedbackEnabled();
            case "petting.action.too_healthy" -> isTooHealthyFeedbackEnabled();
            case "petting.action.already_vanilla_tamed" -> isAlreadyVanillaTamedFeedbackEnabled();
            case "petting.action.already_tamed_other" -> isAlreadyTamedOtherFeedbackEnabled();
            case "petting.action.pet_full_health" -> isPetFullHealthFeedbackEnabled();
            case "petting.action.pet_limit_reached" -> isPetLimitReachedFeedbackEnabled();
            case "petting.action.saddle_required" -> isSaddleRequiredFeedbackEnabled();
            case "petting.action.mount_uncontrolled" -> isMountUncontrolledFeedbackEnabled();

            case "petting.action.pet_died" -> isPetDiedFeedbackEnabled();
            case "petting.action.pet_respawned" -> isPetRespawnedFeedbackEnabled();
            case "petting.action.pet_respawn_failed" -> isPetRespawnFailedFeedbackEnabled();
            case "petting.action.egg_converted_to_golden" -> isEggConvertedFeedbackEnabled();

            default -> true;
        };
    }
}
