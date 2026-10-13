# Custom Treat Tags

**Petting** is fully data-driven. Which entities can be tamed by which [Golden Treat](Golden-Treats) is determined by Minecraft **entity type tags**.

You can customize, add modded mobs, or reassign vanilla mobs to any treat using a simple datapack.

---

## Tag IDs and File Locations

All nine treat tags reside in the `petting` namespace:

| Treat Item | Entity Type Tag ID | Datapack JSON Path |
|---|---|---|
| **Golden Wheat** | `petting:tames_with/golden_wheat` | `data/petting/tags/entity_type/tames_with/golden_wheat.json` |
| **Golden Star** | `petting:tames_with/golden_star` | `data/petting/tags/entity_type/tames_with/golden_star.json` |
| **Golden Flesh** | `petting:tames_with/golden_flesh` | `data/petting/tags/entity_type/tames_with/golden_flesh.json` |
| **Golden Bone** | `petting:tames_with/golden_bone` | `data/petting/tags/entity_type/tames_with/golden_bone.json` |
| **Golden Fish** | `petting:tames_with/golden_fish` | `data/petting/tags/entity_type/tames_with/golden_fish.json` |
| **Golden Kelp** | `petting:tames_with/golden_kelp` | `data/petting/tags/entity_type/tames_with/golden_kelp.json` |
| **Golden Fungus** | `petting:tames_with/golden_fungus` | `data/petting/tags/entity_type/tames_with/golden_fungus.json` |
| **Golden Eye** | `petting:tames_with/golden_eye` | `data/petting/tags/entity_type/tames_with/golden_eye.json` |
| **Golden Slime** | `petting:tames_with/golden_slime` | `data/petting/tags/entity_type/tames_with/golden_slime.json` |

---

## Creating a Datapack

To add or modify treat tags:

1. In your world folder, navigate to `datapacks/` and create a folder (e.g. `MyPettingTweaks`).
2. Create `pack.mcmeta` inside `MyPettingTweaks`:
   ```json
   {
     "pack": {
       "pack_format": 48,
       "description": "Custom Treat Tags for Petting"
     }
   }
   ```
3. Create the directory path:  
   `MyPettingTweaks/data/petting/tags/entity_type/tames_with/`
4. Add the JSON file for the treat you want to edit.

### Example 1: Adding Modded Mobs (Appending)
To make custom modded creatures tameable with **Golden Wheat** without removing existing vanilla animals, set `"replace": false`:

```json
{
  "replace": false,
  "values": [
    "alexsmobs:capuchin_monkey",
    "alexsmobs:elephant",
    "twilightforest:deer"
  ]
}
```

### Example 2: Reassigning an Entire Tag (Overwriting)
If you want **Golden Flesh** to *only* tame Phantoms and nothing else, set `"replace": true`:

```json
{
  "replace": true,
  "values": [
    "minecraft:phantom"
  ]
}
```

### Example 3: Referencing Other Tags
You can include entire tags inside your treat definition. Use `"required": false` so your datapack does not encounter errors if another mod is absent:

```json
{
  "replace": false,
  "values": [
    { "id": "#c:bosses", "required": false },
    { "id": "#mymod:custom_dragons", "required": false }
  ]
}
```

---

## Tag Matching Rules & Priority

When interacting with a mob using a treat:

1. **Explicit Tag Match**: If the entity type is included in `petting:tames_with/<treat>`, the treat is accepted.
2. **Exclusion Check**: If the entity type is present in *another* treat's `tames_with` tag, it will **reject** all other treats.
3. **Fallback Matching** (only active for mobs belonging to **none** of the nine treat tags):
   - Max health $> 300.0 \rightarrow$ Requires **Golden Star**.
   - Hostile mob $\rightarrow$ Requires **Golden Bone**.
   - Any other mob $\rightarrow$ Requires **Golden Wheat**.
4. **Server Configuration Overrides**:
   - If an entity ID is listed in `untameableEntities` (`petting-server.toml`), it can never be tamed, regardless of tags.
   - The wild **Ender Dragon** cannot be tamed directly by treat even if in the tag; it must be summoned via the [Golden Dragon Egg](Golden-Dragon-Egg) ritual.

---

## Recipe Viewer Live Synchronization

Because Petting dynamically reads the entity tags at runtime, any mobs added or modified via datapacks will **automatically update in JEI's "Pet Taming" recipe view**!

---

*Related Pages*: [Golden Treats](Golden-Treats) · [Taming](Taming) · [Compatibility](Compatibility)
