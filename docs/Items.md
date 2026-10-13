# Items and Blocks

Everything the mod registers in the item and block registries. All registry ids use the `petting` namespace. The textures shown are the files from `assets/petting/textures` in the repository.

## Overview

| Texture | Name | Registry id | Rarity | Enchantment glint | In creative tab |
|---|---|---|---|---|---|
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_paw.png" width="32" alt="Golden Paw"> | [Golden Paw](#golden-paw) | `petting:golden_paw` | default | yes | no |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="32" alt="Golden Wheat"> | [Golden Wheat](Golden-Treats#golden-wheat) | `petting:golden_wheat` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_star.png" width="32" alt="Golden Star"> | [Golden Star](Golden-Treats#golden-star) | `petting:golden_star` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_flesh.png" width="32" alt="Golden Flesh"> | [Golden Flesh](Golden-Treats#golden-flesh) | `petting:golden_flesh` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_bone.png" width="32" alt="Golden Bone"> | [Golden Bone](Golden-Treats#golden-bone) | `petting:golden_bone` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fish.png" width="32" alt="Golden Fish"> | [Golden Fish](Golden-Treats#golden-fish) | `petting:golden_fish` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_kelp.png" width="32" alt="Golden Kelp"> | [Golden Kelp](Golden-Treats#golden-kelp) | `petting:golden_kelp` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_fungus.png" width="32" alt="Golden Fungus"> | [Golden Fungus](Golden-Treats#golden-fungus) | `petting:golden_fungus` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_eye.png" width="32" alt="Golden Eye"> | [Golden Eye](Golden-Treats#golden-eye) | `petting:golden_eye` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_slime.png" width="32" alt="Golden Slime"> | [Golden Slime](Golden-Treats#golden-slime) | `petting:golden_slime` | Rare | yes | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/collar.png" width="32" alt="Red Collar"> | [Collars (16 colors)](Collars) | `petting:collar` (red) and `petting:<color>_collar` | default | only while bound to a respawn point | yes |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/block/golden_dragon_egg.png" width="32" alt="Golden Dragon Egg"> | [Golden Dragon Egg](Golden-Dragon-Egg) | `petting:golden_dragon_egg` (block and item) | Epic | no | no |

The creative tab **Petting** (`petting:petting`) contains the nine golden treats and the sixteen collars, in that order. Its icon is the Golden Paw.

## Golden Paw

<img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_paw.png" width="96" alt="Golden Paw texture">

| Property | Value | Source |
|---|---|---|
| Registry id | `petting:golden_paw` | `PettingModItems.java` |
| Class | `GoldenPawItem` | `GoldenPawItem.java` |
| Enchantment glint | Always | `GoldenPawItem.isFoil` |
| Enabled | `isEnabled(...)` always returns `false` | `GoldenPawItem.isEnabled` |
| Recipe | None | `data/petting/recipe` |
| Creative tab entry | No, it is only the tab icon | `PettingModTabs.java` |
| Recipe viewers | Hidden through the item tag `c:hidden_from_recipe_viewers` | `data/c/tags/item` |

The Golden Paw has no gameplay function in the code. It exists as the creative tab icon.

## Golden treats

Nine items that all use the `GoldenTreatItem` class: rarity **Rare**, permanent enchantment glint. Each one is linked to an entity-type tag. See [Golden Treats](Golden-Treats) for recipes and tameable mobs.

## Collars

Sixteen items using the `CollarItem` class. Maximum stack size is **1**. Red is the base item `petting:collar`; the other fifteen are `petting:<dye color>_collar`. See [Collars](Collars).

All collars belong to the item tag `petting:collars`, which the dyeing recipes use as an ingredient.

## Golden Dragon Egg

A `BlockItem` for the `petting:golden_dragon_egg` block (rarity Epic). It has no crafting recipe and is not in the creative tab. It is created by right-clicking a vanilla Dragon Egg with a Golden Star. See [Golden Dragon Egg](Golden-Dragon-Egg).

| Block property | Value | Source |
|---|---|---|
| Base properties | Full copy of the vanilla Dragon Egg block properties | `PettingModBlocks.java` |
| Light level | 6 | `PettingModBlocks.java` |
| Collision / outline shape | Column, 14 wide, from y=0 to y=16 | `GoldenDragonEggBlock.SHAPE` |
| Behavior | Falling block that teleports when clicked or attacked | `GoldenDragonEggBlock.java` |
| Block entity | `petting:golden_dragon_egg` (stores owner UUID and name) | `GoldenDragonEggBlockEntity.java` |
| Drop | The Golden Dragon Egg item, with owner data copied from the block entity | `loot_table/blocks/golden_dragon_egg.json` |

## Other registered content

| Type | Id | Notes |
|---|---|---|
| Menu | `petting:pet_inventory` | The [Pet Inventory](Pet-Inventory) screen |
| Creative tab | `petting:petting` | Title "Petting" |
| Data components | `petting:collar_settings`, `petting:collar_respawn_point`, `petting:dragon_egg_owner`, `petting:dragon_egg_owner_name` | See [Technical Reference](Technical-Reference) |
| Attachment | `petting:pet_data` | Per-entity pet state |
| Item tag | `petting:collars` | All 16 collars |
| Item tag | `c:hidden_from_recipe_viewers` | Golden Paw and Golden Dragon Egg |
| Entity-type tags | `petting:tames_with/<treat>` (9) | See [Custom Treat Tags](Custom-Treat-Tags) |
