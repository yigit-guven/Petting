# Golden Treats

**Golden Treats** are special food items used to tame mobs and heal tamed pets. There are nine distinct treats in **Petting**, each targeting different categories of mobs through data-driven entity type tags (`petting:tames_with/<treat>`).

All golden treats share several common characteristics in code:
- **Max Stack Size**: 64 (`PettingModItems.java`)
- **Rarity**: `Rarity.RARE` (aqua item name)
- **Visual Glint**: Permanent enchantment foil effect (`isFoil` returns `true`)
- **Creative Tab**: Located in the "Petting" tab (`itemGroup.petting`)
- **Healing Power**: When right-clicked on an injured pet you own, consumes 1 treat and restores **50% of the pet's maximum health** (or 25% for a Golden Ender Dragon using a Golden Star).

---

## Treat Selection & Fallback Rules

When you right-click a mob with a treat, `PetEggHelper.canTame(mob, treat)` evaluates whether the treat matches:

1. **Tag Match**: If the mob's entity type is listed in `petting:tames_with/<treat>`, the treat is valid.
2. **Untameable Check**: If the entity is in the server configuration `untameableEntities` list, no treat can tame it.
3. **Ender Dragon Check**: An untamed Ender Dragon cannot be tamed directly by holding a treat. (See [Golden Dragon Egg](Golden-Dragon-Egg) for the summoning ritual).
4. **Other Tag Check**: If the mob is explicitly registered in *another* treat's `tames_with` tag, it will **not** accept a mismatched treat.
5. **Fallback Matching** (for modded or untagged mobs):
   - **Max Health > 300**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_star.png" width="20" height="20" alt="Golden Star"> **Golden Star**.
   - **Hostile Mob (`Enemy`)**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_bone.png" width="20" height="20" alt="Golden Bone"> **Golden Bone**.
   - **All Other Mobs**: Requires <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="20" height="20" alt="Golden Wheat"> **Golden Wheat**.

---

## Treat Catalog

