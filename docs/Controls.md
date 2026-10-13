# Controls

**Petting** registers custom key bindings under the **Petting** controls category, along with context-sensitive controls while riding pets.

All keybindings can be customized in the Minecraft controls menu:  
`Options...` ➔ `Controls...` ➔ `Key Binds` ➔ scroll down to **Petting**.

---

## Default Key Bindings

| Key Binding | Default Key | Translation Key | Description |
|---|---|---|---|
| **Open Pet Inventory** | **`Shift + Right Mouse`** | `key.petting.open_pet_inventory` | Opens the pet equipment and tactical GUI for the targeted owned pet (or current mount). |
| **Sit / Stand Pet** | **`Right Mouse`** | `key.petting.toggle_pet_sit` | Commands the targeted owned pet to toggle between sitting and following. |
| **Ride / Dismount Pet** | **`R`** | `key.petting.ride_pet` | Mounts the targeted owned pet within range, or dismounts from your current pet mount. |

---

## Smart Right-Click Behavior

The **Sit / Stand Pet** action is bound to Right Click by default, but Petting incorporates intelligent context detection:

If you are holding an **item interaction** for that pet, the sit/stand command is suppressed so the item functions normally:
- Holding a **Collar** when the pet has no collar $\rightarrow$ Equips the collar.
- Holding a **Dye** matching a different color than the equipped collar $\rightarrow$ Recolors the collar.
- Holding a valid **Golden Treat** when the pet is injured $\rightarrow$ Heals the pet.
- Holding a **Golden Star** when targeting a dragon $\rightarrow$ Heals the dragon.
- Holding a **Saddle** when the pet has no saddle $\rightarrow$ Equips the saddle.
- Holding a **Name Tag**, **Lead**, or **Shears** $\rightarrow$ Performs the standard vanilla action.

---

## Riding Controls Cheatsheet

When actively riding a pet, the following controls apply:

### 1. Land Mounts
| Action | Key | Details |
|---|---|---|
| **Steer & Move** | **`W`, `A`, `S`, `D`** | Directional walking control. |
| **Jump** | **`Space`** | Standard jump (jump height `0.50`). |
| **Sprint** | **`Sprint` (Ctrl)** | Applies a `1.35x` speed multiplier. |
| **Dismount** | **`Sneak` (Shift) or `R`** | Immediately dismounts. |
| **Attack** | **`Left Click`** | Triggers ridden melee or projectile attack. |

### 2. Flying Mounts
| Action | Key | Details |
|---|---|---|
| **Steer & Move** | **`W`, `A`, `S`, `D`** | Horizontal aerial movement. |
| **Ascend** | **`Space` (Jump)** | Fly upward (speed controlled by `flyingPetAscentSpeed`). |
| **Descend / Land** | **`Sneak` (Shift)** | Fly downward (speed controlled by `flyingPetDescentSpeed`). |
| **Dismount** | **`Sneak` on ground or `R`** | Dismounts when touching the floor or pressing `R` in flight. |
| **Signature Attack** | **`Left Click`** | Fires Fireballs, Wither Skulls, Arrows, or bites. |

### 3. Swimming Mounts
| Action | Key | Details |
|---|---|---|
| **Steer & Swim** | **`W`, `A`, `S`, `D`** | Horizontal aquatic movement. |
| **Rise** | **`Space` (Jump)** | Ascends toward the water surface. |
| **Dive** | **`Sneak` (Shift)** | Submerges toward the sea floor. |
| **Dismount** | **`Sneak` on sea floor or `R`** | Dismounts when grounded on the ocean floor or pressing `R`. |
| **Attack** | **`Left Click`** | Triggers aquatic attack. |

---

*Related Pages*: [Riding](Riding) · [Pet Inventory](Pet-Inventory) · [Configuration](Configuration)
