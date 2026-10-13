# Golden Treats

**Golden Treats** are special food items used to tame mobs and heal tamed pets. There are nine distinct treats in **Petting**, each targeting different categories of mobs through data-driven entity type tags (`petting:tames_with/<treat>`).

All golden treats share several common characteristics:
- **Max Stack Size**: 64
- **Rarity**: **Rare** (aqua item name)
- **Visual Glint**: Permanent enchantment foil effect
- **Creative Tab**: Located in the "Petting" creative tab
- **Healing Power**: When right-clicked on an injured pet you own, consumes 1 treat and restores **50% of the pet's maximum health** (or 25% for a Golden Ender Dragon using a Golden Star).

---

## Treat Selection & Fallback Rules

When you right-click a mob with a treat, the game evaluates whether the treat matches:

1. **Tag Match**: If the mob's entity type is listed in `petting:tames_with/<treat>`, the treat is valid.
2. **Untameable Check**: If the entity is in the server configuration `untameableEntities` list, no treat can tame it.
3. **Ender Dragon Check**: An untamed wild Ender Dragon cannot be tamed directly by holding a treat. (See [Golden Dragon Egg](Golden-Dragon-Egg) for the summoning ritual).
4. **Other Tag Check**: If the mob is explicitly registered in *another* treat's `tames_with` tag, it will **not** accept a mismatched treat.
5. **Universal Fallback Matching** (for modded or untagged mobs):
   - **Max Health > 300 HP**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="20" height="20" alt="Golden Star"> **Golden Star**.
   - **Hostile Mobs**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_bone.png" width="20" height="20" alt="Golden Bone"> **Golden Bone**.
   - **All Other Mobs**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="20" height="20" alt="Golden Wheat"> **Golden Wheat**.

---

## Treat Catalog