| Texture | Treat | Recipe Summary | Target Tag | Default Mobs / Categories |
|:---:|---|---|---|---|
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="32" height="32" alt="Golden Wheat"> | [Golden Wheat](#golden-wheat) | 8 Gold Nuggets + 1 Wheat | `petting:tames_with/golden_wheat` | Farm animals, herbivores, mounts, `#can_equip_saddle`, `#followable_friendly_mobs` |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_star.png" width="32" height="32" alt="Golden Star"> | [Golden Star](#golden-star) | 8 Gold Blocks + 1 Nether Star | `petting:tames_with/golden_star` | Bosses (`#c:bosses`), `#raiders`, Wither, Warden, Elder Guardian, Ravager, Iron Golem |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_flesh.png" width="32" height="32" alt="Golden Flesh"> | [Golden Flesh](#golden-flesh) | 8 Gold Ingots + 1 Rotten Flesh | `petting:tames_with/golden_flesh` | `#undead`, `#zombies`, Phantom, Zoglin, Zombie Horse, Zombified Piglin |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_bone.png" width="32" height="32" alt="Golden Bone"> | [Golden Bone](#golden-bone) | 4 Nuggets + 4 Ingots + 1 Bone | `petting:tames_with/golden_bone` | `#skeletons`, Wolf, Skeleton Horse |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fish.png" width="32" height="32" alt="Golden Fish"> | [Golden Fish](#golden-fish) | 4 Nuggets + 4 Ingots + 1 Fish (`#minecraft:fishes`) | `petting:tames_with/golden_fish` | Cats, Ocelots, Polar Bears, Dolphins, Axolotls, Guardians, Fish |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_kelp.png" width="32" height="32" alt="Golden Kelp"> | [Golden Kelp](#golden-kelp) | 8 Gold Nuggets + 1 Dried Kelp | `petting:tames_with/golden_kelp` | `#aquatic`, Turtles, Squids, Glow Squids |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fungus.png" width="32" height="32" alt="Golden Fungus"> | [Golden Fungus](#golden-fungus) | 4 Nuggets + 4 Ingots + 1 Crimson or Warped Fungus | `petting:tames_with/golden_fungus` | Striders, Hoglins, Piglins, Piglin Brutes, Blazes, Ghasts |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_eye.png" width="32" height="32" alt="Golden Eye"> | [Golden Eye](#golden-eye) | 4 Nuggets + 4 Ingots + 1 Eye of Ender | `petting:tames_with/golden_eye` | `#arthropod`, Endermen, Endermites, Shulkers, Bats, Allays, Vexes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_slime.png" width="32" height="32" alt="Golden Slime"> | [Golden Slime](#golden-slime) | 4 Nuggets + 4 Ingots + 1 Slimeball or Magma Cream | `petting:tames_with/golden_slime` | Slimes, Magma Cubes, Frogs, Tadpoles |

---

## Golden Wheat

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="64" height="64" alt="Golden Wheat">

- **Item ID**: `petting:golden_wheat`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Nugget (`#c:nuggets/gold`)
  - Center: 1x Wheat (`#c:crops/wheat`)
- **Entity Tag**: `petting:tames_with/golden_wheat`
- **Default Entities**:
  - Tags included: `#minecraft:can_equip_saddle`, `#minecraft:followable_friendly_mobs`
  - Explicit mobs: `minecraft:cow`, `minecraft:sheep`, `minecraft:pig`, `minecraft:chicken`, `minecraft:goat`, `minecraft:mooshroom`, `minecraft:horse`, `minecraft:donkey`, `minecraft:mule`, `minecraft:camel`, `minecraft:llama`, `minecraft:trader_llama`, `minecraft:rabbit`, `minecraft:sniffer`, `minecraft:armadillo`
- **Fallback**: Default fallback treat for any untagged passive mob.

---

## Golden Star

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_star.png" width="64" height="64" alt="Golden Star">

- **Item ID**: `petting:golden_star`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Block (`#c:storage_blocks/gold`)
  - Center: 1x Nether Star (`minecraft:nether_star`)
- **Entity Tag**: `petting:tames_with/golden_star`
- **Default Entities**:
  - Tags included: `#c:bosses`, `#minecraft:raiders`
  - Explicit mobs: `minecraft:ender_dragon`, `minecraft:wither`, `minecraft:warden`, `minecraft:elder_guardian`, `minecraft:ravager`, `minecraft:iron_golem`
- **Special Uses**:
  - Right-click on a placed vanilla Dragon Egg block to infuse it into a **Golden Dragon Egg**.
  - Right-click an owned Golden Ender Dragon to restore **25% of its maximum health**.
- **Fallback**: Default fallback treat for any untagged mob with maximum health exceeding 300.0.

---

## Golden Flesh

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_flesh.png" width="64" height="64" alt="Golden Flesh">

- **Item ID**: `petting:golden_flesh`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Ingot (`#c:ingots/gold`)
  - Center: 1x Rotten Flesh (`minecraft:rotten_flesh`)
- **Entity Tag**: `petting:tames_with/golden_flesh`
- **Default Entities**:
  - Tags included: `#minecraft:undead`, `#minecraft:zombies`
  - Explicit mobs: `minecraft:phantom`, `minecraft:zoglin`, `minecraft:zombie_horse`, `minecraft:zombified_piglin`

---

## Golden Bone

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_bone.png" width="64" height="64" alt="Golden Bone">

- **Item ID**: `petting:golden_bone`
- **Crafting Recipe**:
  - Pattern: `NGN` / `GXG` / `NGN`
  - Corners (`N`): 4x Gold Nugget (`#c:nuggets/gold`)
  - Edges (`G`): 4x Gold Ingot (`#c:ingots/gold`)
  - Center (`X`): 1x Bone (`#c:bones`)
- **Entity Tag**: `petting:tames_with/golden_bone`
- **Default Entities**:
  - Tags included: `#minecraft:skeletons`
  - Explicit mobs: `minecraft:wolf`, `minecraft:skeleton_horse`
- **Fallback**: Default fallback treat for any untagged hostile mob (`Enemy`).

---

## Golden Fish

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fish.png" width="64" height="64" alt="Golden Fish">

- **Item ID**: `petting:golden_fish`
- **Crafting Recipe**:
  - Pattern: `NGN` / `GXG` / `NGN`
  - Corners (`N`): 4x Gold Nugget (`#c:nuggets/gold`)
  - Edges (`G`): 4x Gold Ingot (`#c:ingots/gold`)
  - Center (`X`): 1x Any Fish (`#minecraft:fishes`)
- **Entity Tag**: `petting:tames_with/golden_fish`
- **Default Entities**:
  - Explicit mobs: `minecraft:cat`, `minecraft:ocelot`, `minecraft:polar_bear`, `minecraft:dolphin`, `minecraft:axolotl`, `minecraft:guardian`, `minecraft:cod`, `minecraft:salmon`, `minecraft:tropical_fish`, `minecraft:pufferfish`

---

## Golden Kelp

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_kelp.png" width="64" height="64" alt="Golden Kelp">

- **Item ID**: `petting:golden_kelp`
- **Crafting Recipe**:
  - Surrounding: 8x Gold Nugget (`#c:nuggets/gold`)
  - Center: 1x Dried Kelp (`minecraft:dried_kelp`)
- **Entity Tag**: `petting:tames_with/golden_kelp`
- **Default Entities**:
  - Tags included: `#minecraft:aquatic`
  - Explicit mobs: `minecraft:turtle`, `minecraft:squid`, `minecraft:glow_squid`

---

## Golden Fungus

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fungus.png" width="64" height="64" alt="Golden Fungus">

- **Item ID**: `petting:golden_fungus`
- **Crafting Recipes**: (Two recipes registered in data)
  - Pattern: `NGN` / `GXG` / `NGN`
  - Corners (`N`): 4x Gold Nugget (`#c:nuggets/gold`)
  - Edges (`G`): 4x Gold Ingot (`#c:ingots/gold`)
  - Center (`X`): 1x Crimson Fungus (`minecraft:crimson_fungus`) OR 1x Warped Fungus (`minecraft:warped_fungus`)
- **Entity Tag**: `petting:tames_with/golden_fungus`
- **Default Entities**:
  - Explicit mobs: `minecraft:strider`, `minecraft:hoglin`, `minecraft:piglin`, `minecraft:piglin_brute`, `minecraft:blaze`, `minecraft:ghast`

---

## Golden Eye

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_eye.png" width="64" height="64" alt="Golden Eye">

- **Item ID**: `petting:golden_eye`
- **Crafting Recipe**:
  - Pattern: `NGN` / `GXG` / `NGN`
  - Corners (`N`): 4x Gold Nugget (`#c:nuggets/gold`)
  - Edges (`G`): 4x Gold Ingot (`#c:ingots/gold`)
  - Center (`X`): 1x Eye of Ender (`minecraft:ender_eye`)
- **Entity Tag**: `petting:tames_with/golden_eye`
- **Default Entities**:
  - Tags included: `#minecraft:arthropod`
  - Explicit mobs: `minecraft:enderman`, `minecraft:endermite`, `minecraft:shulker`, `minecraft:bat`, `minecraft:allay`, `minecraft:vex`

---

## Golden Slime

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_slime.png" width="64" height="64" alt="Golden Slime">

- **Item ID**: `petting:golden_slime`
- **Crafting Recipes**: (Two recipes registered in data)
  - Pattern: `NGN` / `GXG` / `NGN`
  - Corners (`N`): 4x Gold Nugget (`#c:nuggets/gold`)
  - Edges (`G`): 4x Gold Ingot (`#c:ingots/gold`)
  - Center (`X`): 1x Slimeball (`#c:slimeballs`) OR 1x Magma Cream (`minecraft:magma_cream`)
- **Entity Tag**: `petting:tames_with/golden_slime`
- **Default Entities**:
  - Explicit mobs: `minecraft:slime`, `minecraft:magma_cube`, `minecraft:frog`, `minecraft:tadpole`

---

*Related Pages*: [Taming](Taming) · [Custom Treat Tags](Custom-Treat-Tags) · [Items](Items)
