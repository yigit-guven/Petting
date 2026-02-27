<div align="center">
  <h1><img src="https://raw.githubusercontent.com/yigit-guven/Petting/refs/heads/1.20.1-Forge/src/main/resources/logo.png" width="32" height="32" alt="Petting Logo" style="vertical-align: middle;"> Petting</h1>
  <p><i>Turn any mob into your loyal, protective companion!</i></p>

  [![CurseForge Downloads](https://img.shields.io/curseforge/dt/1083170?style=flat-square\&logo=curseforge\&label=CurseForge\&color=orange)](https://www.curseforge.com/minecraft/mc-mods/petting) [![Modrinth Downloads](https://img.shields.io/modrinth/dt/petting?style=flat-square\&logo=modrinth\&label=Modrinth)](https://modrinth.com/mod/petting) [![Discord](https://img.shields.io/discord/1440563800930652235?style=flat-square\&logo=discord\&label=Discord)](https://discord.gg/aPk7Qs5d4H)

  <p>
    <a href="https://github.com/yigit-guven/Petting"><b>GitHub</b></a> •
    <a href="https://github.com/yigit-guven/Petting/wiki"><b>Wiki</b></a> •
    <a href="https://github.com/yigit-guven/Petting/issues"><b>Issues</b></a> •
    <a href="https://discord.gg/aPk7Qs5d4H"><b>Discord</b></a>
  </p>
</div>

---

**Petting** is the ultimate mob-taming overhaul for Minecraft. Instead of simply fighting monsters, you can now **tame** them! Transform Zombies, Creepers, or even the Wither Boss into loyal family members that guard your base, carry your items, and follow you into the deepest caves.

---

## 📖 Table of Contents
1. [Core Mechanics: Taming](#core-mechanics-taming)
2. [Smart Controls (No-Fuss Interaction)](#smart-controls)
3. [The Pet Inventory & Equipment](#pet-inventory)
4. [Pet Behaviors (AI Modes)](#pet-behaviors)
5. [Immortal Safety & Quality of Life](#pet-safety)
6. [Tethering & Pet Beds (Base Management)](#tethering--beds)
7. [Vanilla Taming Integration](#vanilla-integration)
8. [Advanced Configuration (The Master Guide)](#configuration)

---

## 🤝 Core Mechanics: Taming

Taming a mob is simple, but deeply customizable. By default, you can crouch (Shift) and Right-Click a mob using **Golden Wheat** to attempt to tame it.

### 🌾 Golden Wheat Attraction
Golden Wheat isn't just for taming! While held, nearby animals will be **attracted** to you, looking your way and following you at an increased speed (`1.25x`), making it easier to lead them into taming pens.

### 🎲 Taming Success Factors
*   **Base Chance**: Default `33%` success rate (Configurable).
*   **Weakness Scaling**: Mobs with lower HP are easier to tame! (Check `healthScalesTamingChance`).
*   **Health Thresholds**: Some mobs might require you to lower their HP below a certain percentage before they can be tamed at all.
*   **Kill Requirements**: You may be required to have killed at least one mob of that type before you are "skilled" enough to tame them.
*   **Custom Taming Items**: Every mob can have its own taming item! (e.g., Skeletons eating Bones, Creepers needing Gunpowder).

---

## 🕹️ Smart Controls
We've replaced complex button combos with a "Smart" interaction system that prioritizes what you want to do.

### Empty Hand Actions (Owner Only)
- **Right-Click**: Cycles through high-priority actions:
    1.  **Stand Up**: If your pet is sitting, it will stand.
    2.  **Mount**: If the pet has a **Saddle** and riding is allowed, you'll hop on !
    3.  **Sit Down**: If standing, the pet will sit and relax.
- **Shift + Right-Click**: Reliably opens the **Pet Inventory**.

### 🪄 The Command Stick
Hold a **Stick** to manage your pet's brain:
- **Right-Click**: Cycles AI modes: **Wander** -> **Sit** -> **Wait**.
- **Shift + Right-Click**: Prints a detailed **Status Report** (Aggression, Tethers, Attributes).

---

## 🎒 Pet Inventory & Equipment
Every tamed pet has a resident inventory (Default: **Shift + Right-Click**).
- **Armor Slots**: Give your Zombie full Netherite or protect your Creeper with Iron!
- **Hand Slots**: Equip your pet with Swords or Shields (mobs physically hold and use them!).
- **Saddle Slot**: Required for mounting certain large or powerful mobs.
- **Persistent Storage**: 14 slots for your pet to carry your loot.

> [!TIP]
> Use the `petPortraitRenderScale` config to customize how large your pet looks inside the inventory screen!

---

## 🦮 Pet Behaviors
Once tamed, use the **Stick** to toggle their state. State changes are shown in your **Action Bar** for quick confirmation.

1.  **Wander (Follow)**: Passive tracking. The pet follows you and teleports to you if it falls behind.
2.  **Sit (Relax)**: Pet stays put. While sitting, pets **regenerate health** (Default: 0.5 hearts every 2 seconds).
3.  **Wait (Guard)**: Pet stands still but remains aggressive. It will track nearby targets with its head but won't walk away from its post.

---

## 🛡️ Immortal Safety & Quality of Life
- **Passive Immunities**: All tamed pets are **immune to Fall Damage, Fire, and Lava**.
- **The Goat Horn Whistle**: Blow a Goat Horn while crouching to teleport **EVERY** owned pet to your side (resets them to Wandering mode).
- **Mutual Pacifism**: Your pets will **never** attack each other. Vanilla wolves and Skeletons can finally live in peace.
- **NBT Preservation**: Mobs keep their natural names, gear, and potion effects when tamed.

---

## 🛏️ Tethering & Pet Beds
Manage where your pets live.
- **Pet Tether**: Right-click a pet to bind it to a **10-block radius**. They won't follow you until released. Bound pets ignore the Whistle by default to stay at their post.
- **Pet Beds**: Bind a pet to a bed block. If they die, their death is **canceled**, and they are teleported safely to their bed at full health.

---

## 🐺 Vanilla Integration
Vanilla pets (Wolves, Cats) are automatically upgraded to **Hybrid Pets**. They gain all Petting immunities, respect the Max Pet limits, and respond to the Goat Horn Whistle while keeping their vanilla behaviors.

---

## 💀 Boss Pets (The Wither)
- **True Pacifism**: Tamed Withers will **not** fire destructive skulls while idle. All stray projectiles are deleted instantly.
- **Boss Bar Stealth**: The obnoxious Wither health bar is hidden from the server once tamed.

---

## ⚙️ Advanced Configuration (Master Guide)

Edit `config/petting-common.toml` for total control.

### 🛡️ Taming Settings
| Option | Default | Description |
| :--- | :--- | :--- |
| `tameChance` | `0.33` | Success rate per attempt (0.0 - 1.0). |
| `healthScalesTamingChance`| `false`| If true, lower HP = better success chance. |
| `tameHealthThreshold` | `0.0` | Required % of missing health (e.g., 0.5 means 50% HP or lower). |
| `requireKillToTame` | `false`| Must have 1 kill of that mob in stats to tame. |
| `whitelistOnly` | `false`| Only mobs in `tamingWhitelist` can be tamed. |
| `allowGoldenWheat` | `true` | Enables the universal taming item. |

### 🧠 AI & Behavior Settings
| Option | Default | Description |
| :--- | :--- | :--- |
| `followDistance` | `10.0` | Blocks away before following starts. |
| `teleportDistance` | `20.0` | Blocks away before snapping to owner. |
| `sitHealEnabled` | `true` | Regen health while sitting. |
| `maxPetsPerPlayer` | `-1` | Soft-cap for pet ownership (Attributes can modify this). |
| `hideTamedBossBars` | `true` | Hide Wither/Ender Dragon health bars. |

### 🏇 Riding & Inventory Settings
| Option | Default | Description |
| :--- | :--- | :--- |
| `allowPetRiding` | `true` | Master toggle for mounting pets. |
| `mountRequireSaddle` | `false`| If true, MUST have a saddle in the slot to ride. |
| `landRidingSpeedMultiplier`| `1.0` | Movement speed multiplier for ground mobs. |
| `flyingRidingSpeedMultiplier`| `1.0` | Movement speed multiplier for flying mobs. |
| `petPortraitRenderScale`| `45.0` | Visual scale of pet in the inventory screen. |

### 🛠️ Item Interaction Toggles
Enable or Disable individual item tools (Default: `true` for all):
`allowPerPetStatus` (Stick), `allowPerPetAggression` (Sword), `allowPerPetSelfDefense` (Shield), `allowPerPetGuard` (Cookie), `allowPerPetFollowDist` (Lead), `allowPerPetTeleportDist` (Pearl), `allowPerPetWhistleToggle` (Clock), `allowPetTethering` (Tether), `allowPetReleasing` (Shears).

### 💬 UI & Feedback
| Option | Default | Description |
| :--- | :--- | :--- |
| `commandFeedbackStyle` | `ACTION_BAR`| Choices: `ACTION_BAR`, `CHAT`, `NONE`. |
| `enableParticles` | `true` | Visual hearts/smoke on interaction. |
| `interactionCooldown` | `20` | Ticks between petting actions. |

---

<div align="center">
  🚀 <b>Go out and build your ultimate monster army!</b><br>
  <i>Made with ❤️ by <a href="https://github.com/yigit-guven">Yigit Guven</a></i>
</div>