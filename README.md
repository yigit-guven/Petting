<div align="center">
  <h1><img src="https://raw.githubusercontent.com/yigit-guven/Petting/refs/heads/1.20.1-Forge/src/main/resources/logo.png" width="32" height="32" alt="Petting Logo" style="vertical-align: middle;"> Petting</h1>
  <p><i>Turn any mob into your loyal, protective companion!</i></p>

  [![CurseForge Downloads](https://img.shields.io/curseforge/dt/1083170?style=flat-square&logo=curseforge&label=CurseForge&color=orange)](https://www.curseforge.com/minecraft/mc-mods/petting) [![Modrinth Downloads](https://img.shields.io/modrinth/dt/petting?style=flat-square&logo=modrinth&label=Modrinth)](https://modrinth.com/mod/petting) [![Discord](https://img.shields.io/discord/1440563800930652235?style=flat-square&logo=discord&label=Discord)](https://discord.gg/aPk7Qs5d4H)

  <p>
    <a href="https://github.com/yigit-guven/Petting"><b>GitHub</b></a> •
    <a href="https://github.com/yigit-guven/Petting/wiki"><b>Wiki</b></a> •
    <a href="https://github.com/yigit-guven/Petting/issues"><b>Issues</b></a> •
    <a href="https://discord.gg/aPk7Qs5d4H"><b>Discord</b></a>
  </p>
</div>

---

**Petting** is a mob-taming mod for Minecraft that allows players to tame various entities. Tamed mobs can guard locations, carry items, and follow the player.

---

## 📖 Table of Contents
1. [🤝 Core Mechanics: Taming](#core-mechanics-taming)
2. [🕹️ Smart Controls & Movement](#smart-controls)
3. [🎒 Pet Inventory & Scaling](#pet-inventory)
4. [🦮 Interaction Tools](#pet-behaviors)
5. [🧬 Entity Conversion](#conversion)
6. [🛡️ Pet Safety & Pet Beds](#pet-safety)
7. [💀 Hidden Combat Mechanics](#hidden-mechanics)
8. [🐉 Boss Pets](#boss-pets)
9. [⚙️ Advanced: Categories & Attributes](#advanced-systems)
10. [🕹️ Administrative Core & NBT](#admin-commands)
11. [⚙️ Complete Configuration Guide](#configuration)

---

## 🤝 Core Mechanics: Taming

Taming is the process of acquiring a pet. By default, crouching (Shift) and Right-Clicking a compatible mob with **Golden Wheat** will attempt to tame it.

### 🌾 Golden Wheat Attraction
While holding Golden Wheat (`petting:golden_wheat`), nearby animals will follow the player at an increased speed (`1.25x`).

### 🎲 Taming Success Factors
*   **Base Chance**: Default `33%` success rate (Configurable).
*   **Weakness Scaling**: Mobs with lower HP are easier to tame! (`healthScalesTamingChance`).
*   **Health Thresholds**: Some mobs might require you to lower their HP below a certain percentage before they can be tamed at all (`tameHealthThreshold`).
*   **Kill Requirements**: You may be required to have killed at least one mob of that type before being allowed to tame them (`requireKillToTame`).
*   **Custom Taming Items**: Configure specific items for specific mobs (e.g. Skeletons eating Bones, Creepers needing Gunpowder) in the `customTamingItems` config using the `entity|item` format.

---

## 🕹️ Smart Controls & Movement
Petting uses an interaction system to manage pet states.

### Empty Hand Actions (Owner Only)
- **Right-Click**: Cycles through high-priority actions:
    1.  **Stand Up**: If your pet is sitting, it will stand.
    2.  **Mount**: If the pet has a **Saddle**, you'll hop on!
    3.  **Sit Down**: If standing, the pet will sit and relax.
- **Shift + Right-Click**: Opens the **Pet Inventory**.

### 🏇 Riding Physics
Riding supports movement control for all mob types:
- **Space Bar**: Swim or Fly upwards.
- **S Key**: Backwards movement (Land) or Swim/Fly downwards.
- **Sprint (Ctrl)**: Adds a 1.3x speed boost while riding.
- **Neutral Buoyancy**: Flyers and Swimmers will maintain their current height/depth when you stop moving.
- **Rotation Sync**: The pet's rotation is synchronized with the player's view.

---

## 🎒 Pet Inventory & Equipment Scaling
Every pet has a dedicated inventory accessible via **Shift + Right-Click**.
- **Armor Slots**: Give your pets full protection.
- **Hand Slots**: Equip your pet with Swords or Shields (mobs physically hold and use them!).
- **Detection**: The mod automatically detects supported equipment slots for each mob type.
- **Stat Scaling**: All tamed pets receive a global bonus to **Base Armor** (`petBaseArmor`) and **Armor Toughness** (`petBaseArmorToughness`) via config.
- **Storage**: 14 slots for your pet to carry your loot.

---

## 🦮 Interaction Tools
Specific tools are used to manage pet behaviors. All tool items are configurable.

| Tool | Default Item | Action |
| :--- | :--- | :--- |
| **Status Stick** | `minecraft:stick` | **Right-Click**: Follow/Wait/Wander. **Shift+Right-Click**: Detailed Status Report. |
| **Whistle** | `petting:follow_whistle` | Cycles follow distance (5-50 blocks). |
| **Orb** | `petting:teleport_orb` | Cycles teleport triggering distance (10-100 blocks). |
| **Clock** | `minecraft:clock` | Toggles whether the pet responds to global Whistles (Goat Horns). |
| **Sword** | `any sword` | Toggles **Aggressive Mode**: Attack if owner attacks. |
| **Shield** | `minecraft:shield` | Toggles **Defense Mode**: Attack if self-attacked. |
| **Cookie** | `minecraft:cookie` | Toggles **Guard Mode**: Attack if owner is attacked. |
| **Tether** | `petting:pet_tether` | Binds a pet to a **10-block roam radius** around its current position. |
| **Shears** | `minecraft:shears` | **Crouch + Right-Click** to release the pet back to the wild. |
| **Goat Horn** | `any goat horn` | **Crouch + Use** to teleport ALL your owned pets to your side instantly. |

---

## 🧬 Entity Conversion

When a pet entity converts to another type (e.g., Zombie to Drowned), all pet data is transferred to the new entity.
- Data transferred: Owner UUID, Custom Names, AI Modes, Follow/Teleport distances, **Saddles**, **Equipped Items**, and even **Pet Bed Bindings**.

---

## 🛡️ Pet Safety & Pet Beds
The mod includes systems for pet protection and respawning.

### 🛏️ The Pet Bed Block

The Pet Bed allows pets to respawn at a fixed location.
- **Binding Mode**: Right-click a Pet Bed to enter Binding Mode, then right-click your pet. They are now linked!
- **Infinite Respawn**: If a bound pet dies, the death is canceled. They instead **teleport safely to their bed**, heal fully, and wait for you in a sitting pose.
- **Cancellation**: Right-click the air while in Binding Mode to cancel the process.

### 🛡️ Environmental Protections & QoL
- **Passive Immunities**: All tamed pets are **immune to Fall Damage, Fire, and Lava**.
- **Mutual Pacifism**: Your pets will **never** attack each other (including accidental projectile hits).
- **Sit Heal**: Tamed pets regenerate health while sitting (Default: 1.0 health every 2 seconds).

---

## 💀 Hidden Combat Mechanics
The mod includes automated AI modifications for combat safety.

- **Wither Peace**: Tamed Withers will not fire skulls while in an idle state.
- **Aura Protection**: Pets will clear the target of nearby hostile entities that are targeting the pet's owner.
- **Warden Management**: Tamed Wardens have their anger levels towards owners and other pets continuously cleared.

---

## 💀 Boss Pets
- **Wither**: Skull projectiles are neutralized while idle.
- **Ender Dragon**: Health bar is hidden once tamed and supports riding combat.
- **Boss Bar Stealth**: Option to hide health bars for tamed bosses (`hideTamedBossBars`).

---

## ⚙️ Advanced: Categories & Attributes
System for managing multiple pets and limits.

### 🏷️ Categories
Group mobs into categories to set shared limits (e.g., "Undead" or "Dragons").
- **Format**: `SlotID|DisplayName|MobList|DefaultLimit`
- **Example**: `1|Undead|minecraft:zombie,minecraft:skeleton|5`

### ⚡ Custom Attributes
Players can have their pet count increased via custom attributes compatible with gear mods:
- `petting:max_pets`: Increases the global pet limit.
- `petting:max_pets_category_1` through `20`: Increases limits for specific categories defined in the config.

---

## 🕹️ Administrative Core & NBT
Admins and map-makers can use these tools for deep integration.

### Admin Commands
- `/pet <target_mob> <target_player>`: Instantly tames any mob for the specified player.

### Instant Taming via /summon (NBT)
You can spawn pre-tamed mobs using vanilla commands by adding the standard NBT tags:
```bash
/summon zombie ~ ~ ~ {ForgeData: {pettingtamed: 1b, ownerUUID: "YOUR-UUID-HERE"}}
```
*Note: Mobs summoned this way will automatically have their hostile AI disabled and be linked to you.*

---

## ⚙️ The Complete Configuration Guide

Edit `config/petting-common.toml` for total control.

### 🛡️ Taming & Persistence
| Option | Default | Description |
| :--- | :--- | :--- |
| `tameChance` | `0.33` | Success rate per attempt. |
| `healthScalesTamingChance`| `false`| Lower HP = better success chance. |
| `tameHealthThreshold` | `0.0` | Required % of missing health. |
| `requireKillToTame` | `false`| Must have 1 kill in stats type. |
| `whitelistOnly` | `false` | Only IDs in `tamingWhitelist` can be tamed. |
| `tamingWhitelist` | `[]` | List of allowed IDs for taming. |
| `blacklistEnabled` | `false` | Enable global mod blacklist. |
| `tamingBlacklist` | `[]` | IDs to COMPLETELY ignore. |
| `allowGoldenWheat` | `true` | Enables universal taming item. |
| `disableRespawnOnTame` | `true` | Mobs keep original gear/NBT instead of respawning. |

### 🧠 AI & Survival
| Option | Default | Description |
| :--- | :--- | :--- |
| `followDistance` | `10.0` | Blocks away before following starts. |
| `teleportDistance` | `20.0` | Blocks away before snapping to owner. |
| `maxPetsPerPlayer` | `-1` | Soft-cap for pet ownership (-1 is infinite). |
| `allowOwnerToHurtPets`| `false`| Enable **Friendly Fire**. |
| `sitHealEnabled` | `true` | Regen health while sitting. |
| `sitHealAmount` | `1.0` | Half-hearts healed per interval. |
| `sitHealInterval` | `40` | Ticks between heals (20 ticks = 1s). |
| `enableGoatHornWhistle` | `true` | Allows Crouch+Horn to summon all pets. |
| `whistleTeleportsTethered` | `false` | Area-bound pets also teleport to whistle. |
| `hideTamedBossBars` | `true` | Hide Wither/Ender Dragon health bars. |
| `preventPetToOwnerDamage` | `true` | Pets cannot damage their owners. |

### 🏇 Riding & Movement
| Option | Default | Description |
| :--- | :--- | :--- |
| `allowPetRiding` | `true` | Master toggle for mounting pets. |
| `mountRequireSaddle` | `false`| If true, MUST have a saddle to ride. |
| `landRidingSpeedMultiplier`| `1.0` | Land speed bonus. |
| `flyingRidingSpeedMultiplier`| `1.0` | Flying speed bonus. |
| `swimmingRidingSpeedMultiplier`| `1.0`| Swimming speed bonus. |
| `ridingWhitelistOnly` | `false` | Only IDs in `ridingWhitelist` can be ridden. |
| `ridingBlacklistEnabled` | `false` | Enable riding blacklist. |
| `manualFlyingMobs` | `[]` | Force 3D Flight for specific mob IDs. |
| `manualSwimmingMobs` | `[]` | Force 3D Swimming for specific mob IDs. |
| `allowPetAttackWhileRiding` | `true` | Fire projectiles while riding Wither/Dragon. |

### 🎒 Inventory & Base Stats
| Option | Default | Description |
| :--- | :--- | :--- |
| `petBaseArmor` | `0.0` | Global armor bonus for all tamed pets. |
| `petBaseArmorToughness` | `0.0` | Global toughness bonus. |
| `extraEquippableMobs` | `[]` | Force equipment slots on specific IDs. |
| `alwaysShowEquipmentSlots` | `false`| Force Armor/Hand slots on all mobs. |
| `inventoryWhitelistOnly` | `false` | Only whitelisted mobs get an inventory. |
| `inventoryBlacklistEnabled` | `false` | Disable inventory for blacklisted IDs. |
| `petPortraitRenderScale`| `45.0` | Render scale in the inventory screen. |

### 🛠️ Interaction Toggles
| Option | Default | Description |
| :--- | :--- | :--- |
| `allowPerPetStatus` | `true` | Enable Stick (Status). |
| `allowPerPetAggression` | `true` | Enable Sword (Aggressive). |
| `allowPerPetSelfDefense`| `true` | Enable Shield (Defense). |
| `allowPerPetGuard` | `true` | Enable Cookie (Guard). |
| `allowPerPetFollowDist` | `true` | Enable Whistle (Follow Dist). |
| `allowPerPetTeleportDist`| `true` | Enable Orb (Teleport Dist). |
| `allowPerPetWhistleToggle`| `true` | Enable Clock (Whistle response). |
| `allowPetTethering` | `true` | Enable Tether (Area Bind). |
| `allowPetReleasing` | `true` | Enable Shears (Release). |

---

<div align="center">
  🚀 <b>Enjoy the mod!</b><br>
  <i>Made with ❤️ by <a href="https://github.com/yigit-guven">Yigit Guven</a></i>
</div>