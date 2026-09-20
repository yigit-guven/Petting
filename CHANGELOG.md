# Changelog

All notable changes to this project will be documented in this file.

## [5.0.0]
### Added
- Ported mod to Minecraft 26.3 and NeoForge.
- Modernized mod architecture and resource pipeline.
- Added comprehensive update.json for Forge and NeoForge update checker.

## [4.2.3-beta.4-forge-1.20.1] - 2026-06-25

## Bug Fixes:
- **Interaction Fix**: Fixed a bug introduced in 4.2.3-beta.3 where tamed mobs, especially those with custom GUIs or on the GUI blacklist, became completely non-interactable with an empty hand due to aggressive event cancellation.

## Performance Optimizations:
- **Tick Handlers Optimization**: Reduced the execution frequency of heavy tick handlers (Warden Darkness suppression, Golden Wheat temptation, and Pet Attribute updates). These now run periodically (e.g., every 10-20 ticks) instead of every single tick, drastically reducing server CPU overhead.
- **Temptation Memory Leak Fix**: Golden Wheat temptation now properly cleans up `TemptedByGoldenWheat` tags and AI states when an animal wanders out of range or the player stops holding wheat.

## Tweaks:
- **Sitting Visuals**: Pets now simulate the "sneaking" state when sitting, causing their nametags to appear slightly darker/translucent and less intrusive through walls, providing better visual feedback that they are waiting.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.3-beta.3...4.2.3-beta.4-forge-1.20.1

---

## [4.2.3-beta.3] - 2026-06-23

## Bug Fix:
- **Crash Fix**: Resolved a critical crash caused by an `ObfuscationReflectionHelper` mapping mismatch (`java.lang.NoSuchFieldException: jumping`) when running the mod in a production environment.

---

## [4.2.3-beta.2] - 2026-06-02

## Changelog

### Fixed
- Fixed a multiplayer issue where opening Pet Settings for one player could also open the settings screen for other nearby players, including spectators.
- Fixed Pet Settings opening from normal pet-settings synchronization events (such as right-click interactions and per-pet tool actions).
- Fixed owner scoping for Pet Settings open responses so the UI is now sent only to the requesting owner.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.3-beta1...4.2.3-beta.2

---

## [4.2.3-beta1] - 2026-05-31

## Changelog

### Added / Fixed
- Added a complete pet settings and key mapping UI system, including pet settings, control mappings, and command-input screens where there was no UI in the previous release.
- Fixed right-click owner pet mapping handling so custom `control_right_click` and `control_shift_right_click` rules now override legacy sit-toggle behavior reliably.
- Ensured owned-pet right-click mappings always use a default fallback (`SIT|NONE` for right-click, `CYCLE|NONE` for shift-right-click) when no per-pet or player default mapping exists.
- Added event priority to `PlayerRightclicksEntitzProcedure` so the new mapping handler runs before legacy pet interaction logic.
- Prevented the legacy `OwnerRightclicksPetProcedure` from processing events already cancelled by the mapping handler.
- Added `RUN_COMMAND` support in key mapping rules and implemented `CommandInputScreen` so command strings can be entered, stored, and executed.
- Fixed key mapping UI behavior to avoid stale mapping data and improved screen navigation when editing RUN_COMMAND rules.
- Fixed pet settings toggles and follow/teleport distance updates to send correct server payloads and sync properly.
- Fixed goat horn whistle handling so it now teleports pets when enabled, without requiring shift-click.
- Fixed golden wheat taming logic so failed tame attempts no longer update mob names or change the client-side entity display.
- Updated legacy tool-based pet boolean toggles to use server-side `EntitySettingsServer.applyUpdate` for proper validation and client sync.
- Ensured `ignoreWhistle` is correctly respected by pet whistle teleport logic.
- Added permission-aware `RUN_COMMAND` execution so pets only run embedded commands if the owner actually has permission to execute that command.
- Fixed blacklist handling for taming by default enabling pet blacklist support and making blacklisted entities fully transparent to Petting interactions, preventing Create package duplication glitches.
- Fixed bee death loop when bound to a pet bed after stinging an enemy; the bed save now resets the bee's stinger state so it no longer immediately re-triggers death.
- Fixed pet bed not working across dimensions; the bed's dimension is now stored on binding and the death handler teleports the pet into the correct dimension via dimension transition.
- Enhanced blacklist system to support whole mod namespaces (e.g. `"create"`), namespace wildcards (e.g. `"create:*"`), and prefix wildcards (e.g. `"create:mechanical*"`) for taming, riding, and inventory blacklists; updated config comments to document all supported formats.
- Fixed tamed creeper pets not respawning at their pet bed after exploding; the game's explosion sequence calls `discard()` directly and bypasses the normal death event, so the fix detects swelling pet creepers each tick and aborts the explosion by resetting their swell direction.
- Improved pet persistence protection: tamed pets are now marked as `PersistenceRequired` immediately when they join or load into a level (via `EntityJoinLevelEvent`), rather than waiting for the first game tick, preventing edge cases where the game could despawn them before the tick handler ran.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.7...4.2.3-beta1

