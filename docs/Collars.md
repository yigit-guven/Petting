# Collars

**Collars** are wearable pet accessories that store individualized tactical settings and an optional respawn binding.

There are 16 color variants matching the 16 Minecraft dyes. All collar variants share the same functionality, differing only in visual strap color and item registry name.

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/collar(red).png" width="32" height="32" alt="Red Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/orange_collar.png" width="32" height="32" alt="Orange Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/yellow_collar.png" width="32" height="32" alt="Yellow Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/lime_collar.png" width="32" height="32" alt="Lime Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/green_collar.png" width="32" height="32" alt="Green Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/cyan_collar.png" width="32" height="32" alt="Cyan Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/light_blue_collar.png" width="32" height="32" alt="Light Blue Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/blue_collar.png" width="32" height="32" alt="Blue Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/purple_collar.png" width="32" height="32" alt="Purple Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/magenta_collar.png" width="32" height="32" alt="Magenta Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/pink_collar.png" width="32" height="32" alt="Pink Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/white_collar.png" width="32" height="32" alt="White Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/light_gray_collar.png" width="32" height="32" alt="Light Gray Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/gray_collar.png" width="32" height="32" alt="Gray Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/black_collar.png" width="32" height="32" alt="Black Collar">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/brown_collar.png" width="32" height="32" alt="Brown Collar">
</p>

---

## Crafting & Dyeing

### Crafting
All collars can be crafted on a Crafting Table using 5 Leather, 1 Dye of the desired color, and 1 Gold Nugget:

```text
[Leather] [Leather] [Leather]
[Leather] [ Dye  ] [Leather]
[       ] [Nugget] [       ]
```

- **Top Row**: 3x Leather (`#c:leathers`)
- **Middle Row**: 1x Leather, 1x Dye (`#c:dyes/<color>`), 1x Leather
- **Bottom Row**: Empty, 1x Gold Nugget (`#c:nuggets/gold`), Empty
- Crafting with Red Dye yields `petting:collar`. Crafting with any other color yields `petting:<color>_collar`.

### Re-Dyeing via Crafting Grid
Any existing collar can be dyed to a different color by combining it shapelessly with any dye in a crafting grid:
- **Input**: 1x Any Collar (`#petting:collars`) + 1x Dye (`#c:dyes/<color>`)
- **Output**: 1x Collar of the corresponding color.

### In-World Dyeing
If an owned pet is currently wearing a collar, you can change its color directly in-world without opening any menus:
- Hold the new dye in your main hand.
- Right-click your pet.
- If the equipped collar is a different color, 1 dye is consumed, and the equipped collar changes color while **preserving all existing collar settings and respawn bindings**.

---

## Equipping a Collar

There are two ways to equip a collar onto an owned pet:

1. **Direct Right-Click**: Hold any collar in your main hand and right-click your owned pet. If the pet does not already have a collar equipped (and `enableCollarSlot` is enabled in configuration), the collar is consumed and equipped immediately.
2. **Pet Inventory GUI**: Press `Shift + Right-Click` on your pet (or use the configured *Open Pet Inventory* key) to open its inventory screen, then place the collar into the dedicated **Collar Slot** on the right side of the screen.

When a pet with an equipped collar dies and cannot respawn, the collar drops at the pet's death location.

---

## Visual Rendering

When equipped on a pet, the collar is rendered directly onto the model in the game world via `PetCollarLayer`:
- **Custom 3D Geometry**: A collar strap wrapped around the base of the mob's head model and a dangling golden medallion in front.
- **Color Tinting**: The strap texture is dynamically tinted to match the collar's dye color.
- **Enchantment Foil**: If the collar is bound to a valid respawn point (bed or cushion), the collar renders with an animated purple enchantment glint.
- **Universal Attachment**: The collar layer is attached to all living entity renderers across the game.

---

## Collar Settings & Configuration

When a collar is equipped, opening the pet inventory reveals the **Collar Tab** on the left side of the GUI. This allows configuring:

| Setting | Type | Range / Options | Description |
|---|---|---|---|
| **Combat Mode** | Cycle Button | Passive, Defensive, Aggressive, Wander, Guard | Controls the pet's AI combat logic. (See [Combat Modes](Combat-Modes)). |
| **Follow / Wander / Patrol Distance** | Slider | `minFollowDistance` to `maxFollowDistance` (default 2 to 32 blocks) | Distance before the pet begins following (in Follow modes), maximum roam distance from post (in Wander mode), or monster detection patrol radius (in Guard mode). |
| **Teleport Distance** | Slider | `minTeleportDistance` to `maxTeleportDistance` (default 8 to 64 blocks) | Distance from the owner before the pet teleports to owner's position. |
| **Teleport** | Toggle | ON / OFF | When OFF, the pet will never teleport to the owner, only walking/flying on foot. |
| **Prefer Ranged** | Toggle | ON / OFF | When ON and holding a bow or crossbow, the pet stays at a distance and shoots arrows instead of melee attacking. |
| **Defend Other Pets** | Toggle | ON / OFF | When ON, the pet rushes to defend any fellow pet belonging to the same owner when attacked. |
| **Retaliate** | Toggle | ON / OFF | When ON, the pet immediately targets whatever entity attacks it. |
| **Reset Defaults** | Button | Click | Resets all settings on the equipped collar back to the server configuration default values. |

These settings are stored permanently on the collar item stack in its `petting:collar_settings` data component (`net.yigitguven.petting.data.CollarSettings`). If you remove the collar and place it on another pet, the settings transfer with the collar.

---

## Respawn Point Binding

Right-clicking a placed bed (`#minecraft:beds`) or cushion (`#c:cushions`) with a collar in hand binds that collar to that location:
- An enchant particle effect and chime sound play.
- The item gains an enchantment glint and tooltip line `Bound to bed` or `Bound to cushion`.
- When an equipped pet dies, it respawns at this location. See [Collar Respawn](Collar-Respawn) for full mechanics.

---

*Related Pages*: [Combat Modes](Combat-Modes) · [Pet Inventory](Pet-Inventory) · [Collar Respawn](Collar-Respawn) · [Configuration](Configuration)
