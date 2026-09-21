# Customizing Taming Treats (Datapacks & KubeJS)

Petting uses Minecraft’s native entity type tagging system (`TagKey<EntityType<?>>`). This allows server administrators, modpack creators, and singleplayer users to easily assign any mob (vanilla or modded) to any Golden Treat using **Datapacks** or **KubeJS** without editing core mod files.

---

## Treat Tag List

Each Golden Treat is linked to a specific entity tag under the `petting` namespace:

| Golden Treat | Entity Type Tag ID |
| :--- | :--- |
| **Golden Wheat** | `#petting:golden_wheat` |
| **Golden Star** | `#petting:golden_star` |
| **Golden Flesh** | `#petting:golden_flesh` |
| **Golden Bone** | `#petting:golden_bone` |
| **Golden Fish** | `#petting:golden_fish` |
| **Golden Kelp** | `#petting:golden_kelp` |
| **Golden Fungus** | `#petting:golden_fungus` |
| **Golden Eye** | `#petting:golden_eye` |
| **Golden Slime** | `#petting:golden_slime` |

---

## Method 1: Using a Data Pack

A data pack can add new mobs to an existing treat tag or completely rewrite a treat's acceptable mobs.

### Directory Structure
Inside your world or server directory, navigate to `datapacks/`:

```text
datapacks/
└── MyCustomPetting/
    ├── pack.mcmeta
    └── data/
        └── petting/
            └── tags/
                └── entity_type/
                    └── tames_with/
                        ├── golden_wheat.json
                        └── golden_star.json
```

### 1. Create `pack.mcmeta`
```json
{
  "pack": {
    "pack_format": 48,
    "description": "Custom Petting Mob Treat Mappings"
  }
}
```

### 2. Create or Edit the Tag JSON Files
Place the JSON file under:  
`data/petting/tags/entity_type/tames_with/<treat_name>.json`

#### Example A: Appending Modded Mobs
To make modded mobs accept **Golden Wheat** alongside existing vanilla mobs, keep `"replace": false`:

```json
{
  "replace": false,
  "values": [
    "alexsmobs:capybara",
    "alexsmobs:kangaroo",
    "autumnity:turkey"
  ]
}
```

#### Example B: Completely Overwriting Default Mobs
If you want **Golden Star** to *only* tame a specific set of bosses:

```json
{
  "replace": true,
  "values": [
    "minecraft:ender_dragon",
    "minecraft:wither",
    "cataclysm:ignis"
  ]
}
```

---

## Method 2: Using KubeJS

If you are running a modpack with [KubeJS](https://curseforge.com/minecraft/mc-mods/kubejs), you can modify Petting's treat tags via server scripts.

Place a script in `kubejs/server_scripts/petting_tags.js`:

```javascript
ServerEvents.tags('entity_type', event => {
    // Add custom modded mobs to Golden Wheat
    event.add('petting:golden_wheat', [
        'alexsmobs:capybara',
        'alexsmobs:kangaroo'
    ])

    // Add modded bosses to Golden Star
    event.add('petting:golden_star', [
        'cataclysm:ignis',
        'cataclysm:netherite_monstrosity',
        'twilightforest:naga'
    ])

    // Remove a mob from a treat tag if desired
    event.remove('petting:golden_flesh', 'minecraft:phantom')
})
```

---

## Automatic Fallback Rule

If a mob is **not** assigned to any of the 9 treat tags via Petting or your data pack, the following fallback logic automatically applies:

1. **Max Health > 300**: Tamed with **Golden Star**.
2. **Hostile Monsters (`Enemy`)**: Tamed with **Golden Bone**.
3. **Passive & Neutral Mobs**: Tamed with **Golden Wheat**.
