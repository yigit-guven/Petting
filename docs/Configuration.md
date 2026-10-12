# Configuration Guide

Welcome to the **Petting** configuration guide. This page covers all available configuration options, where configuration files are located, and how to configure them in-game or via text files.

---

## Accessing Configuration

Petting offers two ways to customize configuration settings:

### 1. In-Game Configuration Screen
1. Open the pause menu or main menu and select **Mods**.
2. Locate and click on **Petting** in the mods list.
3. Click the **Config** button.
4. From this screen, you can access the **Client** and **Server** configuration menus.

### 2. Configuration Files
- **Client Configuration**: Located at `.minecraft/config/petting-client.toml`. Applies locally to your client.
- **Server Configuration**: Located at `.minecraft/config/petting-server.toml` (default template) or `.minecraft/saves/<WorldName>/serverconfig/petting-server.toml` (per-world settings). In multiplayer, server settings are managed by the server host.

---

## Server Settings (`petting-server.toml`)

These settings control server-wide mechanics, taming thresholds, and success rates.

### Section: `[taming]`

| Setting | Type | Default | Range | Description |
| :--- | :--- | :--- | :--- | :--- |
| `maxHealthPercentage` | Double | `30.0` | `0.0` – `100.0` | The maximum health percentage a mob must be at or below to be eligible for taming. |
| `hostileMobsOnly` | Boolean | `true` | `true` / `false` | When `true`, only hostile mobs (`Enemy`) must be weakened to the health threshold. When `false`, all mobs must be weakened before taming. |
| `unsuccessfulTamingChance` | Double | `33.3` | `0.0` – `100.0` | The percentage chance that a taming attempt fails and consumes the treat. Default `33.3` gives a 2/3 (66.7%) success rate per attempt. Set to `0.0` for 100% guaranteed success. |
| `untameableEntities` | List<String> | `[]` | Any entity ID | A list of entity registry IDs (e.g. `["minecraft:warden", "modid:boss_name"]`) that can never be tamed. |
| `maxPetCount` | Integer | `-1` | `-1` – `2147483647` | The maximum number of pets a player can own at a time. Set to `-1` for unlimited pets. |

#### Setting Details:

- **`maxHealthPercentage`**:
  Specifies the health percentage threshold required before a mob can be tamed.
  *Example*: At `30.0`, a mob with 20 HP must be weakened to 6 HP or lower before it can be tamed.

- **`hostileMobsOnly`**:
  Controls whether peaceful and neutral mobs also require weakening.
  - `true` (*Default*): Only hostile entities require health weakening. Passive and neutral mobs can be tamed at full health.
  - `false`: All mobs must be weakened below `maxHealthPercentage` before taming.

- **`unsuccessfulTamingChance`**:
  Controls the failure rate of taming attempts.
  - `33.3` (*Default*): 33.3% chance of failure (66.7% success rate) per treat.
  - `0.0`: Guaranteed success on every attempt.
  - `100.0`: Disables taming via treats.

- **`untameableEntities`**:
  Blacklist specific mobs from ever being tamed.
  *Example*: `untameableEntities = ["minecraft:warden", "minecraft:ender_dragon"]`

- **`maxPetCount`**:
  Controls how many pets a single player can own simultaneously.
  - `-1` (*Default*): Unlimited pets.
  - `0`: Players cannot tame any pets.
  - `1+`: Players can tame up to this number of pets. If a player attempts to tame another mob after reaching their limit, angry villager particles are displayed, the treat is not consumed, and taming is prevented.

### Section: `[friendlyFire]`

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `ownerCanAttackPets` | Boolean | `true` | When `true`, players can attack and damage their own pets. When `false`, friendly fire from the owner to their pets is prevented. |
| `petsCanAttackPets` | Boolean | `false` | When `true`, pets belonging to the same owner can attack and damage each other. When `false`, friendly fire between pets of the same owner is prevented. |

#### Setting Details:

- **`ownerCanAttackPets`**:
  Controls whether players can attack and damage their own pets.
  - `true` (*Default*): Owners can hit and damage their own pets. Note that pets will still never attack or target their owner under any circumstances.
  - `false`: Pets are completely immune to damage caused by their owner.

- **`petsCanAttackPets`**:
  Controls whether pets belonging to the same owner can attack and damage each other.
  - `false` (*Default*): Friendly fire between pets of the same owner is completely disabled (pets cannot target or damage each other).
  - `true`: Pets of the same owner can target and damage each other.

