# Riding

**Petting** includes a comprehensive mount and riding system. Nearly any tamed pet can be ridden across land, through water, or through the skies with specialized steering physics and combat abilities.

---

## Mounting & Dismounting

- **Mount Key**: Look at your owned pet and press **`R`** (default keybind).
- **Mount Range**: Within 3 blocks (or up to 6 blocks; up to 18 blocks for an Ender Dragon).
- **Dismount**:
  - Press **`R`** at any time.
  - Or press **`Sneak` (Shift)** while grounded (or on the sea floor).

---

## Saddle & Steering Control

Whether you can steer your mount depends on whether it has an equipped [Saddle](Pet-Inventory):

| State | Control | Behavior |
|---|---|---|
| **Saddled (Controlled)** | Full Steering | The pet's native AI is suppressed, its rotation locks to your camera aim, and it obeys WASD, Jump, and Sneak inputs. |
| **Unsaddled (Uncontrolled)** | Passive Passenger | If `allowRidingWithoutSaddle` is enabled (default `true`), you sit on the mob while it wanders using its own AI. You cannot steer it. |

> [!NOTE]
> If `allowRidingWithoutSaddle` is set to `false` in `petting-synced.toml`, attempting to mount an unsaddled pet is blocked with the message: *"This pet requires an equipped saddle to be ridden!"*

---

## Movement Modes

Petting dynamically assigns movement physics based on the mob's species and anatomy:

### 1. Land Walking
- **Steer**: `WASD`
- **Jump**: `Space` (jump height 0.50).
- **Step Assist**: The pet's step height is increased to `1.06` blocks, allowing smooth navigation up 1-block slopes without manual jumping.
- **Sprint**: Double-tap `W` or press `Sprint` for a `1.35x` movement speed boost.

### 2. Aerial Flight
Automatically detected for flying mobs (Blazes, Ghasts, Withers, Phantoms, Vexes, Bats, Allays, Bees, Parrots, the Golden Ender Dragon, or mobs with flying abilities):
- **Steer**: `WASD` (horizontal speed based on mob speed $\times$ `flyingPetSpeedMultiplier`).
- **Ascend**: Hold **`Jump` (Space)** (vertical speed controlled by `flyingPetAscentSpeed`, default `0.35`).
- **Descend / Land**: Hold **`Sneak` (Shift)** (vertical speed controlled by `flyingPetDescentSpeed`, default `0.35`).
- **Dismount**: Sneaking while touching the ground, or pressing `R` in mid-air.

### 3. Aquatic Swimming
Automatically detected for water creatures (Dolphins, Guardians, Squids, Turtles, Axolotls, Fish, etc.) or any pet submerged in water:
- **Steer**: `WASD` (speed controlled by `swimmingPetSpeedMultiplier`, default `1.0`).
- **Ascend**: Hold **`Jump` (Space)** to rise.
- **Dive**: Hold **`Sneak` (Shift)** to submerge.
- **Water Breathing**: As long as you are mounted on an aquatic pet underwater, the rider is granted continuous **Water Breathing**!
- **On Land**: Aquatic creatures crawl slowly on dry land.

---

## Ridden Combat

While mounted on a saddled pet, you can engage enemies directly from the saddle:

### 1. Attack on Click (Left Click)
Pressing your normal **Attack** key (Left Click) while riding commands your mount to execute a signature attack in your crosshair direction. Attacks have a short cooldown (`riddenAttackCooldownTicks`, default 10 ticks = 0.5s):

| Pet Type | Signature Ridden Attack |
|---|---|
| **Golden Ender Dragon** | Launches a **Dragon Fireball** |
| **Ghast** | Launches a **Large Fireball** (explosive) |
| **Blaze** | Launches a **Small Fireball** |
| **Wither** | Launches a **Wither Skull** projectile |
| **Skeleton / Bow-wielder** | Fires an **Arrow** in aim direction |
| **Other Mobs** | Performs an instantaneous **Melee Strike** against the target in front |

Friendly-fire protection guarantees that ridden attacks never damage you or your other companions.

### 2. Auto-Attack Hostiles
Configurable in the [Pet Inventory](Pet-Inventory) Riding Tab:
- When toggled **ON**, every 20 ticks (1 second) your mount scans for untamed hostile monsters within 12 blocks with line of sight.
- It automatically shoots projectiles at range or executes melee attacks within 3.5 blocks while you steer.

---

## Server Whitelist & Blacklist

Server administrators can restrict which entity types can be ridden in `petting-synced.toml` under `[riding]`:

- `useWhitelist = false` (default): All entities are ridable except those listed in `blacklist`.
- `useWhitelist = true`: Only entity types explicitly specified in `whitelist` can be ridden.
- `enablePetRiding = false`: Completely disables pet riding across the server.

---

*Related Pages*: [Controls](Controls) · [Pet Inventory](Pet-Inventory) · [Golden Dragon Egg](Golden-Dragon-Egg) · [Configuration](Configuration)
