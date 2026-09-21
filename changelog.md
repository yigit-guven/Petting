# Petting 4.2.3-beta.6

## Features & Additions:
- **Pet Bed Recipe**: Added the missing shaped crafting recipe for the Pet Bed (`data/petting/recipes/pet_bed.json`) using wool and planks.
- **Custom Taming Items Support**: Fully wired the `CUSTOM_TAMING_ITEMS` configuration option (`entity_registry|item_registry`), allowing modpack authors to define custom items to tame specific entities even when Golden Wheat is disabled.
- **Wearable Items Equipment Support**: Expanded pet equipment slots to support non-armor wearable items (such as elytra, carved pumpkins, and mob heads) via `LivingEntity.getEquipmentSlotForItem` for both direct placement and shift-click quick move.

## Bug Fixes:
- **Owner Friendly-Fire Fix**: Prevented owners from inadvertently damaging their pets while on foot or shooting projectiles when `ALLOW_OWNER_TO_HURT_PETS` is false (previously only checked while riding).
- **Environmental Invulnerability Fix**: Replaced duplicate `IS_FIRE` damage tags in `PetAttackLogic` with `IS_DROWNING` and `IS_FREEZING` to protect custom pets from drowning or freezing to death.
- **Duplicate Sitting Heal Fix**: Removed redundant sit-healing loop in `FollowOwnerOrTeleport` that bypassed the `SIT_HEAL_ENABLED` config setting (sit-healing is properly governed by `EntityTickUpdateProcedure`).
- **Pet Bed Respawn Validation**: Added block validation in `PetDeathHandlerProcedure` to verify that the Pet Bed still exists in the destination world. If destroyed or broken, the owner is notified, bed coordinates are cleaned up, and standard death handling proceeds.
- **Mob Conversion Sync & Inventory Transfer**: Ensured `ignoreWhistle`, `control_right_click`, and `control_shift_right_click` are preserved during mob conversion, and transferred pet inventory capability contents between original and converted entities.
- **Pet Release Data Cleanup**: Releasing a pet with shears now cleans up stored pet bed coordinates (`pet_bed_loc_x/y/z/dim`) and control mappings, and untames vanilla `TamableAnimal` instances.
- **Immediate Tamed Persistence**: Ensured newly tamed mobs immediately receive `setPersistenceRequired()`, preventing them from despawning.
- **Whistle Crouch Requirement**: Enforced sneaking requirement (`player.isShiftKeyDown()`) when sounding the Goat Horn / Whistle tool, matching configuration docs and preventing unintended mass summons.

## Performance Optimizations:
- **Temptation Tick Optimization**: Players now cache `pettingHoldingGoldenWheat` so the server skips entity AABB scans when the player is not holding golden wheat, and search radius is tightened to 12 blocks.