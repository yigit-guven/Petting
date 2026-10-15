# Collar Respawn

**Petting** features a respawn mechanic that ensures your companions are never permanently lost to accidental deaths, hazards, or combat.

By binding a [Collar](Collars) to a bed or cushion, any pet wearing that collar will instantly resurrect at that location upon death.

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/collar(red).png" width="48" height="48" alt="Red Collar">
</p>

---

## How to Bind a Collar

To set a pet's respawn anchor:

1. Craft or obtain any collar.
2. Hold the collar in your hand and **Right-Click** on any valid anchor:
   - **Beds**: Any vanilla or modded bed block (`#minecraft:beds` tag).
   - **Cushions**: Any cushion block or cushion entity (`#c:cushions` tag).
3. Confirmation:
   - Green enchant particles burst from the anchor.
   - An enchanting chime sound plays.
   - You receive a chat notification: `Collar bound to bed at [X, Y, Z]` (or `cushion`).
   - The collar item gains an enchanted foil glint and a tooltip line: `Bound to bed` (or `Bound to cushion`).
4. Equip the bound collar onto your pet via `Shift + Right-Click` or direct right-click.

---

## The Respawn Sequence

When an equipped pet takes fatal damage in the world, the game intercepts the death event:

```text
[Pet takes lethal damage]
         │
         ▼
[Is Collar Respawn Point Valid?]
  ├─ YES ──► 1. Cancel death event completely
  │          2. Teleport pet to bound bed / cushion (cross-dimension supported)
  │          3. Restore health to 100% full HP
  │          4. Clear fire and all active potion effects
  │          5. Grant 60 ticks (3 seconds) of invulnerability
  │          6. Set pet order to SIT (keeps pet safe at home)
  │          7. Play Totem of Undying resurrection particles and sound
  │          8. Keep collar and all equipped items safe on pet
  │          9. Send chat alert: "Your pet <Name> died and respawned at its bed!"
  │
  └─ NO ───► 1. Pet dies permanently
             2. Collar, saddle, and equipment drop at death site
             3. Pet is deregistered from owner's active pet list
             4. Send chat alert: "Your pet <Name> died and could not respawn because its bed was missing!"
```

---

## Gameplay Notes

- **Cross-Dimension Support**: If you bind a collar in the Overworld and your pet dies in the Nether or the End, it will safely resurrect back at your Overworld bed.
- **Spawn Offsets**:
  - For beds: Teleports the pet to `X + 0.5, Y + 0.6, Z + 0.5`.
  - For cushions: Teleports the pet to `X + 0.5, Y + 0.25, Z + 0.5`.
- **Anchor Integrity**: If the bound bed or cushion is broken or mined away while the pet is adventuring, the pet cannot respawn. Re-bind the collar whenever you move your bed.
- **Server Toggle**: Server admins can disable this feature entirely by setting `enableCollarRespawn = false` in `petting-synced.toml`.

---

*Related Pages*: [Collars](Collars) · [Pet Behavior](Pet-Behavior) · [Configuration](Configuration)
