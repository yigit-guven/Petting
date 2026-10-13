# Compatibility

**Petting** is designed from the ground up for maximum compatibility with vanilla Minecraft, modded ecosystems, and recipe viewers.

---

## Just Enough Items (JEI) Integration

Petting includes dedicated, native Just Enough Items (JEI) integration (`PettingJeiPlugin.java`):

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="32" height="32" alt="Golden Wheat">
</p>

- **Recipe Category**: **Pet Taming** (`petting:taming`, title: `petting.jei.taming.title`).
- **Category Icon**: Golden Wheat.
- **Recipe Catalysts**: All nine Golden Treats are registered as catalysts. Clicking any Golden Treat in JEI displays its taming category.
- **Recipe Layout (`PetTamingCategory`)**:
  - **Input Slot (Left)**: The Golden Treat.
  - **Output Grid (Right)**: Up to 18 slots (3 rows of 6) displaying the **Spawn Egg** corresponding to each mob tamed by that treat.
  - **Dynamic Pagination**: If a treat tag contains more mobs than fit in the grid, the slots automatically cycle through all eligible mobs.
  - **Custom Representatives**: Entities without spawn eggs (such as the Ender Dragon) use custom representative items (`PetEggHelper`), displaying the Golden Dragon Egg.
- **Hidden Items**: In accordance with the common conventions (`c:hidden_from_recipe_viewers`), the **Golden Paw** (creative tab icon only) and **Golden Dragon Egg** (ritual only) are hidden from default JEI recipe lists.

---

## Modded Mobs Compatibility

Petting does not rely on hardcoded mob lists for core features. Any modded entity extending standard Minecraft entity classes (`Mob`, `LivingEntity`, `PathfinderMob`) is fully compatible:

### 1. Automatic Fallback Matching
Even if a modded mob is not registered in any of Petting's tags, it can still be tamed immediately using the automatic fallback hierarchy:
- **Max Health > 300.0 HP** $\rightarrow$ Requires **Golden Star**.
- **Hostile Mobs (`Enemy`)** $\rightarrow$ Requires **Golden Bone**.
- **All Other Mobs** $\rightarrow$ Requires **Golden Wheat**.

### 2. Universal Equipment & Armor
- Humanoid modded mobs automatically gain the 4-slot armor system (Head, Chest, Legs, Feet) and hand slots.
- Modded mounts and quadrupeds gain the Body Slot (compatible with modded horse/wolf armors).

### 3. Universal Collar Rendering
`PetCollarLayer` is registered on **all** `LivingEntity` renderers across the game. When a collar is equipped on a modded mob, the 3D collar strap and golden medallion attach to its head model automatically.

### 4. Modded Riding
Any modded mob can be ridden (subject to the server `whitelist`/`blacklist`). Aerial and aquatic modded mobs are dynamically classified by checking movement controllers, water navigation, or `NoGravity` flags.

---

## Bed & Cushion Mod Compatibility

When binding collars for the [Collar Respawn](Collar-Respawn) mechanic:
- **Beds**: Matches any block belonging to the `#minecraft:beds` tag, any block extending `BedBlock`, or any block registry identifier containing `"bed"`.
- **Cushions**: Matches any block or entity belonging to the `#c:cushions` common convention tag, or any registry identifier containing `"cushion"`.

---

## Tag Conventions

Petting adheres to standard Common Tags (`#c:` namespace) and Minecraft Tags (`#minecraft:`):

| Tag | Purpose in Petting |
|---|---|
| `#c:bosses` | Included in `petting:tames_with/golden_star` |
| `#c:cushions` | Accepted as respawn anchors for collars |
| `#c:hidden_from_recipe_viewers` | Hides Golden Paw and Golden Dragon Egg from recipe lookups |
| `#c:dyes/<color>` | Used in collar crafting and dyeing recipes |
| `#c:leathers`, `#c:nuggets/gold`, `#c:ingots/gold` | Used in collar and treat crafting recipes |
| `#minecraft:fishes` | Accepted in Golden Fish recipe |
| `#minecraft:can_equip_saddle` | Included in `petting:tames_with/golden_wheat` |
| `#minecraft:undead`, `#minecraft:zombies` | Included in `petting:tames_with/golden_flesh` |
| `#minecraft:skeletons` | Included in `petting:tames_with/golden_bone` |
| `#minecraft:aquatic` | Included in `petting:tames_with/golden_kelp` |
| `#minecraft:arthropod` | Included in `petting:tames_with/golden_eye` |

---

*Related Pages*: [Custom Treat Tags](Custom-Treat-Tags) · [Golden Treats](Golden-Treats) · [Collar Respawn](Collar-Respawn)