### Section: `[behavior]`

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `disableBuiltInHostility` | Boolean | `true` | When `true`, removes built-in mob aggression and targeting. Pets will strictly attack according to their collar tactical settings. |
| `enablePetInventory` | Boolean | `true` | When `true`, pet inventory, collar equipment, and pet riding are enabled. When `false`, pet inventory is completely disabled: pets follow default settings, cannot wear collars or equipment, and cannot be ridden. |
| `enableCollarSlot` | Boolean | `true` | When `true`, all pet inventory GUIs provide a dedicated slot to equip collars. When `false`, the collar slot is removed from all pet GUIs. |
| `enablePetSitting` | Boolean | `true` | When `true`, players can command owned pets to sit or stand using the configured keybind (default: Right-Click). |
| `forgiveAttacksOnSit` | Boolean | `true` | When `true`, commanding a pet to sit clears its attack targets and calms active hostilities, immediately halting combat. |
| `flyingPetsHoverWhenSitting` | Boolean | `true` | When `true`, flying pets commanded to sit will hover stationary in mid-air instead of falling to the ground. |
| `swimmingPetsStayStillWhenSitting` | Boolean | `true` | When `true`, aquatic pets commanded to sit will remain stationary in water without floating up, sinking, or drifting. |
| `hideTamedBossBars` | Boolean | `true` | When `true`, boss health bars for tamed pets (such as Wither, Ender Dragon, and other boss pets) are hidden from the screen. |
| `tamedMonstersPreventSleep` | Boolean | `false` | When `true`, tamed monster pets (such as Zombies, Skeletons, Withers, etc.) nearby will prevent players from sleeping like vanilla monsters do. When `false` (default), players can sleep peacefully near their friendly tamed pets. |
| `enablePetDeathMessages` | Boolean | `true` | When `true`, players receive a chat notification when their pet dies or respawns at a bound bed/cushion. When `false`, death and respawn notifications are disabled on the server. |

#### Setting Details:

- **`disableBuiltInHostility`**:
  Controls whether pets retain natural mob-type aggression or strictly obey their collar behavioral settings.
  - `true` (*Default*): Built-in mob targeting is completely disabled (Iron Golems will not attack monsters, Wolves will not hunt sheep/skeletons, Withers will not target everything, etc.). Pets will strictly follow their configured tactical rules.
  - `false`: Pets retain their original mob AI targeting goals.

- **`enablePetInventory`**:
  Controls whether the pet inventory, equipment, and riding system is enabled.
  - `true` (*Default*): Full pet inventory GUI is accessible, collars and gear can be equipped, and allowed pets can be ridden.
  - `false`: Completely disables the pet inventory feature. Newly and existing tamed pets operate strictly on default configuration settings (`defaultCombatMode`, collar defaults). Pets cannot wear collars, saddles, or items, pet inventory GUIs cannot be opened, and attempting to ride a pet does nothing.

- **`enableCollarSlot`**:
  Controls whether the universal collar slot is enabled in pet inventory GUIs.
  - `true` (*Default*): All pet inventory GUIs display a dedicated collar equipment slot.
  - `false`: Removes the collar equipment slot from all pet inventory GUIs.

- **`enablePetSitting`**:
  Controls whether the universal sitting mechanic is enabled for pets.
  - `true` (*Default*): Players can command owned pets to sit or stand using the **Sit / Stand Pet** keybind (default: Right-Click).
  - `false`: Sitting mechanic is disabled.

- **`forgiveAttacksOnSit`**:
  Controls whether sitting immediately halts ongoing combat.
  - `true` (*Default*): Ordering a pet to sit immediately clears its attack targets, forgets entity anger, and stops all attack goals just like a tamed wolf in vanilla Minecraft.
  - `false`: Pets do not clear targets when ordered to sit.

- **`flyingPetsHoverWhenSitting`**:
  Controls physical suspension for flying pets (e.g. Allays, Ghasts, Phantoms, Bats, Bees, Parrots, Blazes, Withers, Vexes) while sitting.
  - `true` (*Default*): Flying pets hover completely stationary in mid-air right where they are commanded to sit, without losing altitude or falling.
  - `false`: Flying pets fall to the ground subject to gravity.

- **`swimmingPetsStayStillWhenSitting`**:
  Controls stillness for aquatic pets (and pets in water) while sitting.
  - `true` (*Default*): Swimming pets remain completely stationary in water without sinking, floating to the surface, or being swept away by water currents.
  - `false`: Normal fluid physics and water currents apply while sitting.

