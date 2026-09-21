## Changelog

### 4.2.3-beta.5

#### Added & Implemented
- **Custom Taming Items Support**: Implemented `CUSTOM_TAMING_ITEMS` config processing (`entity_id|item_id`), enabling custom taming items even when `allowGoldenWheat` is set to false.
- **Immediate Persistence**: Newly tamed mobs are now immediately marked as persistence-required to prevent accidental despawning before the first server tick.
- **Axe Mineable Tag**: Tagged `petting:pet_bed` under `#minecraft:mineable/axe` so axes harvest pet beds quickly.
- **Non-Armor Equipables Support**: `EquipmentSlotHandler` and pet inventory quick-move now support all wearable/equipable items (Elytra, carved pumpkins, mob heads, etc.) via `mob.getEquipmentSlotForItem(...)`.
- **Conversion Data Synchronization**: Mob conversions (e.g. Zombie into Drowned) now preserve `ignoreWhistle`, custom control mappings (`control_right_click`, `control_shift_right_click`), and automatically transfer pet inventory items and equipped gear.

#### Fixed
- **1.21 Data Pack Directory Paths**: Migrated `loot_tables` to singular `loot_table` (`data/petting/loot_table/blocks/pet_bed.json`) so breaking pet beds drops the item correctly in 1.21.
- **1.21 Advancements Directory Path**: Migrated `advancements` to singular `advancement` (`data/petting/advancement/recipes/golden_wheat_recipe.json`).
- **Owner Hurt Pet Protection**: Fixed `PetDamageHandler` so `allowOwnerToHurtPets = false` properly prevents players from damaging their own pets while standing or walking (previously only prevented while riding) and prevents projectile friendly fire.
- **Duplicate Environmental Damage Checks**: Corrected copy-pasted `IS_FIRE` damage tags in `PetAttackLogic` to properly check `IS_DROWNING` and `IS_FREEZING`.
- **Duplicate Sitting Heal**: Removed redundant sitting heal execution in `FollowOwnerOrTeleport` that previously caused double-healing every tick interval.
- **Pet Bed Respawn Validation**: Checked for the physical existence of the pet bed before respawning. If destroyed or missing, the owner is alerted and stale bed coordinates are cleared rather than respawning in thin air.
- **Pet Releasing Data Cleanup**: Cleaned up bed coordinates, control mappings, and tamable entity status when releasing a pet with shears.
- **Global Whistle Activation**: Required sneaking (`Shift + Right-Click`) to use the global whistle/goat horn as documented, avoiding unintentional summoning during regular horn use.
- **Tick Performance Optimization**: Optimized `GoldenWheatItemInHandTickProcedure` to bypass 12-block entity searches when players are not holding Golden Wheat.
- **1.21.1 Pack Format**: Updated `pack.mcmeta` with Minecraft 1.21.1 pack formats (`48` data, `34` resource).
- **Cleaned Up Deprecations**: Removed deprecated `EventBusSubscriber.Bus.GAME` references across event handlers.