package net.yigitguven.petting.config;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.List;

public class PettingConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // --- Taming Settings ---
    public static final ForgeConfigSpec.DoubleValue TAME_CHANCE;
    public static final ForgeConfigSpec.BooleanValue HEALTH_SCALES_TAMING_CHANCE;
    public static final ForgeConfigSpec.DoubleValue TAME_HEALTH_THRESHOLD;
    public static final ForgeConfigSpec.BooleanValue REQUIRE_KILL_TO_TAME;
    public static final ForgeConfigSpec.BooleanValue WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TAMING_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TAMING_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GOLDEN_WHEAT;
    public static final ForgeConfigSpec.BooleanValue DISABLE_RESPAWN_ON_TAME;

    // --- AI & Behavior Settings ---
    public static final ForgeConfigSpec.DoubleValue FOLLOW_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue BOUND_ROAM_RADIUS;
    public static final ForgeConfigSpec.BooleanValue SIT_HEAL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SIT_HEAL_AMOUNT;
    public static final ForgeConfigSpec.IntValue SIT_HEAL_INTERVAL;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GOAT_HORN_WHISTLE;
    public static final ForgeConfigSpec.BooleanValue WHISTLE_TELEPORTS_TETHERED;
    public static final ForgeConfigSpec.BooleanValue HIDE_TAMED_BOSSBARS;
    public static final ForgeConfigSpec.IntValue MAX_PETS_PER_PLAYER;

    // --- Pet Riding Settings ---
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_RIDING;
    public static final ForgeConfigSpec.BooleanValue MOUNT_REQUIRE_SADDLE;
    public static final ForgeConfigSpec.DoubleValue LAND_RIDING_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLYING_RIDING_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue RIDING_WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RIDING_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue RIDING_BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RIDING_BLACKLIST;

    // --- Pet Inventory Settings ---
    public static final ForgeConfigSpec.DoubleValue PET_PORTRAIT_RENDER_SCALE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_EQUIPPABLE_MOBS;
    public static final ForgeConfigSpec.BooleanValue INVENTORY_WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INVENTORY_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue INVENTORY_BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INVENTORY_BLACKLIST;

    // --- Item Interaction Settings ---
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_STATUS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_AGGRESSION;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_SELF_DEFENSE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_GUARD;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_FOLLOW_DIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_TELEPORT_DIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_WHISTLE_TOGGLE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_TETHERING;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_RELEASING;

    // --- Controls & Feedback Settings ---
    public static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLES;
    public static final ForgeConfigSpec.IntValue INTERACTION_COOLDOWN;
    public enum FeedbackStyle {
        ACTION_BAR,
        CHAT,
        NONE
    }
    public static final ForgeConfigSpec.EnumValue<FeedbackStyle> COMMAND_FEEDBACK_STYLE;

    // --- Custom Taming & Categories ---
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TAMING_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PET_CATEGORIES;

    static {
        BUILDER.push("Taming Settings");
        TAME_CHANCE = BUILDER.comment("Base chance (0.0 to 1.0) for a tame attempt to succeed. Example: 0.33 is ~33% chance.")
                .defineInRange("tameChance", 0.33, 0.0, 1.0);
        HEALTH_SCALES_TAMING_CHANCE = BUILDER.comment("If true, a mob's missing health increases tame success chance (weaker mobs are easier to tame).")
                .define("healthScalesTamingChance", false);
        TAME_HEALTH_THRESHOLD = BUILDER.comment("Required percentage of missing health before taming is allowed (0.0 to 1.0). Example: 0.5 means mob must be at 50% health.")
                .defineInRange("tameHealthThreshold", 0.0, 0.0, 1.0);
        REQUIRE_KILL_TO_TAME = BUILDER.comment("If true, the player must have killed at least one mob of that type before being allowed to tame it.")
                .define("requireKillToTame", false);
        WHITELIST_ONLY = BUILDER.comment("If true, only mobs listed in 'tamingWhitelist' can be tamed.")
                .define("whitelistOnly", false);
        TAMING_WHITELIST = BUILDER.comment("List of entity IDs allowed for taming. Example: [\"minecraft:zombie\", \"minecraft:creeper\"]")
                .defineListAllowEmpty("tamingWhitelist", List.of(), obj -> obj instanceof String);
        BLACKLIST_ENABLED = BUILDER.comment("If true, mobs listed in 'tamingBlacklist' cannot be tamed.")
                .define("blacklistEnabled", false);
        TAMING_BLACKLIST = BUILDER.comment("List of entity IDs forbidden from taming. Example: [\"minecraft:wither\"]")
                .defineListAllowEmpty("tamingBlacklist", List.of(), obj -> obj instanceof String);
        ALLOW_GOLDEN_WHEAT = BUILDER.comment("If true, the Golden Wheat item can be used to tame any tamable mob.")
                .define("allowGoldenWheat", true);
        DISABLE_RESPAWN_ON_TAME = BUILDER.comment("If true, tamed mobs keep their original NBT/Armor instead of being re-spawned (prevents losing gear).")
                .define("disableRespawnOnTame", true);
        BUILDER.pop();

        BUILDER.push("AI & Behavior Settings");
        FOLLOW_DISTANCE = BUILDER.comment("Distance (in blocks) the owner must be from the pet before it begins following.")
                .defineInRange("followDistance", 10.0, 1.0, 100.0);
        TELEPORT_DISTANCE = BUILDER.comment("Distance (in blocks) at which the pet will instantly teleport to the owner.")
                .defineInRange("teleportDistance", 20.0, 5.0, 200.0);
        BOUND_ROAM_RADIUS = BUILDER.comment("The radius (in blocks) a pet will wander around within the area it is bound to.")
                .defineInRange("boundRoamRadius", 10.0, 1.0, 100.0);
        SIT_HEAL_ENABLED = BUILDER.comment("If enabled, pets will slowly regenerate health while in the Sitting state.")
                .define("sitHealEnabled", true);
        SIT_HEAL_AMOUNT = BUILDER.comment("Amount of health (in half-hearts) healed per interval.")
                .defineInRange("sitHealAmount", 1.0, 0.0, 100.0);
        SIT_HEAL_INTERVAL = BUILDER.comment("Time (in ticks) between each heal while sitting. (20 ticks = 1 second).")
                .defineInRange("sitHealInterval", 40, 1, 1000000);
        ENABLE_GOAT_HORN_WHISTLE = BUILDER.comment("If true, using a Goat Horn while crouching teleports all your pets to you.")
                .define("enableGoatHornWhistle", true);
        WHISTLE_TELEPORTS_TETHERED = BUILDER.comment("If true, pets bound to an area will also teleport when you use a Goat Horn whistle.")
                .define("whistleTeleportsTethered", false);
        HIDE_TAMED_BOSSBARS = BUILDER.comment("If true, Boss Bars for entities like the Wither will be hidden once they are tamed.")
                .define("hideTamedBossBars", true);
        MAX_PETS_PER_PLAYER = BUILDER.comment("The base limit of pets a player can own. (-1 for infinite). Can be modified by attributes.")
                .defineInRange("maxPetsPerPlayer", -1, -1, 10000);
        BUILDER.pop();

        BUILDER.push("Pet Riding Settings");
        ALLOW_PET_RIDING = BUILDER.comment("If true, players can ride their pets by Shift + Right-Clicking with an empty hand.")
                .define("allowPetRiding", true);
        MOUNT_REQUIRE_SADDLE = BUILDER.comment("If true, you CANNOT ride a pet unless it has a Saddle in its custom inventory slot.")
                .define("mountRequireSaddle", false);
        LAND_RIDING_SPEED_MULTIPLIER = BUILDER.comment("Multiplier for movement speed while riding ground mobs. 1.0 is default.")
                .defineInRange("landRidingSpeedMultiplier", 1.0, 0.0, 10.0);
        FLYING_RIDING_SPEED_MULTIPLIER = BUILDER.comment("Multiplier for movement speed while riding flying mobs. 1.0 is default.")
                .defineInRange("flyingRidingSpeedMultiplier", 1.0, 0.0, 10.0);
        RIDING_WHITELIST_ONLY = BUILDER.comment("If true, only mobs in 'ridingWhitelist' can be ridden by players.")
                .define("ridingWhitelistOnly", false);
        RIDING_WHITELIST = BUILDER.comment("List of entity IDs specifically allowed to be ridden. Example: [\"minecraft:ravager\"]")
                .defineListAllowEmpty("ridingWhitelist", List.of(), obj -> obj instanceof String);
        RIDING_BLACKLIST_ENABLED = BUILDER.comment("If true, mobs listed in 'ridingBlacklist' cannot be ridden.")
                .define("ridingBlacklistEnabled", false);
        RIDING_BLACKLIST = BUILDER.comment("List of entity IDs forbidden from being ridden. Example: [\"minecraft:phantom\"]")
                .defineListAllowEmpty("ridingBlacklist", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("Pet Inventory Settings");
        PET_PORTRAIT_RENDER_SCALE = BUILDER.comment("Scales the size of the 3D model preview inside the Pet Inventory UI.")
                .defineInRange("petPortraitRenderScale", 45.0, 1.0, 500.0);
        EXTRA_EQUIPPABLE_MOBS = BUILDER.comment("Force specific mobs to always have Armor/Hand slots visible. Example: [\"minecraft:ghast\"]")
                .defineListAllowEmpty("extraEquippableMobs", List.of(), obj -> obj instanceof String);
        INVENTORY_WHITELIST_ONLY = BUILDER.comment("If enabled, only mobs in the 'inventoryWhitelist' will have equipment slots.")
                .define("inventoryWhitelistOnly", false);
        INVENTORY_WHITELIST = BUILDER.comment("List of entity IDs allowed to have a Pet Inventory. Example: [\"minecraft:skeleton\"]")
                .defineListAllowEmpty("inventoryWhitelist", List.of(), obj -> obj instanceof String);
        INVENTORY_BLACKLIST_ENABLED = BUILDER.comment("If enabled, mobs in the 'inventoryBlacklist' will have their inventories disabled.")
                .define("inventoryBlacklistEnabled", false);
        INVENTORY_BLACKLIST = BUILDER.comment("List of entity IDs forbidden from having a Pet Inventory. Example: [\"minecraft:creeper\"]")
                .defineListAllowEmpty("inventoryBlacklist", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        BUILDER.push("Item Interaction Settings");
        ALLOW_PER_PET_STATUS = BUILDER.comment("Allows viewing a pet's status by Right-Clicking it with a STICK.")
                .define("allowPerPetStatus", true);
        ALLOW_PER_PET_AGGRESSION = BUILDER.comment("Allows toggling Aggressive Mode by Right-Clicking with a SWORD.")
                .define("allowPerPetAggression", true);
        ALLOW_PER_PET_SELF_DEFENSE = BUILDER.comment("Allows toggling Self-Defense by Right-Clicking with a SHIELD.")
                .define("allowPerPetSelfDefense", true);
        ALLOW_PER_PET_GUARD = BUILDER.comment("Allows toggling Guard Mode by Right-Clicking with a COOKIE.")
                .define("allowPerPetGuard", true);
        ALLOW_PER_PET_FOLLOW_DIST = BUILDER.comment("Allows cycling follow distance by Right-Clicking with a LEAD.")
                .define("allowPerPetFollowDist", true);
        ALLOW_PER_PET_TELEPORT_DIST = BUILDER.comment("Allows cycling teleport triggers by Right-Clicking with an ENDER PEARL.")
                .define("allowPerPetTeleportDist", true);
        ALLOW_PER_PET_WHISTLE_TOGGLE = BUILDER.comment("Allows toggling whistle response by Right-Clicking with a CLOCK.")
                .define("allowPerPetWhistleToggle", true);
        ALLOW_PET_TETHERING = BUILDER.comment("Allows binding a pet to its current location using a PET TETHER.")
                .define("allowPetTethering", true);
        ALLOW_PET_RELEASING = BUILDER.comment("Allows releasing a pet to the wild via Crouch + Right-Click with SHEARS.")
                .define("allowPetReleasing", true);
        BUILDER.pop();

        BUILDER.push("Controls & Feedback Settings");
        ENABLE_PARTICLES = BUILDER.comment("Enables heart and effect particles during taming or petting interactions.")
                .define("enableParticles", true);
        INTERACTION_COOLDOWN = BUILDER.comment("Wait time (ticks) required before you can pet the same mob again.")
                .defineInRange("interactionCooldown", 20, 0, 1000000);
        COMMAND_FEEDBACK_STYLE = BUILDER.comment("Defines how the mod communicates state changes to you.",
                "Options: ACTION_BAR (text above hotbar), CHAT (normal chat), NONE (silent)")
                .defineEnum("commandFeedbackStyle", FeedbackStyle.ACTION_BAR);
        BUILDER.pop();

        BUILDER.push("Custom Taming & Categories");
        CUSTOM_TAMING_ITEMS = BUILDER.comment("Specify custom items for taming specific mobs.",
                "Format: entity_registry|item_registry. Example: [\"minecraft:zombie|minecraft:bone\"]")
                .defineListAllowEmpty("customTamingItems", List.of(), obj -> obj instanceof String);
        PET_CATEGORIES = BUILDER.comment("Group mobs into categories with shared limits.",
                "Format: SlotID|DisplayName|MobList|DefaultLimit",
                "Example: [\"1|Goblins|minecraft:zombie,minecraft:skeleton|5\"]")
                .defineListAllowEmpty("petCategories", List.of(), obj -> obj instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
