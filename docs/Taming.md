# Taming

This page describes the mechanics and rules governing mob taming in **Petting**.

---

## Requirements at a Glance

1. Hold the matching **golden treat** in your **main hand** and right-click the mob.
2. The treat must match that mob species (see [which treat for which mob](#which-treat-tames-which-mob)).
3. The mob must not be listed in the `untameableEntities` server configuration.
4. You must be under your maximum pet limit (`maxPetCount`, unlimited by default).
5. If the mob is hostile, its health must be weakened to or below the configured threshold (30% by default).
6. The taming attempt must succeed the random roll (33.3% default failure rate).

---

## Step-by-Step Flow

When interacting with an untamed mob:

| Step | Check | Outcome when blocked |
|---|---|---|
| **1** | Is the held item a valid treat for this mob? | Normal item interaction continues. |
| **2** | Is the mob already bonded to another player? | Chat warning is shown; taming blocked. |
| **3** | Has the player reached their pet limit? | Angry villager particles play, "no" sound; treat is **not** consumed. |
| **4** | Does the mob need weakening and is it above the health threshold? | Smoke particles, "no" sound, "too healthy" warning; treat is **not** consumed. |
| **5** | Consume one treat (unless in Creative), then roll chance. | If failed: smoke particles, eating sound, failure warning; treat **is** consumed. |
| **6** | Success! | Heart particles burst, level-up chime plays, and the mob bonds to you as a pet. |

> [!NOTE]
> Vanilla-tamed pets (like cats, wolves, or horses) owned by you are recognized as your pets, allowing you to access their pet inventory, collar tactics, and sit toggles. Mobs belonging to other players cannot be tamed.

---

## Weakening Hostile Mobs

By default, hostile mobs must be weakened before they will accept a treat:
- **Condition**: Controlled by `hostileMobsOnly` (default `true`). When set to `false`, even passive animals must be weakened.
- **Threshold**: Controlled by `maxHealthPercentage` (default `30.0%`).
- **Formula**: `Current Health <= Max Health * (maxHealthPercentage / 100.0)`.

---

## Taming Failure Chance

Taming is not always instantaneous:
- Controlled by `unsuccessfulTamingChance` (default `33.3%`).
- When a roll fails, 1 treat is consumed and smoke particles appear. Simply feed the mob another treat until hearts appear!

---

## What Happens Upon Successful Taming

When a mob becomes your pet:
- It is bonded to your player UUID, set to the **Follow** order, and assigned your default combat mode (**Aggressive** by default).
- It is marked as persistent (it will never naturally despawn).
- Any existing attack targets and hostilities are cleared immediately.
- The pet inventory system and tactical AI goals are initialized.
- Boss health bars are hidden from the screen (if `hideTamedBossBars` is enabled).
- The pet is registered toward your active pet count limit.

---

## Which Treat Tames Which Mob?

1. **Ender Dragon**: An untamed wild Ender Dragon can **never** be tamed directly with a treat. See [Golden Dragon Egg](Golden-Dragon-Egg) for the summoning ritual.
2. **Untameable Entities**: Mobs listed in `untameableEntities` cannot be tamed.
3. **Tag Matching**: If a mob is listed in the treat's entity type tag (`petting:tames_with/<treat>`), that treat is valid.
4. **Exclusion Check**: If a mob is assigned to a specific treat tag, it will not accept any other treat.
5. **Universal Fallback** (for untagged or modded mobs):

| Condition | Required Treat |
|---|---|
| Maximum health is greater than 300 HP | **Golden Star** |
| Hostile mob | **Golden Bone** |
| All other mobs | **Golden Wheat** |

See [Golden Treats](Golden-Treats) for the full treat catalog, or [Custom Treat Tags](Custom-Treat-Tags) to customize them via datapacks.

---

## Healing Pets

Right-clicking your own injured pet with a matching treat restores its health:

| Pet State | Result |
|---|---|
| **Injured** | Consumes 1 treat, restores **50% of maximum health**, plays eating sound and heart particles. |
| **Full Health** | Treat is not consumed; shows message that pet is already healthy. |

For a **Golden Ender Dragon**, feeding it a **Golden Star** restores **25% of maximum health** (50 HP).

---

## Pet Limits

- Configured via `maxPetCount` in server settings (`-1` by default for unlimited).
- Tracks all active pets per player across the world.
- The administrative `/summon pet` command bypasses this limit.

---

## Automatic Recognition of Minions & Summons

When player-owned summons, allied minions, or familiars from other mods spawn into the world, Petting automatically detects their owner and integrates them as friendly pets. This prevents accidental friendly fire and allows you to manage them using Petting's systems.

---

## Releasing or Losing a Pet

When a pet is untamed or released:
- Ownership data is cleared, and any equipped collar and saddle drop to the ground.
- If a pet dies, its equipped items drop at the death site unless an active [Collar Respawn](Collar-Respawn) anchor brings it back to life.

---

*Related Pages*: [Golden Treats](Golden-Treats) · [Collars](Collars) · [Combat Modes](Combat-Modes) · [Configuration](Configuration)
