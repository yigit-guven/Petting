package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingClientConfig {
    public static final ModConfigSpec SPEC;

    // Detailed positive feedback toggles
    public static final ModConfigSpec.BooleanValue ENABLE_TAMED_SUCCESS_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_HEALED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_SITTING_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_STANDING_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_COLLAR_BOUND_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_MOUNT_CONTROLLED_FEEDBACK;

    // Detailed negative feedback toggles
    public static final ModConfigSpec.BooleanValue ENABLE_TAMING_FAILED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_TOO_HEALTHY_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_ALREADY_VANILLA_TAMED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_ALREADY_TAMED_OTHER_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_FULL_HEALTH_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_LIMIT_REACHED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_SADDLE_REQUIRED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_MOUNT_UNCONTROLLED_FEEDBACK;

    // Pet death feedback toggles
    public static final ModConfigSpec.BooleanValue ENABLE_PET_DEATH_MESSAGES;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_DIED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_RESPAWNED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_PET_RESPAWN_FAILED_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_EGG_CONVERTED_FEEDBACK;

    // Visual toggles
    public static final ModConfigSpec.BooleanValue ENABLE_COLLAR_MODELS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("feedback");

        ENABLE_TAMED_SUCCESS_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableTamedSuccessFeedback")
                .define("enableTamedSuccessFeedback", false);

        ENABLE_PET_HEALED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetHealedFeedback")
                .define("enablePetHealedFeedback", false);

        ENABLE_PET_SITTING_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetSittingFeedback")
                .define("enablePetSittingFeedback", false);

        ENABLE_PET_STANDING_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetStandingFeedback")
                .define("enablePetStandingFeedback", false);

        ENABLE_COLLAR_BOUND_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableCollarBoundFeedback")
                .define("enableCollarBoundFeedback", false);

        ENABLE_MOUNT_CONTROLLED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableMountControlledFeedback")
                .define("enableMountControlledFeedback", false);

        ENABLE_TAMING_FAILED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableTamingFailedFeedback")
                .define("enableTamingFailedFeedback", false);

        ENABLE_TOO_HEALTHY_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableTooHealthyFeedback")
                .define("enableTooHealthyFeedback", false);

        ENABLE_ALREADY_VANILLA_TAMED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableAlreadyVanillaTamedFeedback")
                .define("enableAlreadyVanillaTamedFeedback", false);

        ENABLE_ALREADY_TAMED_OTHER_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableAlreadyTamedOtherFeedback")
                .define("enableAlreadyTamedOtherFeedback", false);

        ENABLE_PET_FULL_HEALTH_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetFullHealthFeedback")
                .define("enablePetFullHealthFeedback", false);

        ENABLE_PET_LIMIT_REACHED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetLimitReachedFeedback")
                .define("enablePetLimitReachedFeedback", false);

        ENABLE_SADDLE_REQUIRED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableSaddleRequiredFeedback")
                .define("enableSaddleRequiredFeedback", false);

        ENABLE_MOUNT_UNCONTROLLED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableMountUncontrolledFeedback")
                .define("enableMountUncontrolledFeedback", false);

        ENABLE_PET_DEATH_MESSAGES = builder
                .translation("petting.configuration.feedback.enablePetDeathMessages")
                .define("enablePetDeathMessages", false);

        ENABLE_PET_DIED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetDiedFeedback")
                .define("enablePetDiedFeedback", false);

        ENABLE_PET_RESPAWNED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetRespawnedFeedback")
                .define("enablePetRespawnedFeedback", false);

        ENABLE_PET_RESPAWN_FAILED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePetRespawnFailedFeedback")
                .define("enablePetRespawnFailedFeedback", false);

        ENABLE_EGG_CONVERTED_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableEggConvertedFeedback")
                .define("enableEggConvertedFeedback", false);

        builder.pop();

        builder.push("visuals");

        ENABLE_COLLAR_MODELS = builder
                .translation("petting.configuration.visuals.enableCollarModels")
                .define("enableCollarModels", true);

        builder.pop();

        SPEC = builder.build();
    }

    public static boolean isCollarModelsEnabled() {
        return SPEC.isLoaded() ? ENABLE_COLLAR_MODELS.get() : true;
    }

    public static boolean isTamedSuccessFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_TAMED_SUCCESS_FEEDBACK.get() : false;
    }

    public static boolean isPetHealedFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_HEALED_FEEDBACK.get() : false;
    }

    public static boolean isPetSittingFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_SITTING_FEEDBACK.get() : false;
    }

    public static boolean isPetStandingFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_STANDING_FEEDBACK.get() : false;
    }

    public static boolean isCollarBoundFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_COLLAR_BOUND_FEEDBACK.get() : false;
    }

    public static boolean isMountControlledFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_MOUNT_CONTROLLED_FEEDBACK.get() : false;
    }

    public static boolean isTamingFailedFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_TAMING_FAILED_FEEDBACK.get() : false;
    }

    public static boolean isTooHealthyFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_TOO_HEALTHY_FEEDBACK.get() : false;
    }

    public static boolean isAlreadyVanillaTamedFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_ALREADY_VANILLA_TAMED_FEEDBACK.get() : false;
    }

    public static boolean isAlreadyTamedOtherFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_ALREADY_TAMED_OTHER_FEEDBACK.get() : false;
    }

    public static boolean isPetFullHealthFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_FULL_HEALTH_FEEDBACK.get() : false;
    }

    public static boolean isPetLimitReachedFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_LIMIT_REACHED_FEEDBACK.get() : false;
    }

    public static boolean isSaddleRequiredFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_SADDLE_REQUIRED_FEEDBACK.get() : false;
    }

    public static boolean isMountUncontrolledFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_MOUNT_UNCONTROLLED_FEEDBACK.get() : false;
    }

    public static boolean isPetDeathMessagesEnabled() {
        return SPEC.isLoaded() ? ENABLE_PET_DEATH_MESSAGES.get() : false;
    }

    public static boolean isPetDiedFeedbackEnabled() {
        return SPEC.isLoaded() ? (ENABLE_PET_DEATH_MESSAGES.get() || ENABLE_PET_DIED_FEEDBACK.get()) : false;
    }

    public static boolean isPetRespawnedFeedbackEnabled() {
        return SPEC.isLoaded() ? (ENABLE_PET_DEATH_MESSAGES.get() || ENABLE_PET_RESPAWNED_FEEDBACK.get()) : false;
    }

    public static boolean isPetRespawnFailedFeedbackEnabled() {
        return SPEC.isLoaded() ? (ENABLE_PET_DEATH_MESSAGES.get() || ENABLE_PET_RESPAWN_FAILED_FEEDBACK.get()) : false;
    }

    public static boolean isEggConvertedFeedbackEnabled() {
        return SPEC.isLoaded() ? ENABLE_EGG_CONVERTED_FEEDBACK.get() : false;
    }

    public static boolean isFeedbackMessageEnabled(String key) {
        if (!SPEC.isLoaded()) {
            return false;
        }
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
