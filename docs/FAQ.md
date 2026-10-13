# Frequently Asked Questions (FAQ)

Here are direct answers to common questions about **Petting**, derived directly from the mod's gameplay mechanics and configuration defaults.

---

### Why won't a mob tame when I right-click it?
Verify the following checks:
1. **Wrong Treat**: Make sure you are using the correct treat for that mob species. Check [Golden Treats](Golden-Treats) or look up the mob in JEI's "Pet Taming" tab.
2. **Mob is Too Healthy**: By default, hostile mobs must be weakened below **30% of their maximum health** before they can be tamed. Weaken the mob first.
3. **Taming Failed Roll**: By default, there is a **33.3% chance** that any taming attempt fails and consumes the treat. If hearts do not appear, try feeding it another treat.
4. **Pet Limit Reached**: If the server has configured `maxPetCount`, you cannot tame additional pets once your cap is reached.
5. **Untameable Entity**: The mob may be listed in the server's `untameableEntities` configuration.
6. **Ender Dragon**: The wild Ender Dragon cannot be tamed directly with treats. You must use the [Golden Dragon Egg](Golden-Dragon-Egg) ritual.

---

### How do I heal my pet?
Right-click your owned pet while holding a valid [Golden Treat](Golden-Treats) in your main hand:
- Restores **50% of the pet's maximum health** per treat.
- For a **Golden Ender Dragon**, right-click it with a <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="16" height="16" alt="Golden Star"> **Golden Star** to restore **25% of its maximum health** (50 HP).

---

### How do I open the pet's inventory?
Look at your owned pet and press **`Shift + Right-Click`** (or the custom key bound to *Open Pet Inventory*). You can also open the inventory while riding the pet.

---

### Why can't I steer my pet while riding it?
To gain full steering control, your pet must have an equipped **Saddle**:
1. Open the pet inventory (`Shift + Right-Click`).
2. Place a saddle into the **Saddle Slot** on the right side of the screen.
3. Mount the pet (`R`). You now have full directional control (WASD, Jump, Sneak).

Without an equipped saddle, you can mount the pet as a passive passenger (if `allowRidingWithoutSaddle` is enabled), but the pet moves using its own AI.

---

### How do I make my pet respawn when it dies?
1. Hold any collar in your hand.
2. **Right-Click a Bed or Cushion**. You will see green enchant particles and receive a confirmation message.
3. Equip the bound collar onto your pet.
4. If your pet takes fatal damage, the death is cancelled, and it is instantly teleported back to the bed with 100% health, cleared effects, and 3 seconds of invulnerability. (See [Collar Respawn](Collar-Respawn)).

---

### Can my pets hurt me or each other?
- **Owner Immunity**: Tamed pets, their summoned minions, and their projectiles can **never harm their owner**.
- **Pet-to-Pet Friendly Fire**: By default (`petsCanAttackPets = false`), pets belonging to the same owner cannot damage or target one another.

---

### Why isn't my pet attacking hostile monsters?
1. **Check Combat Mode**: Open the pet inventory and check the Collar Tab. If the pet is set to **`PASSIVE`**, it will never attack.
2. **Check Sitting State**: Sitting pets do not engage in combat. Command it to stand (`Right-Click` or sit toggle).
3. **Defensive vs. Aggressive**: In `DEFENSIVE` mode, the pet only attacks mobs that strike its owner. If you want it to hunt down monsters proactively, switch it to **`AGGRESSIVE`** or **`GUARD`** mode.

---

### Can I change my collar's color after crafting it?
Yes! You have two ways:
1. **In-World**: Hold any dye in your hand and right-click your pet while it is wearing a collar of a different color. 1 dye is consumed, and the collar changes color while **preserving all settings and respawn bindings**.
2. **Crafting Table**: Combine any collar shapelessly with any dye in a crafting grid.

---

### Where are the configuration files?
- **Server Settings** (`petting-server.toml`): Saved inside your world folder at `.minecraft/saves/<WorldName>/serverconfig/petting-server.toml`.
- **Client Settings** (`petting-client.toml`): Saved globally for your client at `.minecraft/config/petting-client.toml`.
- You can also edit both through the in-game **Mods** configuration menu.

---

### How do I add modded mobs to treat tags?
You can create a datapack containing `data/petting/tags/entity_type/tames_with/<treat>.json`. Any mob added to these tags can be tamed with that treat and will automatically show up in JEI's "Pet Taming" recipe view. (See [Custom Treat Tags](Custom-Treat-Tags)).

---

*Related Pages*: [Home](Home) · [Getting Started](Getting-Started) · [Taming](Taming) · [Configuration](Configuration)
