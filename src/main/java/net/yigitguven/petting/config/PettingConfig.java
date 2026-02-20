package net.yigitguven.petting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PettingConfig {
    private static final Logger LOGGER = LogManager.getLogger("PettingConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "petting.json");

    // General
    public static boolean disableRespawnOnTame = true;
    public static int interactionCooldown = 20;
    public static boolean enableParticles = true;
    public static boolean requireKillToTame = false;
    public static double tameHealthThreshold = 0.0;
    public static double tameChance = 0.33;
    public static boolean healthScalesTamingChance = false;
    public static int maxPetsPerPlayer = -1;
    public static boolean allowGoldenWheat = true;
    public static boolean enableGoatHornWhistle = true;
    public static boolean whistleTeleportsTethered = false;
    public static boolean hideTamedBossBars = true;

    // Behavior
    public static double sitHealAmount = 1.0;
    public static int sitHealInterval = 40;
    public static double followDistance = 10.0;
    public static double teleportDistance = 20.0;
    public static double boundRoamRadius = 10.0;

    // Controls
    public static ControlScheme controlScheme = ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT;
    public static FeedbackStyle commandFeedbackStyle = FeedbackStyle.ACTION_BAR;

    // Interactions
    public static boolean allowPerPetStatus = true;
    public static boolean allowPerPetAggression = true;
    public static boolean allowPerPetSelfDefense = true;
    public static boolean allowPerPetGuard = true;
    public static boolean allowPerPetFollowDist = true;
    public static boolean allowPerPetTeleportDist = true;
    public static boolean allowPerPetWhistleToggle = true;
    public static boolean allowPetTethering = true;
    public static boolean allowPetReleasing = true;

    // Filters
    public static boolean whitelistOnly = false;
    public static List<String> tamingWhitelist = new ArrayList<>();
    public static boolean blacklistEnabled = false;
    public static List<String> tamingBlacklist = new ArrayList<>();

    // Custom
    public static List<String> customTamingItems = new ArrayList<>();
    public static List<String> petCategories = new ArrayList<>();

    public enum ControlScheme {
        RIGHT_CLICK_SIT_SHIFT_WAIT,
        RIGHT_CLICK_CYCLE,
        SHIFT_RIGHT_CLICK_CYCLE
    }

    public enum FeedbackStyle {
        ACTION_BAR,
        CHAT,
        NONE
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    apply(data);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load Petting config", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(createData(), writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save Petting config", e);
        }
    }

    private static void apply(ConfigData data) {
        disableRespawnOnTame = data.disableRespawnOnTame;
        interactionCooldown = data.interactionCooldown;
        enableParticles = data.enableParticles;
        requireKillToTame = data.requireKillToTame;
        tameHealthThreshold = data.tameHealthThreshold;
        tameChance = data.tameChance;
        healthScalesTamingChance = data.healthScalesTamingChance;
        maxPetsPerPlayer = data.maxPetsPerPlayer;
        allowGoldenWheat = data.allowGoldenWheat;
        enableGoatHornWhistle = data.enableGoatHornWhistle;
        whistleTeleportsTethered = data.whistleTeleportsTethered;
        hideTamedBossBars = data.hideTamedBossBars;
        sitHealAmount = data.sitHealAmount;
        sitHealInterval = data.sitHealInterval;
        followDistance = data.followDistance;
        teleportDistance = data.teleportDistance;
        boundRoamRadius = data.boundRoamRadius;
        controlScheme = data.controlScheme;
        commandFeedbackStyle = data.commandFeedbackStyle;
        allowPerPetStatus = data.allowPerPetStatus;
        allowPerPetAggression = data.allowPerPetAggression;
        allowPerPetSelfDefense = data.allowPerPetSelfDefense;
        allowPerPetGuard = data.allowPerPetGuard;
        allowPerPetFollowDist = data.allowPerPetFollowDist;
        allowPerPetTeleportDist = data.allowPerPetTeleportDist;
        allowPerPetWhistleToggle = data.allowPerPetWhistleToggle;
        allowPetTethering = data.allowPetTethering;
        allowPetReleasing = data.allowPetReleasing;
        whitelistOnly = data.whitelistOnly;
        tamingWhitelist = data.tamingWhitelist != null ? data.tamingWhitelist : new ArrayList<>();
        blacklistEnabled = data.blacklistEnabled;
        tamingBlacklist = data.tamingBlacklist != null ? data.tamingBlacklist : new ArrayList<>();
        customTamingItems = data.customTamingItems != null ? data.customTamingItems : new ArrayList<>();
        petCategories = data.petCategories != null ? data.petCategories : new ArrayList<>();
    }

    private static ConfigData createData() {
        ConfigData data = new ConfigData();
        data.disableRespawnOnTame = disableRespawnOnTame;
        data.interactionCooldown = interactionCooldown;
        data.enableParticles = enableParticles;
        data.requireKillToTame = requireKillToTame;
        data.tameHealthThreshold = tameHealthThreshold;
        data.tameChance = tameChance;
        data.healthScalesTamingChance = healthScalesTamingChance;
        data.maxPetsPerPlayer = maxPetsPerPlayer;
        data.allowGoldenWheat = allowGoldenWheat;
        data.enableGoatHornWhistle = enableGoatHornWhistle;
        data.whistleTeleportsTethered = whistleTeleportsTethered;
        data.hideTamedBossBars = hideTamedBossBars;
        data.sitHealAmount = sitHealAmount;
        data.sitHealInterval = sitHealInterval;
        data.followDistance = followDistance;
        data.teleportDistance = teleportDistance;
        data.boundRoamRadius = boundRoamRadius;
        data.controlScheme = controlScheme;
        data.commandFeedbackStyle = commandFeedbackStyle;
        data.allowPerPetStatus = allowPerPetStatus;
        data.allowPerPetAggression = allowPerPetAggression;
        data.allowPerPetSelfDefense = allowPerPetSelfDefense;
        data.allowPerPetGuard = allowPerPetGuard;
        data.allowPerPetFollowDist = allowPerPetFollowDist;
        data.allowPerPetTeleportDist = allowPerPetTeleportDist;
        data.allowPerPetWhistleToggle = allowPerPetWhistleToggle;
        data.allowPetTethering = allowPetTethering;
        data.allowPetReleasing = allowPetReleasing;
        data.whitelistOnly = whitelistOnly;
        data.tamingWhitelist = tamingWhitelist;
        data.blacklistEnabled = blacklistEnabled;
        data.tamingBlacklist = tamingBlacklist;
        data.customTamingItems = customTamingItems;
        data.petCategories = petCategories;
        return data;
    }

    private static class ConfigData {
        private boolean disableRespawnOnTame = true;
        private int interactionCooldown = 20;
        private boolean enableParticles = true;
        private boolean requireKillToTame = false;
        private double tameHealthThreshold = 0.0;
        private double tameChance = 0.33;
        private boolean healthScalesTamingChance = false;
        private int maxPetsPerPlayer = -1;
        private boolean allowGoldenWheat = true;
        private boolean enableGoatHornWhistle = true;
        private boolean whistleTeleportsTethered = false;
        private boolean hideTamedBossBars = true;
        private double sitHealAmount = 1.0;
        private int sitHealInterval = 40;
        private double followDistance = 10.0;
        private double teleportDistance = 20.0;
        private double boundRoamRadius = 10.0;
        private ControlScheme controlScheme = ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT;
        private FeedbackStyle commandFeedbackStyle = FeedbackStyle.ACTION_BAR;
        private boolean allowPerPetStatus = true;
        private boolean allowPerPetAggression = true;
        private boolean allowPerPetSelfDefense = true;
        private boolean allowPerPetGuard = true;
        private boolean allowPerPetFollowDist = true;
        private boolean allowPerPetTeleportDist = true;
        private boolean allowPerPetWhistleToggle = true;
        private boolean allowPetTethering = true;
        private boolean allowPetReleasing = true;
        private boolean whitelistOnly = false;
        private List<String> tamingWhitelist = new ArrayList<>();
        private boolean blacklistEnabled = false;
        private List<String> tamingBlacklist = new ArrayList<>();
        private List<String> customTamingItems = new ArrayList<>();
        private List<String> petCategories = new ArrayList<>();
    }
}
