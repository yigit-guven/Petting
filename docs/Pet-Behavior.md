# Pet Behavior

Once tamed, a mob's artificial intelligence (AI) is augmented by **Petting** to behave as a loyal companion. The mod applies customized goal systems, suppresses unwanted native hostilities, and provides comprehensive friendly-fire protection.

---

## AI Goal Hierarchy

When an entity joins the world as a tamed pet, conflicting vanilla behaviors (such as wandering away or native monster hostility) are replaced with Petting's companion AI hierarchy:

### Action Goals
1. **Sitting**:
   - Active when the pet has received the `SIT` command.
   - Prevents pathfinding, locks position, and applies sitting stances.
2. **Ranged Attack**:
   - Active if the pet is holding a Bow or Crossbow and has the *Prefer Ranged* collar toggle enabled.
   - Attack radius: 16 blocks. Firing interval: 20 ticks (1 second). Keeps distance from targets.
3. **Melee Attack**:
   - Active for close-quarters combat against the pet's current attack target.
   - Automatically suppressed when ranged attack is active with a bow.
4. **Follow Owner**:
   - Active when the pet has the `FOLLOW` command.
   - Begins pathfinding when distance $\ge$ `followDistance`.
   - Stops pathfinding when within close range of the owner.
5. **Wander & Guard**:
   - Active in `WANDER` or `GUARD` modes, maintaining position around the designated anchor post.

### Target Selection Goals
1. **Defend Owner**:
   - Targets any entity that has recently attacked or damaged the owner within 16 blocks.
2. **Attack Owner's Target**:
   - Targets any entity that the owner has struck in combat.
3. **Guard Patrol**:
   - Active in `GUARD` mode. Scans for untamed hostile monsters within the collar's patrol radius.

---

## Native Hostility Suppression

In vanilla Minecraft, many mobs have hardcoded hostility routines (for example, Iron Golems attack zombies, Wolves hunt sheep, and Withers attack all living creatures).

When the server configuration `disableBuiltInHostility` is enabled (default `true`):
- All native hostility target routines are disabled upon taming.
- Pets will strictly obey their configured [Combat Mode](Combat-Modes).
- Withers have their secondary head random targeting suppressed, preventing them from firing at random terrain or innocent bystanders.

---

## Passive Mob Combat

By default, non-hostile creatures (such as cows, sheep, pigs, chickens, and villagers) do not engage in combat even if tamed:
- **`enablePassiveMobAttacks`** (default: `false`): Governs whether passive mobs are allowed to acquire combat targets or strike enemies. When `false`, passive pets act peacefully and will not fight.
- **`passiveMobAttackDamage`** (default: `2`): Sets the base melee attack damage dealt by passive mobs when passive mob attacks are enabled.

---

## Sitting & Standing

Pets can be commanded to sit or stand using the configured keybind (default `Right Click`):

- **Debounce Protection**: A cooldown prevents accidental double-triggering.
- **Riding Guard**: You cannot command a pet to sit while actively riding it.
- **Attack Forgiveness (`forgiveAttacksOnSit`)**: When commanded to sit, the pet immediately clears its current target, resets anger states, and ceases all hostilities.
- **Aerial & Aquatic Stances**:
  - **Flying Pets (`flyingPetsHoverWhenSitting`)**: When sitting in mid-air, flying pets hover stationary in place rather than falling to the ground.
  - **Aquatic Pets (`swimmingPetsStayStillWhenSitting`)**: Aquatic pets stay anchored in water rather than floating to the surface or sinking.
- **Visual Stances**: Custom sitting poses are rendered for humanoids, illagers, foxes, pandas, wolves, and felines.
- **Discrete Nametags**: When a pet is sitting, its nametag renders discreetly to minimize screen clutter.

---

## Teleportation

When following their owner, pets can teleport across obstacles or long distances:
- **Condition**: Teleportation must be enabled on the collar (or server config), and the distance between owner and pet must exceed the configured `teleportDistance` (slider range 8 to 64 blocks, default 20).
- **Placement**: Searches candidate positions within $\pm 3$ blocks horizontally and $\pm 1$ block vertically of the owner.
- **Safety Checks**: Teleportation only succeeds on a sturdy floor that is unobstructed for the pet's size.
- **Disable Teleport**: If you prefer pets to follow strictly on foot or horseback, toggle **Teleport: OFF** on their collar.

---

## Protection and Friendly Fire

Comprehensive safety features protect you and your companions from accidental combat accidents:

### 1. Owner Immunity
- Tamed pets, their summoned minions, and their projectiles **can never damage their owner**. Any incoming damage from a pet to its owner is completely cancelled.
- Pets can never target their owner under any circumstances.

### 2. Owner-to-Pet Friendly Fire (`ownerCanAttackPets`)
- Controlled by the server configuration `ownerCanAttackPets` (default `true`).
- When set to `false`, pets are 100% immune to damage dealt by their owner (including swords, arrows, and sweeps).

### 3. Pet-to-Pet Friendly Fire (`petsCanAttackPets`)
- Controlled by the server configuration `petsCanAttackPets` (default `false`).
- When `false`, pets belonging to the same owner cannot damage or target one another, preventing accidental chain-reaction brawls among your companions.

### 4. Vehicle & Passenger Protection
- Damage between a mounted pet and its rider is automatically cancelled.

---

## Quality of Life Features

- **Boss Bars (`hideTamedBossBars`)**: Boss health bars for tamed boss pets (such as the Wither and Ender Dragon) are hidden from players' screens to keep the interface clean. The bar is restored if the entity dies or reverts.
- **Peaceful Sleep (`tamedMonstersPreventSleep`)**: In vanilla, nearby monsters prevent players from sleeping. Petting disables this check for friendly tamed monsters, allowing you to sleep peacefully next to your pet Zombie, Skeleton, or Wither.

---

*Related Pages*: [Combat Modes](Combat-Modes) · [Collars](Collars) · [Riding](Riding) · [Configuration](Configuration)
