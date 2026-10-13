# Commands

**Petting** registers an administrative command designed for server operators and map makers to spawn pre-tamed pets directly to a designated player.

---

## `/summon pet`

Spawns any valid mob into the world and immediately bonds it as a tamed pet to the specified player.

### Syntax
```text
/summon pet <owner> <entity> [<pos>] [<nbt>]
```

### Permission Level
- Requires **Gamemaster / Operator Level 2** (`Commands.LEVEL_GAMEMASTERS` / cheats enabled).

### Arguments

| Argument | Type | Required? | Description |
|---|---|:---:|---|
| `<owner>` | Player selector | **Yes** | The player who will become the permanent owner of the newly summoned pet (e.g. `@p`, `@s`, or a player name). |
| `<entity>` | Entity Type ID | **Yes** | The registry identifier of the entity to spawn (e.g. `minecraft:creeper`, `minecraft:warden`, `minecraft:iron_golem`). |
| `[<pos>]` | Coordinate vector (`x y z`) | *No* | The world coordinates where the pet should spawn. Defaults to the executor's current position if omitted. |
| `[<nbt>]` | NBT Compound `{...}` | *No* | Optional NBT data tags applied to the entity upon creation (e.g. custom names, equipment, or potion effects). |

---

## Behavior & Rules

1. **Mob Validation**:
   - The entity type must resolve to an instance of `net.minecraft.world.entity.Mob`.
   - If an invalid entity type is specified (such as a boat, minecart, arrow, or experience orb), the command fails with the error message:  
     > *"Target entity is not a mob and cannot be tamed as a pet."* (`commands.petting.summon.error.not_a_mob`)
2. **Immediate Taming**:
   - Calls `PetHelper.setTamed(mob, owner.getUUID())` upon spawning.
   - Bypasses health weakening requirements and failure chances.
   - Bypasses the server `maxPetCount` limit, allowing operators to gift pets beyond standard survival limits.
3. **Success Feedback**:
   - Sends a confirmation message to the command executor:  
     > *"Summoned `<Entity>` as a pet for `<Player>`"* (`commands.petting.summon.success`)

---

## Examples

- Spawn a tamed Wolf at your current position:
  ```text
  /summon pet @s minecraft:wolf
  ```
- Spawn a tamed Iron Golem for a player named `Alex` at specific coordinates:
  ```text
  /summon pet Alex minecraft:iron_golem ~ ~ ~
  ```
- Spawn a tamed Creeper wearing a custom name tag:
  ```text
  /summon pet @s minecraft:creeper ~ ~ ~ {CustomName:'{"text":"Sparky"}'}
  ```
- Spawn a tamed Warden with extra speed:
  ```text
  /summon pet @s minecraft:warden ~ ~ ~ {Attributes:[{Name:"minecraft:movement_speed",Base:0.4}]}
  ```

---

*Related Pages*: [Taming](Taming) · [Configuration](Configuration) · [Technical Reference](Technical-Reference)
