package net.yigitguven.petting.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PettingClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_POSITIVE_FEEDBACK;
    public static final ModConfigSpec.BooleanValue ENABLE_NEGATIVE_FEEDBACK;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("feedback");

        ENABLE_POSITIVE_FEEDBACK = builder
                .translation("petting.configuration.feedback.enablePositiveFeedback")
                .define("enablePositiveFeedback", false);

        ENABLE_NEGATIVE_FEEDBACK = builder
                .translation("petting.configuration.feedback.enableNegativeFeedback")
                .define("enableNegativeFeedback", false);

        builder.pop();

        SPEC = builder.build();
    }

    public static boolean isPositiveFeedbackEnabled() {
        return ENABLE_POSITIVE_FEEDBACK.get();
    }

    public static boolean isNegativeFeedbackEnabled() {
        return ENABLE_NEGATIVE_FEEDBACK.get();
    }
}
