# Configuration

**Petting** provides extensive configuration options split between **Server / Synced Configuration** (`petting-synced.toml`, world/server-synced) and **Client Configuration** (`petting-client.toml`, player-specific).

You can edit these files using a text editor or via the in-game configuration screen accessible from the **Mods** menu.

---

## Server / Synced Configuration (`petting-synced.toml`)

Server settings govern gameplay rules, taming mechanics, combat AI, and physics across a world or dedicated server. Values are authoritative on the server and automatically synced to connected clients. In singleplayer, this file is located in your world save directory at:  
`.minecraft/saves/<WorldName>/serverconfig/petting-synced.toml`

### Taming Settings (`[taming]`)

| Option | Type | Default | Range / Format | Description |
|---|---|:---:|:---:|---|
| `maxHealthPercentage` | double | `30.0` | `0.0` to `100.0` | The maximum health percentage a mob must be at or below to be tamed. Set to `100.0` to remove the weakening requirement entirely. |
| `hostileMobsOnly` | boolean | `true` | `true` / `false` | When `true`, only hostile mobs must be weakened below `maxHealthPercentage`. Passive animals can be tamed at full health. When `false`, all mobs must be weakened. |
| `unsuccessfulTamingChance` | double | `33.3` | `0.0` to `100.0` | The percentage chance that a taming attempt fails and consumes the treat without taming the mob. Set to `0.0` for 100% taming success. |
| `untameableEntities` | list of strings | `[]` | `["modid:entity", ...]` | Registry IDs of mobs that can never be tamed (e.g. `["minecraft:warden", "modid:special_boss"]`). |
| `maxPetCount` | int | `-1` | `-1` to `2147483647` | The maximum number of pets a player can own at once. Set to `-1` for unlimited pets. |

### Friendly Fire Settings (`[friendlyFire]`)

| Option | Type | Default | Description |
|---|---|:---:|---|
| `ownerCanAttackPets` | boolean | `true` | When `true`, players can attack and damage their own pets. When `false`, pets are 100% immune to damage from their owner. |
| `petsCanAttackPets` | boolean | `false` | When `true`, pets belonging to the same owner can attack each other. When `false`, friendly fire between fellow pets is cancelled. |

### Behavior Settings (`[behavior]`)

| Option | Type | Default | Description |
|---|---|:---:|---|
| `disableBuiltInHostility` | boolean | `true` | When `true`, pets will not naturally attack mobs based on native mob type (e.g. Iron Golems attacking zombies, or Withers blasting all life). Pets will strictly obey their collar combat mode. |
| `enablePetInventory` | boolean | `true` | When `true`, pets have an inventory GUI, can wear collars and equipment, and can be ridden. When `false`, the inventory, collar, and custom riding systems are completely disabled. |
| `enableCollarSlot` | boolean | `true` | When `true`, the collar slot is available in all pet inventory GUIs. |
| `enablePetSitting` | boolean | `true` | When `true`, players can command pets to sit or follow. |
| `forgiveAttacksOnSit` | boolean | `true` | When `true`, commanding a pet to sit immediately clears its attack targets and calms active hostilities. |
| `flyingPetsHoverWhenSitting` | boolean | `true` | When `true`, flying pets commanded to sit will hover stationary in mid-air instead of falling. |
| `swimmingPetsStayStillWhenSitting` | boolean | `true` | When `true`, aquatic pets commanded to sit will stay stationary in water without floating or sinking. |
| `hideTamedBossBars` | boolean | `true` | When `true`, boss health bars for tamed pets (Wither, Ender Dragon, etc.) are hidden from the screen. |
| `tamedMonstersPreventSleep` | boolean | `false` | When `false`, players can sleep peacefully near their friendly tamed monsters. When `true`, nearby monster pets prevent sleeping. |
| `enablePetDeathMessages` | boolean | `true` | When `true`, the server dispatches chat notifications to owners when their pet dies or respawns. |
| `enableEggConvertedFeedback` | boolean | `false` | Server-side toggle controlling whether the server announces Dragon Egg conversion. |

### Riding Settings (`[riding]`)

| Option | Type | Default | Range | Description |
|---|---|:---:|:---:|---|
| `enablePetRiding` | boolean | `true` | `true`/`false` | When `true`, allowed pets can be ridden and show the Riding tab. |
| `useWhitelist` | boolean | `false` | `true`/`false` | When `true`, only entity types listed in `whitelist` can be ridden. When `false`, all mobs are ridable except those in `blacklist`. |
| `whitelist` | list of strings | `[]` | IDs | Entity registry IDs that can be ridden when `useWhitelist = true`. |
| `blacklist` | list of strings | `[]` | IDs | Entity registry IDs that cannot be ridden when `useWhitelist = false`. |
| `allowRidingWithoutSaddle` | boolean | `true` | `true`/`false` | When `true`, players can mount pets without a saddle (uncontrolled). When `false`, mounting requires an equipped saddle. |
| `riddenPetsAttackOnClick` | boolean | `true` | `true`/`false` | When `true`, pressing your attack key while riding makes the pet execute an attack. |
| `flyingPetAscentSpeed` | double | `0.35` | `0.05` to `2.0` | Vertical speed when ascending (Jump) on a flying pet. |
| `flyingPetDescentSpeed` | double | `0.35` | `0.05` to `2.0` | Vertical speed when descending (Sneak) on a flying pet. |
| `flyingPetSpeedMultiplier` | double | `1.0` | `0.1` to `5.0` | Horizontal speed multiplier for flying pets. |
| `swimmingPetSpeedMultiplier` | double | `1.0` | `0.1` to `5.0` | Swimming speed multiplier for aquatic pets. |
| `riddenAttackCooldownTicks` | int | `10` | `1` to `60` | Minimum ticks between attacks performed by ridden pets (20 ticks = 1 second). |

