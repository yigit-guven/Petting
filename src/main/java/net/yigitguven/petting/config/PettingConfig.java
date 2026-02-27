package net.yigitguven.petting.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PettingConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_RESPAWN_ON_TAME;
    public static final ForgeConfigSpec.IntValue INTERACTION_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLES;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_RIDING;
    
    public static final ForgeConfigSpec.BooleanValue WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TAMING_WHITELIST;
    
    public static final ForgeConfigSpec.BooleanValue BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> TAMING_BLACKLIST;
    
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_TAMING_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> PET_CATEGORIES;
    public static final ForgeConfigSpec.BooleanValue ALLOW_GOLDEN_WHEAT;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_STATUS;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_AGGRESSION;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_SELF_DEFENSE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_GUARD;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_FOLLOW_DIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_TELEPORT_DIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PER_PET_WHISTLE_TOGGLE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_TETHERING;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_RELEASING;

    public static final ForgeConfigSpec.IntValue MAX_PETS_PER_PLAYER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GOAT_HORN_WHISTLE;
    public static final ForgeConfigSpec.BooleanValue WHISTLE_TELEPORTS_TETHERED;
    public static final ForgeConfigSpec.BooleanValue HIDE_TAMED_BOSSBARS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_EQUIPPABLE_MOBS;

    public static final ForgeConfigSpec.BooleanValue REQUIRE_KILL_TO_TAME;
    public static final ForgeConfigSpec.DoubleValue TAME_HEALTH_THRESHOLD;

    public static final ForgeConfigSpec.DoubleValue TAME_CHANCE;
    public static final ForgeConfigSpec.BooleanValue HEALTH_SCALES_TAMING_CHANCE;

    public static final ForgeConfigSpec.DoubleValue SIT_HEAL_AMOUNT;
    public static final ForgeConfigSpec.IntValue SIT_HEAL_INTERVAL;
    public static final ForgeConfigSpec.DoubleValue FOLLOW_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_DISTANCE;
    public static final ForgeConfigSpec.DoubleValue BOUND_ROAM_RADIUS;

    public enum ControlScheme {
        RIGHT_CLICK_SIT_SHIFT_WAIT,
        RIGHT_CLICK_CYCLE,
        SHIFT_RIGHT_CLICK_CYCLE
    }
    public static final ForgeConfigSpec.EnumValue<ControlScheme> CONTROL_SCHEME;

    public enum FeedbackStyle {
        ACTION_BAR,
        CHAT,
        NONE
    }
    public static final ForgeConfigSpec.EnumValue<FeedbackStyle> COMMAND_FEEDBACK_STYLE;

    static {
        BUILDER.push("General Settings");

        DISABLE_RESPAWN_ON_TAME = BUILDER
                .comment("If true, mobs will not respawn when tamed to preserve their NBT data (like vanilla armor and weapons), instead applying tags to the existing mob.")
                .define("disableRespawnOnTame", true);

        INTERACTION_COOLDOWN = BUILDER
                .comment("Cooldown in ticks before petting the same mob again.")
                .defineInRange("interactionCooldown", 20, 0, 1000000);

        ENABLE_PARTICLES = BUILDER
                .comment("If true, heart particles will appear when petting/taming.")
                .define("enableParticles", true);

        REQUIRE_KILL_TO_TAME = BUILDER
                .comment("If true, players must have killed at least one of the entity type before they are allowed to tame it.")
                .define("requireKillToTame", false);

        TAME_HEALTH_THRESHOLD = BUILDER
                .comment("Required percentage of missing health before taming works (0.0 to 1.0). e.g., 0.5 means the mob must be missing 50% of its max health.")
                .defineInRange("tameHealthThreshold", 0.0, 0.0, 1.0);

        TAME_CHANCE = BUILDER
                .comment("Base chance (0.0 to 1.0) for a tame attempt to succeed. (e.g., 0.33 means ~1 in 3 chance, like vanilla wolves).")
                .defineInRange("tameChance", 0.33, 0.0, 1.0);

        HEALTH_SCALES_TAMING_CHANCE = BUILDER
                .comment("If true, the mob's missing health percentage is added to the base tame chance, making weaker mobs easier to tame.")
                .define("healthScalesTamingChance", false);

        MAX_PETS_PER_PLAYER = BUILDER
                .comment("The BASE number of custom pets a player can tame. This is now a Player Attribute (petting:max_pets), so it can be modified per-player by other mods or commands. (-1 for infinite).")
                .defineInRange("maxPetsPerPlayer", -1, -1, 10000);

        ALLOW_GOLDEN_WHEAT = BUILDER
                .comment("If true, Golden Wheat can be used to tame mobs. If false, ONLY items defined in Custom Item Settings can be used.")
                .define("allowGoldenWheat", true);

        ENABLE_GOAT_HORN_WHISTLE = BUILDER
                .comment("If true, crouching and using a Goat Horn will teleport all of your tamed pets directly to your location.")
                .define("enableGoatHornWhistle", true);

        WHISTLE_TELEPORTS_TETHERED = BUILDER
                .comment("If true, tethered (bound) pets will also be teleported when using the Goat Horn. If false, tethered pets ignore the whistle.")
                .define("whistleTeleportsTethered", false);

        HIDE_TAMED_BOSSBARS = BUILDER
                .comment("If true, completely hides the Boss Bar UI across the server for all tamed Bosses (like Withers).")
                .define("hideTamedBossBars", true);

        ALLOW_PET_RIDING = BUILDER
                .comment("If true, owners can Shift + Right-Click their pet with an empty hand to ride it.")
                .define("allowPetRiding", true);

        BUILDER.pop();

        BUILDER.push("Inventory Settings");

        EXTRA_EQUIPPABLE_MOBS = BUILDER
                .comment("List of entity registry names that should ALWAYS show equipment slots (Armor/Hands), even if the mod doesn't automatically detect them as humanoid. Example: [\"minecraft:ghast\", \"minecraft:creeper\"]")
                .defineListAllowEmpty("extraEquippableMobs", List.of(), obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("Behavior & AI Settings");

        SIT_HEAL_AMOUNT = BUILDER
                .comment("Amount of health (in half-hearts) a sitting pet regenerates.")
                .defineInRange("sitHealAmount", 1.0, 0.0, 100.0);

        SIT_HEAL_INTERVAL = BUILDER
                .comment("Interval (in ticks) between each sitting health regeneration.")
                .defineInRange("sitHealInterval", 40, 1, 1000000);

        FOLLOW_DISTANCE = BUILDER
                .comment("Distance from owner before the pet starts walking to them.")
                .defineInRange("followDistance", 10.0, 1.0, 100.0);

        TELEPORT_DISTANCE = BUILDER
                .comment("Distance from owner before the pet forcibly teleports to them.")
                .defineInRange("teleportDistance", 20.0, 5.0, 200.0);

        BOUND_ROAM_RADIUS = BUILDER
                .comment("Radius (in blocks) the pet will roam around its bound coordinate.")
                .defineInRange("boundRoamRadius", 10.0, 1.0, 100.0);

        BUILDER.pop();

        BUILDER.push("Controls Settings");

        CONTROL_SCHEME = BUILDER
                .comment("Scheme for commanding pets.",
                        "RIGHT_CLICK_SIT_SHIFT_WAIT: Right-Click toggles Sitting, Shift-Right-Click toggles Waiting (Default)",
                        "RIGHT_CLICK_CYCLE: Right-Click cycles sequentially (Wander -> Sit -> Wait)",
                        "SHIFT_RIGHT_CLICK_CYCLE: Shift-Right-Click cycles sequentially (Wander -> Sit -> Wait)")
                .defineEnum("controlScheme", ControlScheme.RIGHT_CLICK_SIT_SHIFT_WAIT);

        COMMAND_FEEDBACK_STYLE = BUILDER
                .comment("How state changes (e.g. 'Spot is now Sitting') are communicated.",
                        "ACTION_BAR: Small text above the hotbar (Default)",
                        "CHAT: Standard chat message",
                        "NONE: Completely silent")
                .defineEnum("commandFeedbackStyle", FeedbackStyle.ACTION_BAR);

        BUILDER.pop();

        BUILDER.push("Item Interaction Settings");

        ALLOW_PER_PET_STATUS = BUILDER
                .comment("If true, owners can right-click their pet with a Stick to see a status report.")
                .define("allowPerPetStatus", true);

        ALLOW_PER_PET_AGGRESSION = BUILDER
                .comment("If true, owners can right-click their pet with a Sword to toggle Aggressive Mode.")
                .define("allowPerPetAggression", true);

        ALLOW_PER_PET_SELF_DEFENSE = BUILDER
                .comment("If true, owners can right-click their pet with a Shield to toggle Self-Defense retaliation.")
                .define("allowPerPetSelfDefense", true);

        ALLOW_PER_PET_GUARD = BUILDER
                .comment("If true, owners can right-click their pet with a Cookie to toggle Guard Mode.")
                .define("allowPerPetGuard", true);

        ALLOW_PER_PET_FOLLOW_DIST = BUILDER
                .comment("If true, owners can right-click their pet with a Lead to cycle follow distance settings.")
                .define("allowPerPetFollowDist", true);

        ALLOW_PER_PET_TELEPORT_DIST = BUILDER
                .comment("If true, owners can right-click their pet with an Ender Pearl to cycle teleport distance settings.")
                .define("allowPerPetTeleportDist", true);

        ALLOW_PER_PET_WHISTLE_TOGGLE = BUILDER
                .comment("If true, owners can right-click their pet with a Clock to toggle if it responds to Goat Horn whistles.")
                .define("allowPerPetWhistleToggle", true);

        ALLOW_PET_TETHERING = BUILDER
                .comment("If true, owners can right-click their pet with a Pet Tether item to bind them to an area.")
                .define("allowPetTethering", true);

        ALLOW_PET_RELEASING = BUILDER
                .comment("If true, owners can Crouch + Right-Click their pet with Shears to release them.")
                .define("allowPetReleasing", true);

        BUILDER.pop();

        BUILDER.push("Whitelist Settings");

        WHITELIST_ONLY = BUILDER
                .comment("If true, only mobs listed in 'tamingWhitelist' can be tamed.")
                .define("whitelistOnly", false);

        TAMING_WHITELIST = BUILDER
                .comment("List of entity registry names that are allowed to be tamed. (e.g. [\"minecraft:zombie\" , \"minecraft:creeper\"])")
                .defineListAllowEmpty("tamingWhitelist", List.of(), obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("Blacklist Settings");

        BLACKLIST_ENABLED = BUILDER
                .comment("If true, mobs listed in 'tamingBlacklist' CANNOT be tamed. (Overrides Whitelist)")
                .define("blacklistEnabled", false);

        TAMING_BLACKLIST = BUILDER
                .comment("List of entity registry names that are forbidden from taming. (e.g. [\"minecraft:wither\"])")
                .defineListAllowEmpty("tamingBlacklist", List.of(), obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("Custom Item Settings");

        CUSTOM_TAMING_ITEMS = BUILDER
                .comment("Map custom taming items to specific mobs. Format: entity_registry|item_registry. Example: [\"minecraft:zombie|minecraft:bone\"]")
                .defineListAllowEmpty("customTamingItems", List.of(), obj -> obj instanceof String);

        PET_CATEGORIES = BUILDER
                .comment("Define pet categories with per-player attribute limits.",
                        "Format: SlotID|DisplayName|MobList|DefaultLimit",
                        "SlotID: 1 to 20 (corresponds to petting:max_pets_category_X)",
                        "DisplayName: Name shown in-game (e.g. Necromancer)",
                        "MobList: Comma-separated entity IDs (e.g. minecraft:zombie,minecraft:skeleton)",
                        "DefaultLimit: Starting limit for that category. Attributes can override this.",
                        "Example: [\"1|Necromancer|minecraft:zombie,minecraft:skeleton|3\"]")
                .defineListAllowEmpty("petCategories", List.of(), obj -> obj instanceof String);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}