| Texture | Treat | Recipe Summary | Target Tag | Default Mobs / Categories |
|:---:|---|---|---|---|
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="32" height="32" alt="Golden Wheat"> | [Golden Wheat](#golden-wheat) | 8 Gold Nuggets + 1 Wheat | `petting:tames_with/golden_wheat` | Farm animals, herbivores, mounts, `#can_equip_saddle`, `#followable_friendly_mobs` |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="32" height="32" alt="Golden Star"> | [Golden Star](#golden-star) | 8 Gold Blocks + 1 Nether Star | `petting:tames_with/golden_star` | Bosses (`#c:bosses`), `#raiders`, Wither, Warden, Elder Guardian, Ravager, Iron Golem |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_flesh.png" width="32" height="32" alt="Golden Flesh"> | [Golden Flesh](#golden-flesh) | 8 Gold Ingots + 1 Rotten Flesh | `petting:tames_with/golden_flesh` | `#undead`, `#zombies`, Phantom, Zoglin, Zombie Horse, Zombified Piglin |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_bone.png" width="32" height="32" alt="Golden Bone"> | [Golden Bone](#golden-bone) | 4 Nuggets + 4 Ingots + 1 Bone | `petting:tames_with/golden_bone` | `#skeletons`, Wolf, Skeleton Horse |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_fish.png" width="32" height="32" alt="Golden Fish"> | [Golden Fish](#golden-fish) | 4 Nuggets + 4 Ingots + 1 Fish (`#minecraft:fishes`) | `petting:tames_with/golden_fish` | Cats, Ocelots, Polar Bears, Dolphins, Axolotls, Guardians, Fish |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_kelp.png" width="32" height="32" alt="Golden Kelp"> | [Golden Kelp](#golden-kelp) | 8 Gold Nuggets + 1 Dried Kelp | `petting:tames_with/golden_kelp` | `#aquatic`, Turtles, Squids, Glow Squids |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_fungus.png" width="32" height="32" alt="Golden Fungus"> | [Golden Fungus](#golden-fungus) | 4 Nuggets + 4 Ingots + 1 Crimson or Warped Fungus | `petting:tames_with/golden_fungus` | Striders, Hoglins, Piglins, Piglin Brutes, Blazes, Ghasts |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_eye.png" width="32" height="32" alt="Golden Eye"> | [Golden Eye](#golden-eye) | 4 Nuggets + 4 Ingots + 1 Eye of Ender | `petting:tames_with/golden_eye` | `#arthropod`, Endermen, Endermites, Shulkers, Bats, Allays, Vexes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_slime.png" width="32" height="32" alt="Golden Slime"> | [Golden Slime](#golden-slime) | 4 Nuggets + 4 Ingots + 1 Slimeball or Magma Cream | `petting:tames_with/golden_slime` | Slimes, Magma Cubes, Frogs, Tadpoles |

---

## Golden Wheat

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="64" height="64" alt="Golden Wheat">
</p>

- **Item ID**: `petting:golden_wheat`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Nugget
  - Center: 1x Wheat
- **Entity Tag**: `petting:tames_with/golden_wheat`
- **Default Entities**:
  - Tags included: `#minecraft:can_equip_saddle`, `#minecraft:followable_friendly_mobs`
  - Explicit mobs: `cow`, `sheep`, `pig`, `chicken`, `goat`, `mooshroom`, `horse`, `donkey`, `mule`, `camel`, `llama`, `trader_llama`, `rabbit`, `sniffer`, `armadillo`
- **Fallback**: Default fallback treat for any untagged passive mob.

---

## Golden Star

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="64" height="64" alt="Golden Star">
</p>

- **Item ID**: `petting:golden_star`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Block
  - Center: 1x Nether Star
- **Entity Tag**: `petting:tames_with/golden_star`
- **Default Entities**:
  - Tags included: `#c:bosses`, `#minecraft:raiders`
  - Explicit mobs: `ender_dragon`, `wither`, `warden`, `elder_guardian`, `ravager`, `iron_golem`
- **Special Uses**:
  - Right-click on a placed vanilla Dragon Egg block to infuse it into a **Golden Dragon Egg**.
  - Right-click an owned Golden Ender Dragon to restore **25% of its maximum health**.
- **Fallback**: Default fallback treat for any untagged mob with maximum health exceeding 300 HP.

---

## Golden Flesh

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_flesh.png" width="64" height="64" alt="Golden Flesh">
</p>

- **Item ID**: `petting:golden_flesh`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Ingot
  - Center: 1x Rotten Flesh
- **Entity Tag**: `petting:tames_with/golden_flesh`
- **Default Entities**:
  - Tags included: `#minecraft:undead`, `#minecraft:zombies`
  - Explicit mobs: `phantom`, `zoglin`, `zombie_horse`, `zombified_piglin`

---

## Golden Bone

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_bone.png" width="64" height="64" alt="Golden Bone">
</p>

- **Item ID**: `petting:golden_bone`
- **Crafting Recipe**:
  - Corners: 4x Gold Nugget
  - Edges: 4x Gold Ingot
  - Center: 1x Bone
- **Entity Tag**: `petting:tames_with/golden_bone`
- **Default Entities**:
  - Tags included: `#minecraft:skeletons`
  - Explicit mobs: `wolf`, `skeleton_horse`
- **Fallback**: Default fallback treat for any untagged hostile mob.

---

## Golden Fish

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_fish.png" width="64" height="64" alt="Golden Fish">
</p>

- **Item ID**: `petting:golden_fish`
- **Crafting Recipe**:
  - Corners: 4x Gold Nugget
  - Edges: 4x Gold Ingot
  - Center: 1x Any Fish (`#minecraft:fishes`)
- **Entity Tag**: `petting:tames_with/golden_fish`
- **Default Entities**:
  - Explicit mobs: `cat`, `ocelot`, `polar_bear`, `dolphin`, `axolotl`, `guardian`, `cod`, `salmon`, `tropical_fish`, `pufferfish`

---

## Golden Kelp

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_kelp.png" width="64" height="64" alt="Golden Kelp">
</p>

- **Item ID**: `petting:golden_kelp`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Nugget
  - Center: 1x Dried Kelp
- **Entity Tag**: `petting:tames_with/golden_kelp`
- **Default Entities**:
  - Tags included: `#minecraft:aquatic`
  - Explicit mobs: `turtle`, `squid`, `glow_squid`

---

## Golden Fungus

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_fungus.png" width="64" height="64" alt="Golden Fungus">
</p>

- **Item ID**: `petting:golden_fungus`
- **Crafting Recipes**:
  - Corners: 4x Gold Nugget
  - Edges: 4x Gold Ingot
  - Center: 1x Crimson Fungus OR 1x Warped Fungus
- **Entity Tag**: `petting:tames_with/golden_fungus`
- **Default Entities**:
  - Explicit mobs: `strider`, `hoglin`, `piglin`, `piglin_brute`, `blaze`, `ghast`

---

## Golden Eye

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_eye.png" width="64" height="64" alt="Golden Eye">
</p>

- **Item ID**: `petting:golden_eye`
- **Crafting Recipe**:
  - Corners: 4x Gold Nugget
  - Edges: 4x Gold Ingot
  - Center: 1x Eye of Ender
- **Entity Tag**: `petting:tames_with/golden_eye`
- **Default Entities**:
  - Tags included: `#minecraft:arthropod`
  - Explicit mobs: `enderman`, `endermite`, `shulker`, `bat`, `allay`, `vex`

---

## Golden Slime

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_slime.png" width="64" height="64" alt="Golden Slime">
</p>

- **Item ID**: `petting:golden_slime`
- **Crafting Recipes**:
  - Corners: 4x Gold Nugget
  - Edges: 4x Gold Ingot
  - Center: 1x Slimeball OR 1x Magma Cream
- **Entity Tag**: `petting:tames_with/golden_slime`
- **Default Entities**:
  - Explicit mobs: `slime`, `magma_cube`, `frog`, `tadpole`

---

*Related Pages*: [Taming](Taming) · [Custom Treat Tags](Custom-Treat-Tags) · [Items](Items)
