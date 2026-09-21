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

Looking to change which treat tames which mob? See the [Customizing Taming Treats Guide](Custom-Treat-Tags.md) to learn how to add or reassign mobs using Data Packs or KubeJS.