- **`hideTamedBossBars`**:
  Controls the display of boss health bars for tamed pets on players' screens.
  - `true` (*Default*): Boss health bars for tamed pets (e.g. Wither, Ender Dragon, or other boss pets) are hidden from the screen, preventing HUD clutter.
  - `false`: Boss health bars are displayed normally for all boss entities even when tamed.

- **`tamedMonstersPreventSleep`**:
  Controls whether tamed monster pets (such as Zombies, Skeletons, Withers, etc.) nearby prevent players from sleeping with the vanilla "monsters nearby" error.
  - `false` (*Default*): Tamed monsters do not prevent players from sleeping, allowing players to rest peacefully near their pets.
  - `true`: Tamed monsters prevent players from resting just like wild monsters in vanilla Minecraft.

- **`enablePetDeathMessages`**:
  Controls whether death and respawn notifications are sent to the pet's owner from the server.
  - `true` (*Default*): The server sends a chat notification to the owner whenever their pet dies permanently, respawns at a bound bed/cushion, or fails to respawn because the bed/cushion was missing.
  - `false`: Death and respawn notifications are not sent by the server.

### Section: `[riding]`

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enablePetRiding` | Boolean | `true` | Controls whether pet riding mechanics and the Riding button in the pet inventory GUI are enabled. |
| `useWhitelist` | Boolean | `false` | When `true`, only entities in `whitelist` can be ridden. When `false`, all entities except those in `blacklist` can be ridden. |
| `whitelist` | List<String> | `[]` | List of entity registry IDs (e.g. `["minecraft:creeper"]`) permitted for riding when `useWhitelist` is `true`. |
| `blacklist` | List<String> | `[]` | List of entity registry IDs (e.g. `["minecraft:bat"]`) blocked from riding when `useWhitelist` is `false`. |

### Section: `[collar]`

This section manages server-wide defaults and slider limit bounds for collar settings configured through the Collar tab in Pet Inventory GUIs.

| Setting | Type | Default | Range | Description |
| :--- | :--- | :--- | :--- | :--- |
| `defaultCombatMode` | Enum | `AGGRESSIVE` | `AGGRESSIVE`, `DEFENSIVE`, `PASSIVE` | The default combat mode assigned to new collars and newly tamed pets. |
| `defaultFollowDistance` | Integer | `4` | `1` – `32` | The default follow distance in blocks assigned to new collars. |
| `minFollowDistance` | Integer | `2` | `1` – `16` | The minimum follow distance selectable on collar sliders. |
| `maxFollowDistance` | Integer | `32` | `4` – `64` | The maximum follow distance selectable on collar sliders. |
| `defaultTeleportDistance` | Integer | `20` | `5` – `128` | The default teleport distance in blocks assigned to new collars. |
| `minTeleportDistance` | Integer | `8` | `5` – `32` | The minimum teleport distance selectable on collar sliders. |
| `maxTeleportDistance` | Integer | `64` | `16` – `200` | The maximum teleport distance selectable on collar sliders. |
| `defaultTeleportEnabled` | Boolean | `true` | `true` / `false` | Whether teleporting to the owner is enabled by default on new collars. |
| `defaultPreferRangedAttack` | Boolean | `true` | `true` / `false` | Whether pets equipped with a collar prefer ranged attacks first when holding a bow or crossbow. |
| `defaultDefendOtherPets` | Boolean | `true` | `true` / `false` | Whether pets defend other pets of the same owner when attacked. |
| `defaultRetaliateWhenHurt` | Boolean | `true` | `true` / `false` | Whether pets retaliate against attackers who directly damage the pet. |
| `enableCollarRespawn` | Boolean | `true` | `true` / `false` | When `true`, right-clicking any bed or cushion with a collar enchants and binds it. Pets wearing this collar will respawn on that bed or cushion upon dying, unless the bed or cushion is broken. |

#### Setting Details:

- **`enableCollarRespawn`**:
  Controls whether the collar binding and respawn mechanics are enabled.
  - `true` (*Default*): Players can right-click any bed or cushion with a collar to bind it (granting the visual enchantment glint). Pets wearing this bound collar will safely respawn at the bed or cushion on death, as long as the bed or cushion is intact. If the bed or cushion is broken, the pet dies normally.
  - `false`: Completely disables collar binding and pet bed/cushion respawn.

---

## Client Settings (`petting-client.toml`)

These settings control client-side interface behavior and chat feedback.

### Section: `[feedback]`

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enableTamedSuccessFeedback` | Boolean | `false` | Whether to display notifications in chat when successfully taming a mob (`petting.action.tamed_success`). |
| `enablePetHealedFeedback` | Boolean | `false` | Whether to display notifications in chat when healing a pet (`petting.action.pet_healed`). |
| `enablePetSittingFeedback` | Boolean | `false` | Whether to display notifications in chat when commanding a pet to sit (`petting.action.pet_sitting`). |
| `enablePetStandingFeedback` | Boolean | `false` | Whether to display notifications in chat when commanding a pet to stand and follow (`petting.action.pet_standing`). |
| `enableCollarBoundFeedback` | Boolean | `false` | Whether to display notifications in chat when binding a collar to a bed or cushion (`petting.action.collar_bound`). |
| `enableMountControlledFeedback` | Boolean | `false` | Whether to display steering control notifications in chat when mounting a saddled pet (`petting.action.mount_controlled`). |
| `enableTamingFailedFeedback` | Boolean | `false` | Whether to display notifications in chat when a taming attempt fails (`petting.action.taming_failed`). |
| `enableTooHealthyFeedback` | Boolean | `false` | Whether to display warnings in chat when a mob has too much health to be tamed (`petting.action.too_healthy`). |
| `enableAlreadyVanillaTamedFeedback` | Boolean | `false` | Whether to display warnings in chat when attempting to tame a mob already bonded through vanilla taming (`petting.action.already_vanilla_tamed`). |
| `enableAlreadyTamedOtherFeedback` | Boolean | `false` | Whether to display warnings in chat when attempting to tame a mob that belongs to another player (`petting.action.already_tamed_other`). |
| `enablePetFullHealthFeedback` | Boolean | `false` | Whether to display messages in chat when attempting to heal a pet already at full health (`petting.action.pet_full_health`). |
| `enablePetLimitReachedFeedback` | Boolean | `false` | Whether to display warnings in chat when your pet limit has been reached (`petting.action.pet_limit_reached`). |
| `enableSaddleRequiredFeedback` | Boolean | `false` | Whether to display warnings in chat when attempting to mount a pet without a required saddle (`petting.action.saddle_required`). |
| `enableMountUncontrolledFeedback` | Boolean | `false` | Whether to display warnings in chat when mounting a pet without steering control (`petting.action.mount_uncontrolled`). |
| `enablePetDeathMessages` | Boolean | `false` | Master toggle: Whether to display all pet death and respawn notification messages in chat. |
| `enablePetDiedFeedback` | Boolean | `false` | Whether to display notifications in chat when your pet dies (`petting.action.pet_died`). |
| `enablePetRespawnedFeedback` | Boolean | `false` | Whether to display notifications in chat when your pet respawns at a bound bed or cushion (`petting.action.pet_respawned`). |
| `enablePetRespawnFailedFeedback` | Boolean | `false` | Whether to display notifications in chat when your pet fails to respawn because its bed or cushion was missing (`petting.action.pet_respawn_failed`). |

