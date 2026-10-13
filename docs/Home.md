<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/banner_ingame.png" alt="Petting banner" width="480">
</p>

# Petting Wiki

**Petting** is a NeoForge mod (mod id `petting`) that adds a universal taming system. Any mob, vanilla or modded, can be tamed with a *golden treat*. Tamed mobs ("pets") follow you, fight according to a configurable *collar*, can carry equipment, can be ridden, and can respawn at a bound bed or cushion.

This wiki documents the **`26.3-NeoForge`** branch of the [Petting repository](https://github.com/yigit-guven/Petting). Every statement here was derived from the contents of the `src` folder of that branch: Java sources, data files, asset files and language files.

> [!NOTE]
> Page links in this wiki are written without a file extension, which is the format used by GitHub Wikis. Images are loaded from the `26.3-NeoForge` branch of the repository.

---

## Find what you are looking for

| I want to... | Go to |
|---|---|
| Learn the basics and tame my first pet | [Getting Started](Getting-Started) |
| Understand exactly how taming works (health, chance, limits) | [Taming](Taming) |
| See every item and block the mod adds | [Items](Items) |
| Find the recipe for a treat and the mobs it tames | [Golden Treats](Golden-Treats) |
| Craft, dye and configure collars | [Collars](Collars) |
| Understand Passive / Defensive / Aggressive / Wander / Guard | [Combat Modes](Combat-Modes) |
| Know how pets follow, sit, teleport and avoid friendly fire | [Pet Behavior](Pet-Behavior) |
| Equip armor, saddles and collars through the pet GUI | [Pet Inventory](Pet-Inventory) |
| Ride my pet (walking, flying, swimming) | [Riding](Riding) |
| Make my pet come back after death | [Collar Respawn](Collar-Respawn) |
| Obtain and use a tamed Ender Dragon | [Golden Dragon Egg](Golden-Dragon-Egg) |
| See the default key bindings | [Controls](Controls) |
| Use the `/summon pet` command | [Commands](Commands) |
| Change a setting (server or client) | [Configuration](Configuration) |
| Add or remove mobs from a treat with a datapack | [Custom Treat Tags](Custom-Treat-Tags) |
| Look up a chat message and the option that hides it | [Chat Messages](Chat-Messages) |
| Check JEI and modded-mob support | [Compatibility](Compatibility) |
| Look up registry ids, data components, packets and mixins | [Technical Reference](Technical-Reference) |
| Get a quick answer | [FAQ](FAQ) |

---

## Feature overview

| Feature | Summary | Page |
|---|---|---|
| Universal taming | Right-click a mob with the matching golden treat. Hostile mobs must first be weakened (30 % health by default). | [Taming](Taming) |
| Nine golden treats | Each treat is bound to an entity-type tag, so datapacks can extend it. | [Golden Treats](Golden-Treats) |
| Sixteen dyed collars | Collars store per-pet behavior settings and an optional respawn point. | [Collars](Collars) |
| Five combat modes | Passive, Defensive, Aggressive, Wander, Guard. | [Combat Modes](Combat-Modes) |
| Pet inventory GUI | Armor, hand items, collar, saddle and chest slots depending on the mob. | [Pet Inventory](Pet-Inventory) |
| Riding | Walking, flying and swimming controls, ridden attacks, optional saddle requirement. | [Riding](Riding) |
| Collar respawn | Bind a collar to a bed or cushion and the pet respawns there on death. | [Collar Respawn](Collar-Respawn) |
| Tamed Ender Dragon | Golden Star on a Dragon Egg, then the End exit portal ritual. | [Golden Dragon Egg](Golden-Dragon-Egg) |
| JEI integration | A "Pet Taming" category lists which mobs each treat tames. | [Compatibility](Compatibility) |

---

## Mod facts

| Property | Value | Source |
|---|---|---|
| Mod id | `petting` | `Petting.java` |
| Mod loader | `javafml` (NeoForge) | `neoforge.mods.toml` |
| Required dependencies | `neoforge`, `minecraft` (both sides) | `neoforge.mods.toml` |
| Optional integration | JEI (plugin class is annotated `@JeiPlugin`) | `PettingJeiPlugin.java` |
| Issue tracker | <https://github.com/yigit-guven/Petting/issues> | `neoforge.mods.toml` |
| Credits | "Special thanks to Anchor Studios & Ryukazan for their contributions." | `neoforge.mods.toml` |
| Languages shipped | English (`en_us.json`) | `assets/petting/lang` |
| Creative tab | "Petting" (`petting:petting`), icon: Golden Paw | `PettingModTabs.java` |

---

## Page index

**Gameplay**
[Getting Started](Getting-Started) · [Taming](Taming) · [Items](Items) · [Golden Treats](Golden-Treats) · [Collars](Collars) · [Combat Modes](Combat-Modes) · [Pet Behavior](Pet-Behavior) · [Pet Inventory](Pet-Inventory) · [Riding](Riding) · [Collar Respawn](Collar-Respawn) · [Golden Dragon Egg](Golden-Dragon-Egg)

**Reference**
[Controls](Controls) · [Commands](Commands) · [Configuration](Configuration) · [Custom Treat Tags](Custom-Treat-Tags) · [Chat Messages](Chat-Messages) · [Compatibility](Compatibility) · [Technical Reference](Technical-Reference) · [FAQ](FAQ)
