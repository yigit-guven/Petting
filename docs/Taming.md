# Taming

This page describes every rule that applies when you try to tame a mob. All behavior comes from `PetTamingEvents.java`, `PetEggHelper.java`, `PetHelper.java` and `PettingServerConfig.java`.

## Requirements at a glance

1. You right-click a **mob** with a **golden treat** in your **main hand**.
2. The treat must be valid for that mob ([which treat for which mob](#which-treat-tames-which-mob)).
3. The mob must not be listed in the `untameableEntities` config option.
4. You must be below your pet limit (unlimited by default).
5. If the mob requires weakening, its health must be at or below the configured percentage (30 % by default).
6. The attempt must not fail the random roll (33.3 % failure chance by default).

## Step-by-step flow

The checks run in this order for an **untamed** mob:

| # | Check | Outcome when the check blocks taming |
|---|---|---|
| 1 | Is the held item a treat valid for this mob? | Nothing happens; normal interaction continues. |
| 2 | Is the mob already bonded to someone by vanilla taming? | See the note below the table. |
| 3 | Has the player reached the pet limit? | Angry-villager particles, "no" sound, pet-limit message. Treat is **not** consumed. |
| 4 | Does the mob need weakening and is it above the health threshold? | Smoke particles, "no" sound, "too healthy" message. Treat is **not** consumed. |
| 5 | Consume one treat (unless Creative), then roll for failure. | Failed roll: smoke particles, eating sound, failure message. Treat **is** consumed. |
| 6 | Success | Heart particles, level-up sound, mob becomes your pet. |

> [!NOTE]
> Petting's own tamed check (`PetHelper.isTamed`) treats vanilla-tamed mobs as tamed too. A vanilla-tamed mob owned by you is handled as one of your pets (inventory, collar, sit toggle); one owned by another player answers with the "already belongs to someone else" message.

### Weakening rule

A mob needs weakening when **either** is true:

- the config option `hostileMobsOnly` is `false`, or
- the mob implements Minecraft's `Enemy` interface (hostile).

Weakening means `current health <= max health x maxHealthPercentage / 100`. Defaults: `hostileMobsOnly = true`, `maxHealthPercentage = 30.0`.

### Failure chance

`unsuccessfulTamingChance` (default `33.3`, range 0 to 100) is the percent chance that a taming attempt fails. A random number in `[0, 1)` is compared with `chance / 100`; below it means failure.

### What a successful taming does

`PetHelper.setTamed` performs these actions:

- Stores the owner UUID, marks the mob tamed, sets its order to *Follow* and its combat mode to the configured default (`AGGRESSIVE` by default).
- Marks the mob as persistent (it no longer despawns).
- Clears its current attack target and anger, and stops running target goals.
- Synchronizes vanilla tamed state when applicable (horses, `TamableAnimal`) and calls common modded setter methods through reflection.
- Injects Petting's AI goals ([Pet Behavior](Pet-Behavior#injected-ai-goals)).
- Hides the boss bar for tamed boss mobs when `hideTamedBossBars` is on.
- Records the pet in the owner's pet list (used for the pet limit).

## Which treat tames which mob?

`PetEggHelper.canTame` decides this:

1. An **untamed Ender Dragon can never be tamed** with a treat. See [Golden Dragon Egg](Golden-Dragon-Egg).
2. A mob in the `untameableEntities` config list can never be tamed.
3. If the mob's entity type is in the entity-type tag of the held treat (`petting:tames_with/<treat>`), taming is allowed.
4. If the mob's type is in the tag of **any other** treat, the held treat is rejected. The mob is bound to its tagged treats only.
5. Otherwise (the mob is in no treat tag), the **fallback** applies:

| Condition (checked in this order) | Required treat |
|---|---|
| Max health is greater than 300 | Golden Star |
| Mob implements `Enemy` (hostile) | Golden Bone |
| Anything else | Golden Wheat |

The complete tag contents are on [Golden Treats](Golden-Treats). Datapacks can change them: [Custom Treat Tags](Custom-Treat-Tags).

## Healing a pet

Right-clicking your own pet with a treat that is valid for it:

| Pet health | Result |
|---|---|
| Below maximum | Treat consumed (unless Creative), pet heals **50 % of its max health**, heart particles, eating sound. |
| At maximum | Nothing consumed; "already at full health" message. |

A tamed Ender Dragon heals **25 % of its max health** per Golden Star. The Ender Dragon is only listed in the Golden Star tag, so other treats do not apply to it.

## Pet limit

`maxPetCount` is the maximum number of pets one player may own. `-1` (default) means unlimited. Ownership is tracked in a per-world saved data entry (`petting:pets`) keyed by player UUID. The `/summon pet` command does **not** check this limit ([Commands](Commands)).

## Entities that become pets automatically

When any mob joins a level and is not yet tamed, Petting resolves its *root owner*. If that is a player, the mob is tamed to that player automatically. The root owner is resolved through:

1. Petting's own pet data,
2. vanilla `OwnableEntity` owners,
3. vanilla `TraceableEntity` owners (projectiles, area effect clouds, and so on),
4. no-argument getter methods named `getOwner`, `getSummoner`, `getCaster`, `getMaster`, `getLeader`, `getOwnerEntity` or `getCreator` (found by reflection, for modded minions).

This is also what makes player-owned minions immune to friendly fire, see [Pet Behavior](Pet-Behavior#protection-and-friendly-fire).

## Releasing / losing a pet

`PetHelper.setUntamed` clears ownership, drops the equipped collar and saddle on the ground and removes the pet from the owner's list. When a tamed mob dies, its collar and saddle drop too, unless a [Collar Respawn](Collar-Respawn) revives it first.