---

## [4.2.2] - 2026-04-19

- **Critical Crash**: Resolved a `NullPointerException` when opening the Creative Inventory caused by a registration race condition between blocks and items.
- **Improved Stability**: Added defensive checks to the creative tab to ensure the game doesn't crash if an item fails to register for any reason.
- **Internal**: Reordered mod registry initialization to follow Forge best practices for block-item dependency.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.1...4.2.2

---

## [4.2.1] - 2026-04-16

- **Pet Bed Block**: A fully functional custom block for your pets!
    - **Pet Binding**: Right-click a Pet Bed to enter "Binding Mode," then right-click your pet to link them to that bed.
    - **Respawn System**: Bound pets will now respawn at their bed upon death, fully healed and safely sitting.
    - **Custom 3D Model**: Realistic pet bed design with proper paw-print transparency support.
- **Friendly Fire Toggle**: Added `allowOwnerToHurtPets` to the config, allowing you to choose whether owners can damage their own pets.
- **Air-Click Cancellation**: Added the ability to cancel "Binding Mode" simply by right-clicking the air.
- **Interaction Priority**: Binding now correctly overrides default pet behaviors like sitting or standing.
- **Ghost Inventory Issues**: Fixed several desync bugs where right-clicking with blocks in hand would consume items without placing them.
- **Respawn AI Persistence**: Fixed a bug where pets would lose their "Following" or "Attacking" AI after respawning at a bed.
- Added 3D swimming movement support for ridden aquatic pets (Space to swim up, S to swim down).

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.0...4.2.1

---

## [4.2.1-beta4] - 2026-04-08

- Fixed `NoSuchFieldError: CREATIVE_MODE_TAB` by switching to more robust registry key referencing in `PettingModTabs.java`.
- Verified and fixed `PoseStack` rendering imbalance in `PetInventoryScreen.java` by validating 1.20.1-compatible signatures.
- Implemented `hideTamedBossBars` config option to correctly hide boss bars for tamed pets.
- Fixed an issue where boss bars would reappear after a relog by implementing a tamed status synchronization system.
- Improved overall mod stability during startup and screen transitions.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.1-beta3...4.2.1-beta4

---

## [4.2.1-beta3] - 2026-04-07

- **Fixed Mod Loading Crash**: Resolved an issue where an extra `pop()` call in `PettingConfig` caused an `IllegalArgumentException` during initialization.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.1-beta2...4.2.1-beta3

---

## [4.2.1-beta2] - 2026-04-07

- **Configurable Pet Armor**: Added `petBaseArmor` and `petBaseArmorToughness` to allow pets to have increased defense even without wearing armor.
- **Forced Equipment Slots**: Added `alwaysShowEquipmentSlots` config option which, when enabled, allows any tamed mob to use Armor and Hand slots in their inventory UI.
- Implemented a transient attribute modifier system for pets that updates in real-time as configuration changes.
- Improved `PetInventoryUtil` to support forced inventory layouts.
- Centralized pet attribute updates within the entity tick procedure for better reliability.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.1-beta...4.2.1-beta2

---

## [4.2.1-beta] - 2026-04-06

- Free Roam (Wandering) Mode: Pets can now be set to roam freely without following you. This mode bypasses the "Follow/Teleport to owner" logic while still allowing the pet to defend itself and respond to your attacks (if configured).
- Mode Cycle Update: Interacting with a Stick now cycles through: Following → Sitting → Waisting → Wandering.
- Clearer Labels: Renamed the previous generic "Wandering" label to "Following" for better clarity.
- Blacklist Interaction Block: Added a check at the start of the interaction handler (OwnerRightclicksPetProcedure.java) that immediately stops Petting from processing any action on blacklisted mobs. This ensures that even if you own a "Hybrid Pet" (like a vanilla Wolf), Petting will completely ignore it and let the original mod's interactions work or remain vanilla.
- Implemented the Global Blacklist system, which allows you to completely disable the Petting mod for specific entities. This ensures that complex entities are entirely ignored by the mod's AI, inventory, and interaction systems.
- Bound Roam Radius Fix: The pet was snapping back to its bound spot prematurely because of a hardcoded 20-block limit (400.0 squared).
- Improved the boss bar hiding logic.
- Implemented a Configurable Tool ID system that allows you to remap all of the mod's interaction items (like the Stick, Sword, or Shield) to any other item registry ID.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.2.0...4.2.1-beta

