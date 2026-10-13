# Pet Behavior

Once tamed, a mob's artificial intelligence (AI) is augmented by **Petting** to behave as a loyal companion. The mod injects custom goal systems, suppresses unwanted native hostilities, and provides comprehensive friendly-fire protection.

---

## Injected AI Goals

When an entity joins the world, `PetGoalEvents` inspects whether it is a tamed pet. If so, vanilla conflicting goals (such as vanilla `FollowOwnerGoal`, `SitWhenOrderedToGoal`, or `RunAroundLikeCrazyGoal`) are removed, and Petting's custom goal hierarchy is injected:

### Action Goals (`goalSelector`)
1. **Priority 1: `PetSitGoal`**
   - Active when the pet has received the `SIT` order.
   - Prevents pathfinding, locks movement, and applies special sitting stances.
2. **Priority 2: `PetRangedAttackGoal`**
   - Active if the pet is holding a Bow or Crossbow and has the *Prefer Ranged* collar toggle enabled.
   - Movement speed: `1.25x`. Attack radius: `16 blocks`. Attack interval: `20 ticks` (1 second).
3. **Priority 3: `PetMeleeAttackGoal`**
   - Active for close-quarters combat against the pet's current attack target.
   - Movement speed: `1.2x`. Automatically suppressed if the ranged attack goal is active with a bow.
4. **Priority 4: `PetFollowOwnerGoal`**
   - Active when the pet has the `FOLLOW` order.
   - Movement speed: `1.25x`. Repaths towards the owner every 10 ticks.
   - Begins pathfinding when distance $\ge$ `followDistance`.
   - Stops pathfinding when within $\max(1.5, \text{followDistance} \times 0.5)$ blocks of the owner.
5. **Priority 5: `PetWanderGoal`**
   - Movement speed: `1.0x`. Active in `WANDER` or `GUARD` modes, or when idle.

### Target Goals (`targetSelector`)
1. **Priority 1: `PetDefendOwnerTargetGoal`**
   - Targets any entity that has recently attacked or damaged the owner within 16 blocks.
2. **Priority 2: `PetAttackOwnerTargetGoal`**
   - Targets any entity that the owner has recently attacked or struck.
3. **Priority 3: `PetGuardTargetGoal`**
   - Active in `GUARD` mode. Scans for untamed hostile monsters (`Enemy`) within the collar's patrol radius.

---

## Native Hostility Suppression

In vanilla Minecraft, many mobs have hardcoded hostility routines (for example, Iron Golems attack zombies, Wolves hunt sheep, and Withers attack all living creatures).

When the server configuration `disableBuiltInHostility` is enabled (default `true`):
- All native hostility target goals are removed from the pet upon taming.
- `LivingChangeTargetEvent` intercepts target assignments and blocks the pet from acquiring targets outside its configured [Combat Mode](Combat-Modes).
- Withers have their secondary head random target routines suppressed, preventing them from firing at random terrain or innocent bystanders.

---

## Sitting & Standing

Pets can be commanded to sit or stand using the configured keybind (default `Right Click`, handled by `TogglePetSitPayload`):

- **Debounce Protection**: A 350-millisecond cooldown prevents accidental double-triggering.
- **Riding Guard**: You cannot command a pet to sit while actively riding or mounting it.
- **Attack Forgiveness (`forgiveAttacksOnSit`)**: When commanded to sit, the pet immediately clears its current target, resets anger states, and ceases all hostilities.
- **Aerial & Aquatic Poses**:
  - **Flying Pets (`flyingPetsHoverWhenSitting`)**: When sitting in mid-air, flying pets activate stationary hovering (`NoGravity = true`) rather than falling to the ground.
  - **Aquatic Pets (`swimmingPetsStayStillWhenSitting`)**: Aquatic pets stay anchored in water rather than floating to the surface or sinking.
- **Visual Stances**: Custom sitting poses are rendered client-side for humanoids, illagers, foxes, pandas, wolves, and felines.
- **Discrete Nametags**: When a pet is sitting, its nametag is rendered discreetly to avoid screen clutter.

---

## Teleportation

When following their owner, pets can teleport across obstacles or long distances:
- **Condition**: Teleportation must be enabled on the collar (or server config), and the distance between owner and pet must exceed the configured `teleportDistance` (slider range 8 to 64 blocks, default 20).
- **Placement**: Searches up to 10 candidate positions within $\pm 3$ blocks horizontally and $\pm 1$ block vertically of the owner.
- **Safety Checks**: Teleportation will only succeed on a sturdy, solid floor that has no collision obstruction for the pet's bounding box.
- **Disable Teleport**: If you prefer pets to follow strictly on foot or horseback, toggle **Teleport: OFF** on their collar.

---

## Protection and Friendly Fire

`PetSafetyEvents` provides hard guarantees against accidental injury between players and their companions:

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
- **Peaceful Sleep (`tamedMonstersPreventSleep`)**: In vanilla, nearby monsters prevent players from sleeping. `MonsterMixin` disables this check for friendly tamed monsters, allowing you to sleep peacefully next to your pet Zombie, Skeleton, or Wither.

---

*Related Pages*: [Combat Modes](Combat-Modes) · [Collars](Collars) · [Riding](Riding) · [Configuration](Configuration)
