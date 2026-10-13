# Golden Dragon Egg & Golden Ender Dragon

In **Petting**, you cannot tame an untamed Ender Dragon directly with treats. Instead, obtaining a loyal dragon companion requires a magical infusion ritual involving the **Golden Dragon Egg** and the End Exit Portal.

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="48" height="48" alt="Golden Star">
  &nbsp;&nbsp;&nbsp;&nbsp;➔&nbsp;&nbsp;&nbsp;&nbsp;
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/block/golden_dragon_egg.png" width="48" height="48" alt="Golden Dragon Egg">
</p>

---

## Direct Taming Restriction

If you attempt to right-click an untamed Ender Dragon with any treat, taming will be refused with the following in-game message:

> *"The Ender Dragon cannot be tamed directly! Infuse its egg with a Golden Star and summon it at the End Exit Portal."*

---

## Step 1: Crafting the Golden Dragon Egg

1. Slay the vanilla Ender Dragon to obtain the **Dragon Egg** block (`minecraft:dragon_egg`).
2. Place the Dragon Egg on the ground.
3. Craft a <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="20" height="20" alt="Golden Star"> [Golden Star](Golden-Treats#golden-star) (8 Gold Blocks surrounding 1 Nether Star).
4. **Right-Click** the placed Dragon Egg while holding the Golden Star.

### Infusion Effects
- Consumes 1 Golden Star (unless in Creative mode).
- The egg transforms into a **Golden Dragon Egg** (`petting:golden_dragon_egg`).
- Spawns Totem of Undying and End Rod particles with a chime and End Portal ambient sound.
- Records your player UUID and username permanently onto the egg.
- Displays chat confirmation: *"The Dragon Egg radiates golden energy and transforms into a Golden Dragon Egg!"*

### Block Properties
- **Light Level**: Emits light level 6.
- **Gravity**: Subject to gravity (falls like dragon eggs and sand).
- **Teleportation**: If struck or clicked normally, it teleports to a safe surface nearby while **preserving its owner data**.
- **Inspect Owner**: **`Shift + Right-Click`** on the placed egg will NOT teleport it. Instead, it plays a chime and displays who infused it:
  > *"This Golden Dragon Egg is infused by `<Player>`. Summon it at the End Exit Portal using 4 End Crystals."*
- **Item Form**: Mined Golden Dragon Eggs retain their owner tag in your inventory and display the tooltip: `Infused by: <Player>`. Rarity: **Epic**.

---

## Step 2: The Hatching Ritual

To hatch and bond with your dragon:

1. Travel to the central island in **The End** dimension.
2. Place the **Golden Dragon Egg** in the immediate vicinity of the bedrock **End Exit Portal** (within 5 blocks horizontally of center, just above the portal frame).
3. Place **4 End Crystals** on the four flat edges of the bedrock portal frame (the standard vanilla dragon respawn setup).
4. The crystal beams activate, converging on the sky above the portal.

### The Summoning Event
When the ritual finishes:
- The Golden Dragon Egg block is consumed by the ritual.
- A tamed **Golden Ender Dragon** is summoned above the portal.
- The dragon is bonded immediately to the player who infused the egg (or the nearest player within 64 blocks if unbound).
- The vanilla boss health bar is removed from all players' screens.
- **Explosion Safety**: All explosion damage and block/entity damage in the End portal region is suppressed for 25 seconds during and after the ritual to protect the summoner and newly hatched pet.
- Chat announcements are sent:
  - To owner: *"Your Golden Ender Dragon has hatched and bonded with you!"*
  - To all players: *"`<Player>` has summoned a tamed Golden Ender Dragon!"*

---

## Features of the Tamed Golden Ender Dragon

<p align="center">
  <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/entity/enderdragon/dragon_eyes_green.png" width="96" alt="Green Dragon Eyes">
</p>

### 1. Radiant Green Eyes
The tamed dragon features glowing green dragon eyes rather than the hostile purple eyes of wild dragons.

### 2. Zero Terrain Griefing
Unlike the wild Ender Dragon which smashes through stone, dirt, and obsidian, a tamed dragon **never breaks or destroys blocks** when flying or hovering!

### 3. Flight & Combat AI
- **Hovering Flight**: Orbits gracefully above its owner ($\sim +3$ blocks Y) matching your follow distance.
- **Teleportation**: If teleport is enabled and you move beyond the teleport distance, it teleports to you with portal particles and Enderman sound effects.
- **Ranged Combat**: Fires explosive `DragonFireball` projectiles every 40 ticks (2 seconds) at hostile targets up to 48 blocks away.
- **Melee Bite**: Swoops down and bites enemies for 14 damage every 20 ticks within 8.5 blocks, dealing knockback.
- **Guaranteed Friendly Fire Safety**: Can never harm its owner or fellow pets.

### 4. Healing
Right-clicking your tamed dragon with a <img src="https://raw.githubusercontent.com/yigit-guven/Petting/HEAD/src/main/resources/assets/petting/textures/item/golden_star.png" width="16" height="16" alt="Golden Star"> **Golden Star** consumes 1 star and restores **25% of its maximum health** (50 HP out of 200 HP).

### 5. Equipment & Riding
- Right-click with a [Collar](Collars) to equip behavior settings.
- Right-click with a Saddle to enable steering control.
- Mount by pressing **`R`** (up to 18 blocks away).
- Press **Left Click** while riding to shoot a **Dragon Fireball** in the direction you are looking!

---

*Related Pages*: [Golden Treats](Golden-Treats) · [Collars](Collars) · [Riding](Riding) · [Pet Inventory](Pet-Inventory)