---

## [4.2.0] - 2026-04-05

<img width="854" height="449" alt="HFJqtLRaUAAjkd_" src="https://github.com/user-attachments/assets/08e3b362-3d09-4773-8801-447c8f6b5393" />

- **Special Attacks while Riding**: Players can now left-click while riding an Ender Dragon to fire projectiles (Dragon Fireballs).
- **Mount Protection (Anti-Kick)**: Players can no longer accidentally attack (kick) the pet they are currently riding.
- **Improved Damage Prevention**: Added comprehensive protection logic to prevent pets from damaging their owners and to shield both from their own special attack explosions.
- **Warden Darkness Suppression**: The Darkness effect is now automatically removed for players near their owned tamed Wardens.
- **Manual Flying Mobs Config**: Added `manualFlyingMobs` configuration to allow users to manually enable 3D flight controls for any other compatible mobs.
- **Robust Entity Identification**: Updated both client and server logic to use Registry IDs for identifying boss mobs, ensuring consistent behavior across all environments.
- **Taming Improvements**: Optimized the taming process to ensure all boss-level NBT data is correctly injected and synchronized.
- **PartEntity Redirection**: Interaction with multi-part entities (like the Ender Dragon) is now correctly redirected to the main entity, enabling taming and mounting on any part of the body.
- **Sit State Synchronization**: Fixed a bug where a pet's sitting state would occasionally desync when interactin with other items.
- **Interaction Catch-all**: Narrowed the interaction logic to prevent the mod from overriding vanilla behavior for other edible items.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.8...4.2.0

---

## [4.2.0-beta] - 2026-02-27

- Reworked pet interaction system with smarter empty-hand right-click behavior (Stand → Mount if saddled → Sit).
- Shift + Right-Click now consistently opens pet inventory.
- Introduced Command Stick as the dedicated AI controller.
- Stick now cycles AI modes: Wander → Sit → Wait.
- Added Action Bar feedback displaying current AI mode.
- Shift + Stick now provides a full pet status report.
- Introduced Pet Riding system with optional saddle requirement.
- Added allowPetRiding configuration toggle.
- Added mountRequireSaddle configuration option.
- Balanced default flying riding speed to 1.0x for more natural control.
- Added persistent Pet Inventory (Saddle slot, Armor slot, 14 storage slots).
- Press E while riding to access pet inventory.
- Improved shift-click behavior and manual item placement logic.
- Added hand slot ghost icons (Sword/Shield) for better UI clarity.
- Implemented Universal Smart Slot system using reflection for dynamic mob equipment detection.
- Added barrier icons and interaction locks for unsupported equipment slots.
- Added extraEquippableMobs configuration list for manual slot overrides.
- Fully reorganized petting-common.toml into structured categories.
- Added detailed comments, valid options, and usage examples to all config entries.
- Added sitHealEnabled option to control sitting regeneration.
- Added petPortraitRenderScale option for inventory UI scaling.
- Removed deprecated controlScheme system.
- Fixed dedicated server crash caused by client-only class loading.
- Isolated client/server registrations to prevent future dedicated server issues.
- Fixed startup crash related to pet data capability initialization.
- Fixed inventory desync issues under lag conditions.
- Fixed mounted flying speed inconsistencies.
- Fixed AI state updates not syncing correctly during name tag interactions.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.8...4.2.0-beta

---

## [4.1.8] - 2026-02-25

- Fixed tamed Piglins and Hoglins losing their pet status and behavior settings after converting to Zombified versions in the overworld.
- Added data transfer logic for entity conversion using `LivingConversionEvent.Post`.
- Updated build.gradle and resource properties to version 4.1.8.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.5...4.1.8

---

## [4.1.7] - 2026-02-23

- Refine Wither pet projectile handling to allow legitimate attacks: instead of forcefully killing all skulls fired by tamed Wither.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.6...4.1.7

---

## [4.1.6] - 2026-02-23

- Fixed a server-side crash during mod loading (`ClassNotFoundException: Screen`). This was caused by client-only GUI registration in the mod constructor. The fix now properly isolates client-side code in a separate registration class.
- Fixed all crafting recipes (Pet Tether, Golden Wheat, etc.) failing to load on 1.21.1 due to the new `result.id` JSON format requirements.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.5...4.1.6

---

## [4.1.5] - 2026-02-23

- Updated version to 4.1.5,
- Fixed an issue where tamed Wardens and other modern Brain-based mobs would continue to attack the player or other improper targets,
- Fixed an issue where tamed Wardens would not engage targets when commanded by their owner.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.4...4.1.5

---

## [4.1.4] - 2026-02-22

