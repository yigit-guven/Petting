package net.yigitguven.petting.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class PettingConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue DISABLE_RESPAWN_ON_TAME;
    public static final ForgeConfigSpec.IntValue INTERACTION_COOLDOWN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_PARTICLES;
    
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

    public static final ForgeConfigSpec.BooleanValue REQUIRE_KILL_TO_TAME;
    public static final ForgeConfigSpec.DoubleValue TAME_HEALTH_THRESHOLD;

    public static final ForgeConfigSpec.DoubleValue TAME_CHANCE;
    public static final ForgeConfigSpec.BooleanValue HEALTH_SCALES_TAMING_CHANCE;

    public static final ForgeConfigSpec.BooleanValue SIT_HEAL_ENABLED;
    public static final ForgeConfigSpec.DoubleValue SIT_HEAL_AMOUNT;
    public static final ForgeConfigSpec.IntValue SIT_HEAL_INTERVAL;
        public static final ForgeConfigSpec.DoubleValue FOLLOW_DISTANCE;
        public static final ForgeConfigSpec.DoubleValue TELEPORT_DISTANCE;
        // Expose configured clamp ranges for use at runtime (will be initialized in static block)
        public static final int FOLLOW_DISTANCE_MIN;
        public static final int FOLLOW_DISTANCE_MAX;
        public static final int TELEPORT_DISTANCE_MIN;
        public static final int TELEPORT_DISTANCE_MAX;
    public static final ForgeConfigSpec.DoubleValue BOUND_ROAM_RADIUS;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MANUAL_FLYING_MOBS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MANUAL_SWIMMING_MOBS;
    public static final ForgeConfigSpec.BooleanValue PREVENT_PET_TO_OWNER_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue ALLOW_OWNER_TO_HURT_PETS;

    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_RIDING;
    public static final ForgeConfigSpec.BooleanValue MOUNT_REQUIRE_SADDLE;
    public static final ForgeConfigSpec.DoubleValue LAND_RIDING_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue FLYING_RIDING_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue SWIMMING_RIDING_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue RIDING_WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RIDING_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue RIDING_BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> RIDING_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue ALLOW_PET_ATTACK_WHILE_RIDING;

    public static final ForgeConfigSpec.DoubleValue PET_BASE_ARMOR;
    public static final ForgeConfigSpec.DoubleValue PET_BASE_ARMOR_TOUGHNESS;

    public static final ForgeConfigSpec.DoubleValue PET_PORTRAIT_RENDER_SCALE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> EXTRA_EQUIPPABLE_MOBS;
    public static final ForgeConfigSpec.BooleanValue ALWAYS_SHOW_EQUIPMENT_SLOTS;
    public static final ForgeConfigSpec.BooleanValue INVENTORY_WHITELIST_ONLY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INVENTORY_WHITELIST;
    public static final ForgeConfigSpec.BooleanValue INVENTORY_BLACKLIST_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> INVENTORY_BLACKLIST;

    public static final ForgeConfigSpec.ConfigValue<String> STATUS_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> AGGRESSION_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> DEFENSE_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> GUARD_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> FOLLOW_DIST_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> TELEPORT_DIST_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> WHISTLE_RESPONSE_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> TETHER_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> RELEASE_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> GLOBAL_WHISTLE_TOOL_ID;
    public static final ForgeConfigSpec.ConfigValue<String> TAMING_ITEM_ID;

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

        BUILDER.pop();

        BUILDER.push("Behavior & AI Settings");

        SIT_HEAL_ENABLED = BUILDER
                .comment("If true, sitting pets will slowly regenerate health.")
                .define("sitHealEnabled", true);

        SIT_HEAL_AMOUNT = BUILDER
                .comment("Amount of health (in half-hearts) a sitting pet regenerates.")
                .defineInRange("sitHealAmount", 1.0, 0.0, 100.0);

        SIT_HEAL_INTERVAL = BUILDER
                .comment("Interval (in ticks) between each sitting health regeneration.")
                .defineInRange("sitHealInterval", 40, 1, 1000000);

        int minFollow = 1;
        int maxFollow = 100;
        FOLLOW_DISTANCE = BUILDER
                .comment("Distance from owner before the pet starts walking to them.")
                .defineInRange("followDistance", 10.0, (double) minFollow, (double) maxFollow);
        FOLLOW_DISTANCE_MIN = minFollow;
        FOLLOW_DISTANCE_MAX = maxFollow;

        int minTeleport = 5;
        int maxTeleport = 200;
        TELEPORT_DISTANCE = BUILDER
                .comment("Distance from owner before the pet forcibly teleports to them.")
                .defineInRange("teleportDistance", 20.0, (double) minTeleport, (double) maxTeleport);
        TELEPORT_DISTANCE_MIN = minTeleport;
        TELEPORT_DISTANCE_MAX = maxTeleport;

        BOUND_ROAM_RADIUS = BUILDER
                .comment("Radius (in blocks) the pet will roam around its bound coordinate.")
                .defineInRange("boundRoamRadius", 10.0, 1.0, 100.0);

        MANUAL_FLYING_MOBS = BUILDER
                .comment("List of entity ids to manually treat as flying mobs for 3D flight control.")
                .defineListAllowEmpty("manualFlyingMobs", List.of(), obj -> obj instanceof String);

        MANUAL_SWIMMING_MOBS = BUILDER
                .comment("List of entity ids to manually treat as swimming mobs for 3D swimming control.")
                .defineListAllowEmpty("manualSwimmingMobs", List.of(), obj -> obj instanceof String);

        PREVENT_PET_TO_OWNER_DAMAGE = BUILDER
                .comment("If true, pets cannot damage their owners.")
                .define("preventPetToOwnerDamage", true);

        ALLOW_OWNER_TO_HURT_PETS = BUILDER
                .comment("If false, owners cannot damage their own pets.")
                .define("allowOwnerToHurtPets", false);

        BUILDER.pop();

        BUILDER.push("Pet Riding Settings");

        ALLOW_PET_RIDING = BUILDER
                .comment("If true, players can ride their pets.")
                .define("allowPetRiding", true);

        MOUNT_REQUIRE_SADDLE = BUILDER
                .comment("If true, a saddle in the pet inventory is required to ride.")
                .define("mountRequireSaddle", false);

        LAND_RIDING_SPEED_MULTIPLIER = BUILDER
                .comment("Speed multiplier while riding ground pets.")
                .defineInRange("landRidingSpeedMultiplier", 1.0, 0.0, 10.0);

        FLYING_RIDING_SPEED_MULTIPLIER = BUILDER
                .comment("Speed multiplier while riding flying pets.")
                .defineInRange("flyingRidingSpeedMultiplier", 1.0, 0.0, 10.0);

        SWIMMING_RIDING_SPEED_MULTIPLIER = BUILDER
                .comment("Speed multiplier while riding swimming pets.")
                .defineInRange("swimmingRidingSpeedMultiplier", 1.0, 0.0, 10.0);

        RIDING_WHITELIST_ONLY = BUILDER
                .comment("If true, only whitelisted mobs can be ridden.")
                .define("ridingWhitelistOnly", false);

        RIDING_WHITELIST = BUILDER
                .defineListAllowEmpty("ridingWhitelist", List.of(), obj -> obj instanceof String);

        RIDING_BLACKLIST_ENABLED = BUILDER
                .comment("If true, mobs listed in 'ridingBlacklist' cannot be ridden.")
                .define("ridingBlacklistEnabled", false);

        RIDING_BLACKLIST = BUILDER
                .comment(
                    "Mobs that cannot be ridden. Supports exact IDs, whole mod namespaces, and wildcards.",
                    "Examples: \"create\", \"minecraft:ravager\", \"create:mechanical*\"",
                    "Syntax: ridingBlacklist = [\"entry1\", \"entry2\", ...]")
                .defineListAllowEmpty("ridingBlacklist", List.of(), obj -> obj instanceof String);

        ALLOW_PET_ATTACK_WHILE_RIDING = BUILDER
                .comment("If true, pets can perform attacks (like fireballs) while being ridden via left-click.")
                .define("allowPetAttackWhileRiding", true);

        BUILDER.pop();

        BUILDER.push("Pet Stat Settings");

        PET_BASE_ARMOR = BUILDER
                .defineInRange("petBaseArmor", 0.0, 0.0, 100.0);

        PET_BASE_ARMOR_TOUGHNESS = BUILDER
                .defineInRange("petBaseArmorToughness", 0.0, 0.0, 100.0);

        BUILDER.pop();

        BUILDER.push("Pet Inventory Settings");

        PET_PORTRAIT_RENDER_SCALE = BUILDER
                .defineInRange("petPortraitRenderScale", 45.0, 1.0, 500.0);

        EXTRA_EQUIPPABLE_MOBS = BUILDER
                .comment("Force these mobs to show armor/hand slots.")
                .defineListAllowEmpty("extraEquippableMobs", List.of(), obj -> obj instanceof String);

        ALWAYS_SHOW_EQUIPMENT_SLOTS = BUILDER
                .define("alwaysShowEquipmentSlots", false);

        INVENTORY_WHITELIST_ONLY = BUILDER
                .define("inventoryWhitelistOnly", false);

        INVENTORY_WHITELIST = BUILDER
                .defineListAllowEmpty("inventoryWhitelist", List.of(), obj -> obj instanceof String);

        INVENTORY_BLACKLIST_ENABLED = BUILDER
                .comment("If true, mobs listed in 'inventoryBlacklist' will not have the pet inventory UI.")
                .define("inventoryBlacklistEnabled", false);

        INVENTORY_BLACKLIST = BUILDER
                .comment(
                    "Mobs that cannot have a pet inventory. Supports exact IDs, whole mod namespaces, and wildcards.",
                    "Examples: \"create\", \"minecraft:blaze\", \"alexsmobs:*\"",
                    "Syntax: inventoryBlacklist = [\"entry1\", \"entry2\", ...]")
                .defineListAllowEmpty("inventoryBlacklist", List.of(), obj -> obj instanceof String);

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

        BUILDER.push("Tool Settings");
        STATUS_TOOL_ID = BUILDER.comment("Item ID for Status/AI Mode cycling. Default: minecraft:stick")
                .define("statusTool", "minecraft:stick");
        AGGRESSION_TOOL_ID = BUILDER.comment("Item ID for Aggressive Mode toggling. Default: minecraft:iron_sword")
                .define("aggressionTool", "minecraft:iron_sword");
        DEFENSE_TOOL_ID = BUILDER.comment("Item ID for Self-Defense toggling. Default: minecraft:shield")
                .define("defenseTool", "minecraft:shield");
        GUARD_TOOL_ID = BUILDER.comment("Item ID for Guard Mode toggling. Default: minecraft:cookie")
                .define("guardTool", "minecraft:cookie");
        FOLLOW_DIST_TOOL_ID = BUILDER.comment("Item ID for Follow Distance cycling. Default: petting:follow_whistle")
                .define("followDistTool", "petting:follow_whistle");
        TELEPORT_DIST_TOOL_ID = BUILDER.comment("Item ID for Teleport Distance cycling. Default: petting:teleport_orb")
                .define("teleportDistTool", "petting:teleport_orb");
        WHISTLE_RESPONSE_TOOL_ID = BUILDER.comment("Item ID for Whistle Response toggling. Default: minecraft:clock")
                .define("whistleTool", "minecraft:clock");
        TETHER_TOOL_ID = BUILDER.comment("Item ID for Tethering/Binding. Default: petting:pet_tether")
                .define("tetherTool", "petting:pet_tether");
        RELEASE_TOOL_ID = BUILDER.comment("Item ID for Releasing pets. Default: minecraft:shears")
                .define("releaseTool", "minecraft:shears");
        GLOBAL_WHISTLE_TOOL_ID = BUILDER.comment("Item ID for the Global Follow Whistle. Default: minecraft:goat_horn")
                .define("globalWhistleTool", "minecraft:goat_horn");
        TAMING_ITEM_ID = BUILDER.comment("Item ID for the primary Taming Item. Default: petting:golden_wheat")
                .define("tamingItem", "petting:golden_wheat");
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
                .define("blacklistEnabled", true);

        TAMING_BLACKLIST = BUILDER
                .comment(
                    "List of entities or entire mods that CANNOT be tamed.",
                    "Each entry is a string in the list. Supported formats:",
                    "  Exact entity:   \"minecraft:wither\"   (blocks only that entity)",
                    "  Whole mod:      \"create\"              (blocks ALL entities from mod 'create')",
                    "  Mod wildcard:   \"create:*\"            (same as above, explicit wildcard)",
                    "  Prefix:         \"create:mechanical*\"  (blocks any entity whose ID starts with 'create:mechanical')",
                    "Syntax: tamingBlacklist = [\"entry1\", \"entry2\", ...]",
                    "Example: tamingBlacklist = [\"create\", \"minecraft:wither\", \"alexsmobs:void_worm\"]")
                .defineListAllowEmpty("tamingBlacklist", List.of(), obj -> obj instanceof String);

        BUILDER.pop();

        BUILDER.push("Custom Taming Settings");

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
