# Technical Reference

This page provides an architectural and registry-level reference for developers, pack makers, and server administrators.

---

## Mod Identity & Environment

| Property | Value | Source File |
|---|---|---|
| **Mod ID** | `petting` | `Petting.java` |
| **Mod Loader** | `javafml` (NeoForge) | `neoforge.mods.toml` |
| **Dependencies** | `neoforge`, `minecraft` | `neoforge.mods.toml` |
| **Network Protocol Version** | `"1"` | `PettingNetwork.java` |

---

## Registry Entries

### Items (`Registries.ITEM`)
- `petting:golden_paw` (Creative tab icon, `isEnabled = false`)
- `petting:golden_wheat`
- `petting:golden_star`
- `petting:golden_flesh`
- `petting:golden_bone`
- `petting:golden_fish`
- `petting:golden_kelp`
- `petting:golden_fungus`
- `petting:golden_eye`
- `petting:golden_slime`
- `petting:collar` (Red collar)
- 15 dyed variants: `petting:white_collar`, `petting:orange_collar`, `petting:magenta_collar`, `petting:light_blue_collar`, `petting:yellow_collar`, `petting:lime_collar`, `petting:pink_collar`, `petting:gray_collar`, `petting:light_gray_collar`, `petting:cyan_collar`, `petting:purple_collar`, `petting:blue_collar`, `petting:brown_collar`, `petting:green_collar`, `petting:black_collar`
- `petting:golden_dragon_egg` (BlockItem, `Rarity.EPIC`)

### Blocks & Block Entities
- `petting:golden_dragon_egg` (`Block` & `BlockEntityType<GoldenDragonEggBlockEntity>`)

### Menus (`Registries.MENU`)
- `petting:pet_inventory` (`MenuType<PetInventoryMenu>`)

### Creative Mode Tabs (`Registries.CREATIVE_MODE_TAB`)
- `petting:petting` (Display name: `itemGroup.petting`, Icon: Golden Paw)

---

## Data Attachments & Components

### 1. Entity Attachment (`NeoForgeRegistries.ATTACHMENT_TYPES`)
- **`petting:pet_data`** (`PetData.java`): Attached to all entities to manage pet status.
  - Fields serialized via `MapCodec`:
    - `owner`: Optional UUID of owner.
    - `tamed`: boolean.
    - `order`: `PetOrder` (`FOLLOW`, `SIT`, `WANDER`).
    - `combat_mode`: `CombatMode` (`PASSIVE`, `DEFENSIVE`, `AGGRESSIVE`, `WANDER`, `GUARD`).
    - `collar`: Optional `ItemStack` of equipped collar.
    - `saddle`: Optional `ItemStack` of equipped saddle.
    - `ride_attack_on_click`: boolean.
    - `ride_auto_attack_hostiles`: boolean.

### 2. Data Components (`Registries.DATA_COMPONENT_TYPE`)
- **`petting:collar_settings`** (`CollarSettings.java`): Attached to collar item stacks.
  - Encodes: combat mode, follow distance, teleport distance, teleport enabled, prefer ranged, defend other pets, retaliate when hurt.
- **`petting:collar_respawn_point`** (`CollarRespawnPoint.java`): Attached to bound collar item stacks.
  - Encodes: `GlobalPos` (dimension resource location and block pos) and `isCushion` boolean.
- **`petting:dragon_egg_owner`** (`UUID`): Stores the UUID of the player who infused the egg.
- **`petting:dragon_egg_owner_name`** (`String`): Stores the username of the player who infused the egg.

### 3. World Saved Data (`PetSavedData.java`)
- Storage identifier: `petting:pets` in the Overworld `SavedData` repository.
- Maintains a bidirectional lookup between player UUIDs and lists of their active pet UUIDs for pet count limit enforcement (`maxPetCount`).

---

## Network Payloads (`PettingNetwork.java`)

All network packets use typed record payloads synchronized over network channel `"1"`:

### Client ➔ Server
1. **`OpenPetInventoryPayload`** (`petting:open_pet_inventory`):
   - Arguments: `int entityId`
   - Validates ownership and distance ($\le 16$ blocks) before opening `PetInventoryMenu`.
2. **`UpdateCollarSettingsPayload`** (`petting:update_collar_settings`):
   - Arguments: `int entityId`, `CollarSettings settings`
   - Validates ownership and distance ($\le 12$ blocks) before applying collar updates.
3. **`TogglePetSitPayload`** (`petting:toggle_pet_sit`):
   - Arguments: `int entityId`
   - Toggles sitting order between `SIT` and `FOLLOW`.
4. **`MountPetPayload`** (`petting:mount_pet`):
   - Arguments: `int entityId`
   - Validates rideability and mounts player onto pet.
5. **`UpdateRidingSettingsPayload`** (`petting:update_riding_settings`):
   - Arguments: `int entityId`, `boolean attackOnClick`, `boolean autoAttackHostiles`
   - Updates riding behavior toggles on `PetData`.
6. **`RiddenPetAttackPayload`** (`petting:ridden_pet_attack`):
   - Arguments: None
   - Commands the ridden mount to execute its signature attack in aim direction.

### Server ➔ Client
1. **`DismountPetPayload`** (`petting:dismount_pet`):
   - Arguments: None
   - Signals the client to terminate riding steering controllers.

---

## Mixins Overview (`petting.mixins.json`)

| Mixin Class | Target Class | Injected Behavior |
|---|---|---|
| `DragonHoverPhaseMixin` | `DragonHoverPhase` | Enhances flight speed (`1.25x`) and redirects movement towards combat targets for tamed dragons. |
| `EnderDragonFightMixin` | `EnderDragonFight` | Intercepts End crystal respawn ritual to detect Golden Dragon Eggs, tame the newly spawned dragon, and suppress portal explosion damage for 25s. |
| `EnderDragonMixin` | `EnderDragon` | Replaces dragon pathfinding with `getTamedDragonFlyTarget`, disables terrain destruction (`checkWalls = false`), and manages green eyes rendering. |
| `EntityMixin` | `Entity` | Forwards player interactions from multi-part sub-entities (such as `EnderDragonPart`) back to root pet logic. |
| `LivingEntityMixin` | `LivingEntity` | Intercepts movement input and jumping when riding custom mounts. |
| `MonsterMixin` | `Monster` | Allows players to sleep in beds when nearby monsters are friendly tamed pets. |
| `EnderDragonRendererMixin` *(Client)* | `EnderDragonRenderer` | Overrides eye texture layer to render radiant green dragon eyes (`dragon_eyes_green.png`) on tamed dragons. |

---

*Related Pages*: [Commands](Commands) · [Configuration](Configuration) · [Compatibility](Compatibility)
