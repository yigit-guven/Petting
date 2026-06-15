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
    public static boolean sitHealEnabled = true;
    public static double sitHealAmount = 1.0;
    public static int sitHealInterval = 40;
    public static double followDistance = 10.0;
    public static double teleportDistance = 20.0;
    public static double boundRoamRadius = 10.0;
    public static boolean preventPetToOwnerDamage = true;
    public static boolean allowOwnerToHurtPets = false;

    // Pet Stat Settings
    public static double petBaseArmor = 0.0;
    public static double petBaseArmorToughness = 0.0;

    // Pet Inventory Settings
    public static double petPortraitRenderScale = 45.0;

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

    // Tools
    public static String statusTool = "minecraft:stick";
    public static String aggressionTool = "minecraft:iron_sword";
    public static String defenseTool = "minecraft:shield";
    public static String guardTool = "minecraft:cookie";
    public static String followDistTool = "petting:follow_whistle";
    public static String teleportDistTool = "petting:teleport_orb";
    public static String whistleTool = "minecraft:clock";
    public static String tetherTool = "petting:pet_tether";
    public static String releaseTool = "minecraft:shears";
    public static String globalWhistleTool = "minecraft:goat_horn";

    // Filters
    public static boolean whitelistOnly = false;
    public static List<String> tamingWhitelist = new ArrayList<>();
    public static boolean blacklistEnabled = true;
    public static List<String> tamingBlacklist = new ArrayList<>();

    // Custom
    public static String tamingItem = "petting:golden_wheat";
    public static List<String> customTamingItems = new ArrayList<>();
    public static List<String> petCategories = new ArrayList<>();

    // Inventory & Equipment
    public static List<String> extraEquippableMobs = new ArrayList<>();
    public static boolean alwaysShowEquipmentSlots = false;
    public static boolean inventoryBlacklistEnabled = false;
    public static List<String> inventoryBlacklist = new ArrayList<>();
    public static boolean inventoryWhitelistOnly = false;
    public static List<String> inventoryWhitelist = new ArrayList<>();

    // Riding
    public static boolean allowPetRiding = true;
    public static boolean mountRequireSaddle = false;
    public static double landRidingSpeedMultiplier = 1.0;
    public static double flyingRidingSpeedMultiplier = 1.0;
    public static double swimmingRidingSpeedMultiplier = 1.0;
    public static boolean allowPetAttackWhileRiding = true;
    public static boolean ridingBlacklistEnabled = false;
    public static List<String> ridingBlacklist = new ArrayList<>();
    public static boolean ridingWhitelistOnly = false;
    public static List<String> ridingWhitelist = new ArrayList<>();
    public static List<String> manualFlyingMobs = new ArrayList<>();
    public static List<String> manualSwimmingMobs = new ArrayList<>();

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
        }
        // Always save to ensure new properties are written to the file
        save();
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
        sitHealEnabled = data.sitHealEnabled;
        sitHealAmount = data.sitHealAmount;
        sitHealInterval = data.sitHealInterval;
        followDistance = data.followDistance;
        teleportDistance = data.teleportDistance;
        boundRoamRadius = data.boundRoamRadius;
        preventPetToOwnerDamage = data.preventPetToOwnerDamage;
        allowOwnerToHurtPets = data.allowOwnerToHurtPets;
        petBaseArmor = data.petBaseArmor;
        petBaseArmorToughness = data.petBaseArmorToughness;
        petPortraitRenderScale = data.petPortraitRenderScale;
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
        statusTool = data.statusTool != null ? data.statusTool : "minecraft:stick";
        aggressionTool = data.aggressionTool != null ? data.aggressionTool : "minecraft:iron_sword";
        defenseTool = data.defenseTool != null ? data.defenseTool : "minecraft:shield";
        guardTool = data.guardTool != null ? data.guardTool : "minecraft:cookie";
        followDistTool = data.followDistTool != null ? data.followDistTool : "petting:follow_whistle";
        teleportDistTool = data.teleportDistTool != null ? data.teleportDistTool : "petting:teleport_orb";
        whistleTool = data.whistleTool != null ? data.whistleTool : "minecraft:clock";
        tetherTool = data.tetherTool != null ? data.tetherTool : "petting:pet_tether";
        releaseTool = data.releaseTool != null ? data.releaseTool : "minecraft:shears";
        globalWhistleTool = data.globalWhistleTool != null ? data.globalWhistleTool : "minecraft:goat_horn";
        whitelistOnly = data.whitelistOnly;
        tamingWhitelist = data.tamingWhitelist != null ? data.tamingWhitelist : new ArrayList<>();
        blacklistEnabled = data.blacklistEnabled;
        tamingBlacklist = data.tamingBlacklist != null ? data.tamingBlacklist : new ArrayList<>();
        tamingItem = data.tamingItem != null ? data.tamingItem : "petting:golden_wheat";
        customTamingItems = data.customTamingItems != null ? data.customTamingItems : new ArrayList<>();
        petCategories = data.petCategories != null ? data.petCategories : new ArrayList<>();
        
        extraEquippableMobs = data.extraEquippableMobs != null ? data.extraEquippableMobs : new ArrayList<>();
        alwaysShowEquipmentSlots = data.alwaysShowEquipmentSlots;
        inventoryBlacklistEnabled = data.inventoryBlacklistEnabled;
        inventoryBlacklist = data.inventoryBlacklist != null ? data.inventoryBlacklist : new ArrayList<>();
        inventoryWhitelistOnly = data.inventoryWhitelistOnly;
        inventoryWhitelist = data.inventoryWhitelist != null ? data.inventoryWhitelist : new ArrayList<>();
        
        allowPetRiding = data.allowPetRiding;
        mountRequireSaddle = data.mountRequireSaddle;
        landRidingSpeedMultiplier = data.landRidingSpeedMultiplier;
        flyingRidingSpeedMultiplier = data.flyingRidingSpeedMultiplier;
        swimmingRidingSpeedMultiplier = data.swimmingRidingSpeedMultiplier;
        allowPetAttackWhileRiding = data.allowPetAttackWhileRiding;
        ridingBlacklistEnabled = data.ridingBlacklistEnabled;
        ridingBlacklist = data.ridingBlacklist != null ? data.ridingBlacklist : new ArrayList<>();
        ridingWhitelistOnly = data.ridingWhitelistOnly;
        ridingWhitelist = data.ridingWhitelist != null ? data.ridingWhitelist : new ArrayList<>();
        manualFlyingMobs = data.manualFlyingMobs != null ? data.manualFlyingMobs : new ArrayList<>();
        manualSwimmingMobs = data.manualSwimmingMobs != null ? data.manualSwimmingMobs : new ArrayList<>();
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
        data.sitHealEnabled = sitHealEnabled;
        data.sitHealAmount = sitHealAmount;
        data.sitHealInterval = sitHealInterval;
        data.followDistance = followDistance;
        data.teleportDistance = teleportDistance;
        data.boundRoamRadius = boundRoamRadius;
        data.preventPetToOwnerDamage = preventPetToOwnerDamage;
        data.allowOwnerToHurtPets = allowOwnerToHurtPets;
        data.petBaseArmor = petBaseArmor;
        data.petBaseArmorToughness = petBaseArmorToughness;
        data.petPortraitRenderScale = petPortraitRenderScale;
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
        data.statusTool = statusTool;
        data.aggressionTool = aggressionTool;
        data.defenseTool = defenseTool;
        data.guardTool = guardTool;
        data.followDistTool = followDistTool;
        data.teleportDistTool = teleportDistTool;
        data.whistleTool = whistleTool;
        data.tetherTool = tetherTool;
        data.releaseTool = releaseTool;
        data.globalWhistleTool = globalWhistleTool;
        data.whitelistOnly = whitelistOnly;
        data.tamingWhitelist = tamingWhitelist;
        data.blacklistEnabled = blacklistEnabled;
        data.tamingBlacklist = tamingBlacklist;
        data.tamingItem = tamingItem;
        data.customTamingItems = customTamingItems;
        data.petCategories = petCategories;
        
        data.extraEquippableMobs = extraEquippableMobs;
        data.alwaysShowEquipmentSlots = alwaysShowEquipmentSlots;
        data.inventoryBlacklistEnabled = inventoryBlacklistEnabled;
        data.inventoryBlacklist = inventoryBlacklist;
        data.inventoryWhitelistOnly = inventoryWhitelistOnly;
        data.inventoryWhitelist = inventoryWhitelist;
        
        data.allowPetRiding = allowPetRiding;
        data.mountRequireSaddle = mountRequireSaddle;
        data.landRidingSpeedMultiplier = landRidingSpeedMultiplier;
        data.flyingRidingSpeedMultiplier = flyingRidingSpeedMultiplier;
        data.swimmingRidingSpeedMultiplier = swimmingRidingSpeedMultiplier;
        data.allowPetAttackWhileRiding = allowPetAttackWhileRiding;
        data.ridingBlacklistEnabled = ridingBlacklistEnabled;
        data.ridingBlacklist = ridingBlacklist;
        data.ridingWhitelistOnly = ridingWhitelistOnly;
        data.ridingWhitelist = ridingWhitelist;
        data.manualFlyingMobs = manualFlyingMobs;
        data.manualSwimmingMobs = manualSwimmingMobs;
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
        private boolean sitHealEnabled = true;
        private double sitHealAmount = 1.0;
        private int sitHealInterval = 40;
        private double followDistance = 10.0;
        private double teleportDistance = 20.0;
        private double boundRoamRadius = 10.0;
        private boolean preventPetToOwnerDamage = true;
        private boolean allowOwnerToHurtPets = false;
        private double petBaseArmor = 0.0;
        private double petBaseArmorToughness = 0.0;
        private double petPortraitRenderScale = 45.0;
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
        private String statusTool = "minecraft:stick";
        private String aggressionTool = "minecraft:iron_sword";
        private String defenseTool = "minecraft:shield";
        private String guardTool = "minecraft:cookie";
        private String followDistTool = "petting:follow_whistle";
        private String teleportDistTool = "petting:teleport_orb";
        private String whistleTool = "minecraft:clock";
        private String tetherTool = "petting:pet_tether";
        private String releaseTool = "minecraft:shears";
        private String globalWhistleTool = "minecraft:goat_horn";
        private boolean whitelistOnly = false;
        private List<String> tamingWhitelist = new ArrayList<>();
        private boolean blacklistEnabled = true;
        private List<String> tamingBlacklist = new ArrayList<>();
        private String tamingItem = "petting:golden_wheat";
        private List<String> customTamingItems = new ArrayList<>();
        private List<String> petCategories = new ArrayList<>();
        
        private List<String> extraEquippableMobs = new ArrayList<>();
        private boolean alwaysShowEquipmentSlots = false;
        private boolean inventoryBlacklistEnabled = false;
        private List<String> inventoryBlacklist = new ArrayList<>();
        private boolean inventoryWhitelistOnly = false;
        private List<String> inventoryWhitelist = new ArrayList<>();
        
        private boolean allowPetRiding = true;
        private boolean mountRequireSaddle = false;
        private double landRidingSpeedMultiplier = 1.0;
        private double flyingRidingSpeedMultiplier = 1.0;
        private double swimmingRidingSpeedMultiplier = 1.0;
        private boolean allowPetAttackWhileRiding = true;
        private boolean ridingBlacklistEnabled = false;
        private List<String> ridingBlacklist = new ArrayList<>();
        private boolean ridingWhitelistOnly = false;
        private List<String> ridingWhitelist = new ArrayList<>();
        private List<String> manualFlyingMobs = new ArrayList<>();
        private List<String> manualSwimmingMobs = new ArrayList<>();
    }
}