### Collar Settings (`[collar]`)

| Option | Type | Default | Range | Description |
|---|---|:---:|:---:|---|
| `defaultCombatMode` | enum | `AGGRESSIVE` | `PASSIVE`, `DEFENSIVE`, `AGGRESSIVE`, `WANDER`, `GUARD` | Default combat mode assigned to new collars and newly tamed pets. |
| `defaultFollowDistance` | int | `4` | `1` to `32` | Default follow distance in blocks before a pet pathfinds toward its owner. |
| `minFollowDistance` | int | `2` | `1` to `16` | Minimum follow distance selectable on collar sliders. |
| `maxFollowDistance` | int | `32` | `4` to `64` | Maximum follow distance selectable on collar sliders. |
| `defaultTeleportDistance` | int | `20` | `5` to `128` | Default distance in blocks before a following pet teleports to owner. |
| `minTeleportDistance` | int | `8` | `5` to `32` | Minimum teleport distance selectable on collar sliders. |
| `maxTeleportDistance` | int | `64` | `16` to `200` | Maximum teleport distance selectable on collar sliders. |
| `defaultTeleportEnabled` | boolean | `true` | `true`/`false` | Whether teleporting is enabled by default on new collars. |
| `defaultPreferRangedAttack` | boolean | `true` | `true`/`false` | Whether pets prefer ranged attacks when holding a bow by default. |
| `defaultDefendOtherPets` | boolean | `true` | `true`/`false` | Whether pets defend fellow pets by default. |
| `defaultRetaliateWhenHurt` | boolean | `true` | `true`/`false` | Whether pets fight back when damaged by default. |
| `enableCollarRespawn` | boolean | `true` | `true`/`false` | When `true`, right-clicking a bed or cushion binds a collar, enabling pet respawning upon death. |
| `enableCollarModels` | boolean | `true` | `true`/`false` | When `true`, collars worn by pets are visually rendered in-world on their models. When `false`, collar models are hidden server-wide for all players. |

---

## Client Configuration (`petting-client.toml`)

Client configuration options are stored per player client in:  
`.minecraft/config/petting-client.toml`

### Feedback Settings (`[feedback]`)
Petting includes 19 client-side feedback toggles (all default to `false`) allowing individual players to opt in to specific chat notifications without spam:

| Client Option | Default | Target Notification Description |
|---|:---:|---|
| `enableTamedSuccessFeedback` | `false` | Displays chat notification when successfully taming a mob (`You tamed %s!`). |
| `enablePetHealedFeedback` | `false` | Displays chat notification when healing a pet (`%s was healed!`). |
| `enablePetSittingFeedback` | `false` | Displays notification when commanding a pet to sit (`%s is now sitting.`). |
| `enablePetStandingFeedback` | `false` | Displays notification when commanding a pet to follow (`%s is now following.`). |
| `enableCollarBoundFeedback` | `false` | Displays notification when binding a collar to a bed or cushion (`Collar bound to %s at [%d, %d, %d].`). |
| `enableMountControlledFeedback` | `false` | Displays steering control notification when mounting a saddled pet. |
| `enableTamingFailedFeedback` | `false` | Displays warning when a taming attempt fails (`Taming attempt failed!`). |
| `enableTooHealthyFeedback` | `false` | Displays warning when a mob is too healthy to tame (`This mob is too healthy to tame! Weaken it below %d%% health first.`). |
| `enableAlreadyVanillaTamedFeedback` | `false` | Displays warning when attempting to tame a mob already bonded through vanilla taming. |
| `enableAlreadyTamedOtherFeedback` | `false` | Displays warning when attempting to tame a mob belonging to someone else. |
| `enablePetFullHealthFeedback` | `false` | Displays message when trying to heal a pet already at full health. |
| `enablePetLimitReachedFeedback` | `false` | Displays warning when your pet limit has been reached (`You have reached your pet limit (%s)!`). |
| `enableSaddleRequiredFeedback` | `false` | Displays warning when attempting to mount a pet without a required saddle. |
| `enableMountUncontrolledFeedback` | `false` | Displays warning when mounting a pet without steering control. |
| `enablePetDeathMessages` | `false` | Displays all pet death and respawn notifications in chat. |
| `enablePetDiedFeedback` | `false` | Displays notification when your pet dies (`Your pet %s died!`). |
| `enablePetRespawnedFeedback` | `false` | Displays notification when your pet respawns at a bound bed/cushion. |
| `enablePetRespawnFailedFeedback` | `false` | Displays notification when your pet fails to respawn because its bed/cushion was missing. |
| `enableEggConvertedFeedback` | `false` | Displays notification when converting a Dragon Egg into a Golden Dragon Egg. |

### Visual Settings (`[visuals]`)

| Client Option | Default | Description |
|---|:---:|---|
| `enableCollarModels` | `true` | When `true`, collar models are visible on pets for this client. When `false`, this client will not see collar models on any pets. Note: If the server disables collar models (`enableCollarModels = false` in `petting-synced.toml`), collar models will remain hidden regardless of this client setting. |

---

*Related Pages*: [Taming](Taming) · [Pet Behavior](Pet-Behavior) · [Riding](Riding) · [Chat Messages](Chat-Messages)