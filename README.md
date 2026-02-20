<div align="center">
  <h1><img src="https://raw.githubusercontent.com/yigit-guven/Petting/refs/heads/1.20.1-Forge/src/main/resources/logo.png" width="32" height="32" alt="Petting Logo" style="vertical-align: middle;"> Petting</h1>
  <p><i>Turn any mob into your loyal, protective companion!</i></p>

  [![CurseForge Downloads](https://img.shields.io/curseforge/dt/1083170?style=flat-square\&logo=curseforge\&label=CurseForge\&color=orange\&link=https%3A%2F%2Fwww.curseforge.com%2Fminecraft%2Fmc-mods%2Fpetting)](https://www.curseforge.com/minecraft/mc-mods/petting) [![Modrinth Downloads](https://img.shields.io/modrinth/dt/petting?style=flat-square\&logo=modrinth\&label=Modrinth\&link=https%3A%2F%2Fmodrinth.com%2Fmod%2Fpetting)](https://modrinth.com/mod/petting) [![Discord](https://img.shields.io/discord/1440563800930652235?style=flat-square\&logo=discord\&label=Discord)](https://discord.gg/aPk7Qs5d4H)

  <p>
    <a href="https://github.com/yigit-guven/Petting"><b>GitHub</b></a> •
    <a href="https://github.com/yigit-guven/Petting/wiki"><b>Wiki</b></a> •
    <a href="https://github.com/yigit-guven/Petting/issues"><b>Issues</b></a> •
    <a href="https://discord.gg/aPk7Qs5d4H"><b>Discord</b></a>
  </p>
</div>

---

**Petting** is a comprehensive Minecraft mod that reimagines your interaction with the world. Instead of simply fighting monsters, you can now **tame** them! By giving a mob a little love (and the right item), you can transform previously hostile entities like Zombies, Creepers, or even the devastating Wither Boss into fiercely loyal companions that follow you, obey commands, and protect you against all threats.

Designed with extreme configurability and vast quality-of-life improvements, Petting completely unifies with vanilla mechanics, ensuring that your custom monster pets and your vanilla wolves can fight side-by-side without ever turning on you.

---

## 📖 Table of Contents
1. [Core Mechanics: Taming](#core-mechanics-taming)
2. [Pet Behaviors & Commands](#pet-behaviors--commands)
3. [The Pet Tether (Binding Pets)](#the-pet-tether-binding-pets)
4. [Quality of Life & Pet Safety](#quality-of-life--pet-safety)
5. [Vanilla Taming Integration (Hybrid Pets)](#vanilla-taming-integration-hybrid-pets)
6. [Boss Pet Features (The Wither)](#boss-pet-features-the-wither)
7. [Advanced Configuration (The Config File)](#advanced-configuration-the-config-file)

---

Taming a mob is simple, but deeply customizable. By default, any player can crouch (Shift) and Right-Click a mob using **Golden Wheat** to attempt to tame it.

### 🌾 Golden Wheat Attraction
Golden Wheat isn't just for taming! If you hold it in either hand, nearby animals will be **attracted** to you, looking your way and following you (speed 1.25) as long as you keep the wheat out. This makes it much easier to lead mobs into positions for taming!

### Taming Factors
* **RNG Chance:** Taming is not guaranteed! By default, there is a `33%` chance of success (configurable to match vanilla wolves). If you fail, the item is consumed and smoke particles appear.
* **Health Scaling (Optional):** You can enable `healthScalesTamingChance` in the config. If enabled, weakening a mob physically increases your chance of taming it. A mob with 1 HP remaining is far easier to tame than a fully-healed brute!
* **Health Thresholds:** The mod allows server owners to set a `tameHealthThreshold`. If set, a mob **must** be damaged below a certain percentage of its maximum health before taming will even be attempted.
* **Kill Requirements:** If `requireKillToTame` is enabled, a player must have killed at least one entity of that type (recorded in Vanilla player statistics) before they are allowed to tame one.
* **Custom Taming Items:** Servers can map specific items to specific mobs. Want Skeletons to require Bones instead of Golden Wheat? Easily configure it using the `customTamingItems` map!

---

## 🦮 Pet Behaviors & Commands

Once tamed, the mob is yours to command. You can give your pet specific orders by interacting with them empty-handed. Every time you change their state, you receive visual feedback (configurable as Action Bar text, Chat messages, or completely silent).

### The Three States:
1. **Wandering (Default):** Your pet will flexibly follow your trail and defend you if you are attacked, warping to catch up if they fall too far behind.
2. **Sitting (Relaxing):** Your pet will halt all movement and peacefully sit. While sitting, pets will **slowly regenerate health over time**! They will not engage in combat.
3. **Waiting (Guard Mode):** Your pet will stand still, but remain highly alert. They will track nearby entities with their head but refuse to walk away from their post.

### Control Schemes
Players can choose how they interact with their pets via the `controlScheme` config:
* **Classic (Default):** `Right-Click` toggles Sitting. `Shift + Right-Click` toggles Waiting.
* **Right-Click Cycle:** Simply `Right-Click` to cycle smoothly through Wandering -> Sitting -> Waiting.
* **Shift-Right-Click Cycle:** Simply `Shift + Right-Click` to cycle smoothly through Wandering -> Sitting -> Waiting.

---

## ⚓ The Pet Tether & Pet Beds

Provide your pets with a permanent home and specialized guarding zones.

### The Pet Tether (Binding Locations)
Tired of your pets following you to their doom or cluttering up your base? Use a **Pet Tether**!
*   **How to Bind:** Right-Click your tamed pet with a **Pet Tether** (found in the Petting creative tab) to bind them to their current coordinates.
*   **Free Roaming:** Once bound, the pet will freely roam within a configurable radius (default: `10.0` blocks) of their anchor point. They will **not** follow you.
*   **Pathing Back:** If a tethered pet is pushed or wanders outside its roaming radius, it will actively path find back to its anchor point. If it gets significantly separated, it will safely teleport back to its bound spot.
*   **Unbinding:** Simply Right-Click them with the tether again to release them.

### 🛏️ Pet Beds (Death Protection & Respawning)
You can bind your pet to a **Pet Bed** (or any block with "pet_bed" in its registry name) to give them a respawn point.
1. **Active Binding Mode:** Right-click a Pet Bed block with an empty hand. You will see a message: *"[Petting] Binding Mode Active!"*
2. **Bind the Pet:** Right-click your tamed pet while Binding Mode is active.
3. **Immortality (Saved by Bed):** If a pet bound to a bed "dies," the **death is canceled**. The pet is instantly healed to full, cleared of all status effects, and **teleported back to its bed** where it will sit and wait for you.
4. **Safety Check:** The mod automatically searches for a safe spot near the bed to respawn the pet, ensuring they don't get stuck in walls!

---

## 🛠️ Per-Pet Customization (Item Interactions)
While global configurations set the defaults, you can customize every pet individually by **Right-Clicking** them with specific items:

| Item | Action | Description |
| :--- | :--- | :--- |
| **Stick** | **Status Report** | Displays all current settings for this pet in your chat. |
| **Any Sword**| **Aggressive Mode** | Toggles whether the pet attacks mobs that you attack. |
| **Shield** | **Defend Self** | Toggles whether the pet retaliates when it takes damage. |
| **Cookie** | **Guard Owner** | Toggles whether the pet protects you when you are attacked. |
| **Lead** | **Follow Dist** | Cycles between **5, 10, 20, and 50** blocks. |
| **Ender Pearl**| **Teleport Dist** | Cycles between **10, 20, 50, and 100** blocks. |
| **Clock** | **Whistle Mode** | Toggles whether the pet responds to **Goat Horn whistles**. |
| **Pet Tether** | **Bound Move** | Toggles "Anchor Mode" (Stay in a 10-block radius of current spot). |

---


## 🛡️ Quality of Life & Pet Safety

Having a pet shouldn't be stressful! Petting introduces major passive upgrades to ensure your companions don't die to silly mistakes.

### Pet Infallibility
All tamed pets are natively **immune to Fall Damage, Fire Damage, and Lava Damage**. They can follow you off a cliff or through the Nether without instantly perishing!

### The Pet Whistle 🐐
Lost your pets in a cave? Left them sitting 10,000 blocks away? 
Equip a **Goat Horn**, hold `Shift`, and use the item (Blow the horn). Doing so acts as a universal Pet Whistle, instantly teleporting **all** of your owned custom pets directly back to your side, resetting their state to Wandering!

> [!NOTE]
> By default, tethered (bound) pets will **ignore** the whistle to prevent accidentally pulling them away from their guarded posts. This can be enabled in the config via `whistleTeleportsTethered`.


### NBT/Armor Preservation
When you tame a Zombie wearing Diamond Armor, you want it to keep that armor! Taming organically modifies the existing mob rather than replacing it, completely preserving custom names, NBT tags, naturally spawning weapons, and potion effects.

### Limiters
Server owners can enforce a `maxPetsPerPlayer` soft-cap. This is implemented via a **Player Attribute** (`petting:max_pets`), meaning modpacks can dynamically increase or decrease a player's pet limit based on their class, items, or level!

#### Pet Categories (Advanced Limits)
You can now group specific mobs into **Categories** (e.g., Undead, Animal, Special) via the config. 
- Each category uses one of **20 dedicated attribute slots** (`max_pets_category_1` through `20`).
- This allows modpacks to create complex progression systems where players can only tame certain types of mobs once they've unlocked a specific "skill" or level (by increasing the corresponding attribute value).
- If a player reaches a category limit, they will be notified: *"You have reached your [Category] pet limit!"*

If a player reaches a limit, they will be given an error message in chat and prevented from taming any further until they reduce their ranks.

---

## 🐺 Vanilla Taming Integration (Hybrid Pets)

What happens to your Vanilla Minecraft tamed Wolves and Cats? Under Petting, they become **Hybrid Pets**!

When you employ the standard Vanilla taming method (giving a Wolf a Bone), Petting actively intercepts the logic. The animal becomes a standard Vanilla pet (gaining a collar, classic sitting mechanics, and breeding), but it **ALSO** fundamentally receives all Petting mod tags. This means your Vanilla wolves natively gain:
* Immunity to Fall, Fire, and Lava damage!
* Full teleportation support via the Goat Horn Whistle!
* Direct inclusion in your maximum pet cap!

### Universal Mutual Pacifism
Previously, native Minecraft code caused Wolves or `Doggy Talents Next` dogs to furiously growl at, and inevitably murder, your tamed Skeleton or Creeper pets.

Not anymore. **All Friendly Fire is strictly disabled.** The targeting AI aggressively forces **Native Vanilla Pacifism**. If a Wolf and a Creeper are owned by the exact same player, the mod unconditionally terminates any hostile AI paths between them every single tick, rendering them perfectly peaceful to one another.

---

## 💀 Boss Pet Features (The Wither)

Taming a Wither Boss is the ultimate flex, but their destructive nature can be annoying. We've polished them into perfect bodyguards!

* **True Pacifism:** By default, Withers erratically fire exploding side-heads at any living creature, destroying your base. Tamed Withers have this completely neutralized. Unless you explicitly command them by engaging in combat, stray exploding Wither Skulls are **unconditionally deleted** from reality the millisecond they spawn. Your Wither is completely grief-proof while idle!
* **Boss Bar Hiding:** Taming a Wither normally plasters an obnoxious Boss Health bar at the top of the screeen for the entire server. If `hideTamedBossBars` is enabled, the mod uses advanced reflections to silently wipe the Boss Bar interface across the server for any tamed boss!

---

## 💻 Developer & Admin Tools

### Commands
*   `/pet <mob_entity> <player>`: Instantly tames the targeted mob and assigns it to the specified player. (Requires Level 4 Permissions).

### NBT Summoning Tags
For map makers and modpack developers, you can summon mobs that are **automatically tamed** upon spawning by using custom tags:
*   Add the tag `petowner:UUID` (e.g., `petowner:796c428d-1234...`) to a mob to assign it to that UUID.
*   Use `petowner:self` or `petowner:me` to automatically assign the mob to the nearest player within 10 blocks when it spawns.

---

## ⚙️ Advanced Configuration (The Config File)

Every mechanic described above can be tweaked, disabled, or amplified via the mod's configuration file: `config/petting-common.toml`. 
*You can quickly open your config folder by clicking the "Config" button in the Mod Menu!*

### Config Summary
* **Lists:** Define `tamingWhitelist` and `tamingBlacklist` to strictly control exactly which Entity Registry IDs can and cannot be tamed.
* **Numbers:** Tune the `sitHealAmount`, `sitHealInterval`, `followDistance`, `teleportDistance`, `boundRoamRadius`, `maxPetsPerPlayer`, `interactionCooldown`, `tameChance`, and `tameHealthThreshold`.
* **Booleans:** Toggle `disableRespawnOnTame`, `enableParticles`, `whitelistOnly`, `blacklistEnabled`, `allowGoldenWheat`, `enableGoatHornWhistle`, `whistleTeleportsTethered`, `requireKillToTame`, `hideTamedBossBars`, and `healthScalesTamingChance`.
* **Enums:** Configure the `controlScheme` and `commandFeedbackStyle` (ACTION_BAR, CHAT, or NONE).
* **Mappings:** Define `customTamingItems` (e.g., `["minecraft:zombie|minecraft:bone", "minecraft:spider|minecraft:spider_eye"]`) and `petCategories` for grouping mob limits.
* **Item Toggles:** Individually enable/disable item interactions via `allowPerPetStatus`, `allowPerPetAggression`, `allowPerPetSelfDefense`, `allowPerPetGuard`, `allowPerPetFollowDist`, `allowPerPetTeleportDist`, `allowPerPetWhistleToggle`, and `allowPetTethering`.

---
<div align="center">
  <p>🚀 <b>Project Links:</b> 
    <a href="https://modrinth.com/mod/petting">Modrinth</a> | 
    <a href="https://www.curseforge.com/minecraft/mc-mods/petting">CurseForge</a> | 
    <a href="https://github.com/yigit-guven/Petting">Source Code</a> | 
    <a href="https://github.com/yigit-guven/Petting/wiki">Documentation (Wiki)</a> | 
    <a href="https://github.com/yigit-guven/Petting/issues">Bug Reports</a>
  </p>
  <i>Go out and build your ultimate monster army! Made with ❤️ by <a href="https://github.com/yigit-guven">Yigit Guven</a></i>
</div>