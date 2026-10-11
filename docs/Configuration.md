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
| `disableBuiltInHostility` | Boolean | `true` | When `true`, removes built-in mob aggression and targeting. Pets only attack like tamed wolves (to protect owner, attack owner's target, or defend themselves). |
| `defaultCombatMode` | Enum | `AGGRESSIVE` | The default combat mode assigned to newly tamed pets (`AGGRESSIVE`, `DEFENSIVE`, `PASSIVE`). |
| `petsDefendOwner` | Boolean | `true` | When `true`, pets will automatically attack and defend against mobs that damage their owner. |
| `petsAttackOwnerTarget` | Boolean | `true` | When `true`, pets will automatically attack mobs that their owner damages. |
| `petsRetaliate` | Boolean | `true` | When `true`, pets will retaliate and attack mobs that damage the pet itself. |
| `teleportDistance` | Double | `20.0` | Distance in blocks before a following pet attempts to teleport to its owner (`5.0` – `200.0`). |
| `enableCollarSlot` | Boolean | `true` | When `true`, all pet inventory GUIs provide a dedicated slot to equip collars. When `false`, the collar slot is removed from all pet GUIs. |

#### Setting Details:

- **`disableBuiltInHostility`**:
  Controls whether pets retain natural mob-type aggression or strictly behave like tamed wolves.
  - `true` (*Default*): Built-in mob targeting is completely disabled (Iron Golems will not attack monsters, Wolves will not hunt sheep/skeletons, Withers will not target everything, etc.). Pets will only attack to defend their owner, attack targets their owner hits, or defend themselves if attacked.
  - `false`: Pets retain their original mob AI targeting goals.

- **`defaultCombatMode`**:
  Controls the default combat mode assigned when a mob is tamed.
  - `AGGRESSIVE` (*Default*): The pet attacks targets the owner attacks, defends the owner when hurt, and retaliates when hurt.
  - `DEFENSIVE`: The pet only defends the owner and retaliates when hurt; it will not initiate attacks when the owner attacks.
  - `PASSIVE`: The pet will never engage in combat.

- **`petsDefendOwner`**:
  Controls whether pets defend the owner when the owner takes damage from another mob.
  - `true` (*Default*): Pets immediately attack any hostile entity that harms their owner.
  - `false`: Pets will not react to their owner taking damage.

- **`petsAttackOwnerTarget`**:
  Controls whether pets attack whatever mob the owner strikes.
  - `true` (*Default*): Pets attack whatever mob the owner damages.
  - `false`: Pets will not assist when the owner initiates an attack.

- **`petsRetaliate`**:
  Controls self-defense for pets.
  - `true` (*Default*): Pets fight back when struck by an attacker.
  - `false`: Pets will not retaliate when damaged.

- **`teleportDistance`**:
  Controls how far away an owner can get before the following pet teleports to catch up.
  - `20.0` (*Default*): 20 blocks. While engaged in active combat, pets will prioritize fighting rather than following, unless the owner exceeds this distance.

- **`enableCollarSlot`**:
  Controls whether the universal collar slot is enabled in pet inventory GUIs.
  - `true` (*Default*): All pet inventory GUIs display a dedicated collar equipment slot.
  - `false`: Removes the collar equipment slot from all pet inventory GUIs.

---

## Client Settings (`petting-client.toml`)

These settings control client-side interface behavior and chat feedback.

### Section: `[feedback]`

| Setting | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `enablePositiveFeedback` | Boolean | `false` | Whether to display positive feedback messages in chat (e.g., successful taming or pet healing). |
| `enableNegativeFeedback` | Boolean | `false` | Whether to display negative feedback and warning messages in chat (e.g., failure notifications, mob too healthy, or already tamed). |

#### Setting Details:

- **`enablePositiveFeedback`**:
  When enabled (`true`), chat notifications appear upon successfully taming or healing a pet. Disabled (`false`) by default to keep chat clear.

- **`enableNegativeFeedback`**:
  When enabled (`true`), warning and status messages appear in chat if a taming attempt fails, if a mob is too healthy to be tamed, or if a mob already has an owner. Disabled (`false`) by default.

---

## Customizing Mob Treat Mappings

Looking to change which treat tames which mob? See the [Customizing Taming Treats Guide](https://github.com/yigit-guven/Petting/wiki/Custom-Treat-Tags) to learn how to add or reassign mobs using Data Packs or KubeJS.