- Updated mod version to 4.1.4,
- Fixed server-side crash caused by client-only code in mod constructor (`ClassNotFoundException: Screen`),
- Added system to reset pet names to default upon release if the name was automatically generated (preserves manually set names),
- Added a pixel art texture for the `Pet Tether`,
- Created a new `Teleport Orb` item to cycle pet teleport distances, replacing the Ender Pearl,
- Created a new `Follow Whistle` item to cycle pet follow distances, replacing the vanilla Lead,
- Added descriptive tooltips to all custom mod items (`Golden Wheat`, `Pet Tether`, `Teleport Orb`, `Follow Whistle`),
- Balanced and updated custom item recipes,
- Fixed item interaction conflict: Preventing consumable/usable items from being used when right-clicking pets to change settings.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.3...4.1.4

---

## [4.1.3] - 2026-02-20

- **Pet Tether Recipe**: Added a crafting recipe for the Pet Tether (1 Lead, 4 Iron Nuggets).

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.2...4.1.3

---

## [4.1.2] - 2026-02-20

- **Shears (Crouch)**: **Release your pet** back into the wild, clearing all ownership data.
- **Item Protection**: Prevented items like Ender Pearls, Cookies, and Clocks from being consumed or thrown when used to customize pet settings.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.1...4.1.2

---

## [4.1.1] - 2026-02-20

- **Per-Pet Item Interactions**: Customize each pet individually by right-clicking them with specific items:
  - **Stick**: Ray-casts a full Status Report of the pet's current AI settings.
  - **Sword**: Toggles **Aggressive Mode** (Will attack mobs that the owner attacks).
  - **Shield**: Toggles **Self-Defense** (Will retaliate if damaged).
  - **Cookie**: Toggles **Guard Mode** (Will protect the owner if they are attacked).
  - **Lead**: Cycles through **Follow Distances** (5, 10, 20, 50 blocks).
  - **Ender Pearl**: Cycles through **Teleport Distances** (10, 20, 50, 100 blocks).
  - **Clock**: Toggles **Whistle Response** (Force ignore or force respond to whistles).
- **Item Interaction Configs**: Added server-side toggles for every item interaction, allowing server owners to restrict per-pet customization.
- **Pet Categories**: Introduced 20 per-player attribute slots (`petting:max_pets_category_1-20`) and a new `petCategories` config list for complex, group-based pet limits.
- **Max Pet Attribute**: Migrated the global Max Pet limit to a per-player Attribute (`petting:max_pets`), enabling dynamic limit scaling.
- Refined the **Pet Tether** interaction to be integrated with the new item-based customization system.
- Updated the Goat Horn Whistle to respect per-pet whistle overrides.

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.1.0...4.1.1

---

## [4.1.0] - 2026-02-20

- Migrated the entire codebase from MCreator to a standard Forge 1.20.1 MDK workspace for better stability and performance,
- Implemented a robust Configuration system accessible via a clickable "Config" button in the Mod Menu,
- Developed a new Pet State system with Wandering, Sitting (health regeneration), and Waiting (guard mode) behaviors,
- Added a high-utility Pet Whistle feature using the Goat Horn to instantly summon all your pets,
- Hybridized Vanilla Taming so wolves and cats now gain all Petting features (teleportation, fall immunity, etc.) upon taming,
- Integrated "Universal Pacifism" logic to stop native wolves and other modded entities from attacking your custom pets,
- Overhauled taming with an RNG success chance system and optional "Health Scaling" (weaker mobs are easier to tame),
- Implemented Taming Protection including HP thresholds and a "Require Kill" config to prevent taming creatures you haven't defeated,
- Grief-proofed the Wither Boss by neutralizing stray projectiles and added a feature to automatically hide its intrusive Boss Bar,
- Granted all tamed pets passive immunity to Fall, Fire, and Lava damage to prevent common environmental deaths,

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.0.04...4.1.0

---

## [4.0.04] - 2026-02-04

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.0.02...4.0.04

---

## [4.0.03] - 2025-12-19

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.0.01...4.0.03

---

## [4.0.02] - 2025-12-19

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/4.0.01...4.0.02

---

## [4.0.01] - 2025-12-06

**Full Changelog**: https://github.com/yigit-guven/Petting/compare/3.0.1...4.0.01

---

## [3.0.1] - 2025-06-29

### Added
- Death and respawn handling for tamed pets
- Async cleanup system to reduce server load
- Periodic registry maintenance improvements

### Changed
- Enhanced target selection logic for special mobs
- Improved Iron Golem defense mechanics to correctly protect owners
- Improved owner tracking after player death

### Fixed
- Wither, Ender Dragon, and Warden autonomous attack behavior
- Creeper explosion behavior when tamed
- Pet persistence issues after player relog
- Dead pet cleanup in `pets.json` registry

---

*This project adheres to [Semantic Versioning](https://semver.org/).*

---