#### Setting Details:

> [!NOTE]
> All feedback messages are disabled (`false`) by default to keep chat clean. You can selectively enable whichever individual messages you want to see.

- **Positive Feedback Options**:
  - Granular toggles (`enableTamedSuccessFeedback`, `enablePetHealedFeedback`, `enablePetSittingFeedback`, `enablePetStandingFeedback`, `enableCollarBoundFeedback`, `enableMountControlledFeedback`) let you selectively enable specific positive notifications.

- **Negative Feedback Options**:
  - Granular toggles (`enableTamingFailedFeedback`, `enableTooHealthyFeedback`, `enableAlreadyVanillaTamedFeedback`, `enableAlreadyTamedOtherFeedback`, `enablePetFullHealthFeedback`, `enablePetLimitReachedFeedback`, `enableSaddleRequiredFeedback`, `enableMountUncontrolledFeedback`) let you selectively enable specific warnings and failure notifications.

- **Pet Death & Respawn Options**:
  - `enablePetDeathMessages`: Master toggle to show all pet death and collar respawn notifications.
  - Granular toggles (`enablePetDiedFeedback`, `enablePetRespawnedFeedback`, `enablePetRespawnFailedFeedback`) let you selectively enable permanent death, respawn, or failed respawn messages.



---

## Customizing Mob Treat Mappings

Looking to change which treat tames which mob? See the [Customizing Taming Treats Guide](https://github.com/yigit-guven/Petting/wiki/Custom-Treat-Tags) to learn how to add or reassign mobs using Data Packs or KubeJS.