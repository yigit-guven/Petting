# Combat Modes

Every tamed pet in **Petting** operates under a configurable **Combat Mode**. The combat mode dictates when and how the pet engages hostile entities or defends its owner.

Combat modes are configured on the pet's equipped [Collar](Collars) via the pet inventory screen (`Shift + Right-Click`). If a pet has no collar equipped, it uses the server default combat mode (**Aggressive** by default).

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/gui/container/pet_inventory.png" width="300" alt="Pet Inventory GUI">
</p>

---

## The Five Modes

In the Collar tab of the pet inventory, clicking the mode button cycles through all five modes in order:
`PASSIVE` → `DEFENSIVE` → `AGGRESSIVE` → `WANDER` → `GUARD` → `PASSIVE`.

### 1. Passive Mode
> *"Pet never initiates combat or attacks."*

- The pet will never attack any entity under any circumstances.
- **Overrides**: Retaliation and Defend Other Pets toggles are completely inactive while in Passive mode.
- Ideal for utility pets, mounts, or delicate companions you want kept safe from harm.

### 2. Defensive Mode
> *"Defends you when attacked. Never initiates combat."*

- The pet will attack whatever mob deals damage to its owner.
- The pet will **never** initiate combat against passive or unprovoked mobs.
- If **Retaliate** is enabled, it will also fight back if it takes damage itself.
- If **Defend Other Pets** is enabled, it will protect fellow pets belonging to the same owner within 16 blocks when they are attacked.

### 3. Aggressive Mode
> *"Attacks what you strike and defends you when attacked."*

- The pet attacks whatever mob you strike (your current target).
- The pet defends you whenever you are attacked by a mob.
- The pet will proactively seek out and attack untamed hostile monsters in the area.
- If **Retaliate** is enabled, it fights back when damaged.
- If **Defend Other Pets** is enabled, it protects fellow pets when they are attacked.

### 4. Wander Mode
> *"Stays in this area and wanders freely within wander radius without following you away."*

- When switched to Wander mode, the pet anchors its home post at its current block position.
- It will wander freely within the configured **Wander Radius** (controlled by the slider in the Collar tab, from 2 to 32 blocks by default).
- It does **not** follow the owner away from this post.
- If knocked or lured farther than 64 blocks away from its post, it snaps back towards its post.
- Retaliation and Defend Other Pets still apply if enabled.

### 5. Guard Mode
> *"Patrols this area and actively hunts untamed hostile monsters within patrol radius."*

- When switched to Guard mode, the pet anchors its patrol post at its current block position.
- It stays within its configured **Patrol Radius** (slider from 2 to 32 blocks).
- It actively detects and hunts down untamed hostile monsters that enter its patrol radius.
- Includes a leash buffer: if pursuing a hostile mob, it will abandon the chase and return home if the target moves more than 10 blocks beyond its patrol radius.

---

## Tactical Sub-Toggles

In addition to the primary mode, each collar provides four tactical sub-toggles:

| Toggle | Description | Behavior |
|---|---|---|
| **Retaliate** | Fight back when damaged | When damaged by another entity, immediately sets that attacker as the pet's target. (Inactive in Passive mode). |
| **Defend** | Defend other pets | Protects any fellow pet owned by the same player within 16 blocks when attacked. (Inactive in Passive mode). |
| **Ranged** | Prefer ranged attacks | When ON, if the pet is holding a Bow or Crossbow, it maintains distance (16 blocks) and shoots arrows rather than charging into melee. |
| **Teleport** | Follow teleportation | When ON, the pet teleports to its owner if the distance exceeds the configured Teleport Distance slider. When OFF, the pet only follows on foot or wings. |

---

## Target Safety Hierarchy

Whenever a pet acquires an attack target, automatic safety checks protect you and your allies:

1. **State check**: Dead, sitting, or passive combat mode pets will not attack.
2. **Passive mob check**: Non-hostile pets (cows, sheep, pigs, etc.) will not attack unless `enablePassiveMobAttacks` is set to `true` on the server.
3. **Owner immunity**: Pets can **never** target their owner.
4. **Friendly fire check**: Pets belonging to the same owner will not target each other (unless `petsCanAttackPets` is enabled on the server).
5. **Allied passenger check**: Pets will not target entities riding them or the vehicle they are mounted on.
6. **Leash check**: In Guard and Wander modes, pets will not pursue enemies beyond their patrol perimeter.

---

*Related Pages*: [Collars](Collars) · [Pet Behavior](Pet-Behavior) · [Pet Inventory](Pet-Inventory) · [Configuration](Configuration)
