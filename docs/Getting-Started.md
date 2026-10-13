# Getting Started

This page walks through the shortest path from a fresh world to a tamed, collared pet. Each step links to the page with the full rules.

## 1. Craft a golden treat

Every mob is tamed with a *golden treat*. The three most common ones:

| Treat | Recipe (3x3, `N` = gold nugget) | Used for |
|---|---|---|
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_wheat.png" width="24" alt=""> Golden Wheat | 8 gold nuggets around 1 wheat | Peaceful / ordinary mobs |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_bone.png" width="24" alt=""> Golden Bone | Gold nuggets in the corners, gold ingots on the edges, 1 bone in the centre | Skeletons, wolves and (as fallback) hostile mobs |
| <img src="https://raw.githubusercontent.com/yigit-guven/Petting/26.3-NeoForge/src/main/resources/assets/petting/textures/item/golden_star.png" width="24" alt=""> Golden Star | 8 gold blocks around 1 nether star | Bosses and other very strong mobs |

All nine treats, their recipes and the mobs they are tied to are listed on [Golden Treats](Golden-Treats).

## 2. Weaken the mob if it is hostile

By default a **hostile** mob (anything implementing Minecraft's `Enemy` interface) can only be tamed while its health is **at or below 30 %** of its maximum. Peaceful mobs do not need to be weakened by default. Both rules are configurable, see [Configuration](Configuration#taming).

## 3. Right-click the mob with the treat

- The treat must be in your **main hand**.
- By default 33.3 % of attempts fail. A failed attempt still consumes the treat.
- In Creative mode treats are not consumed.

Details: [Taming](Taming).

## 4. Command your pet

| Action | Default input |
|---|---|
| Sit / stand | Right-click the pet |
| Open the pet inventory | Shift + right-click the pet |
| Ride / dismount | `R` |
| Heal the pet | Right-click with a treat that fits it (heals 50 % of max health) |

All three actions are rebindable key mappings, see [Controls](Controls).

## 5. Add a collar (optional but recommended)

A collar stores the pet's combat mode, follow distance, teleport settings and more.

1. Craft a collar: 5 leather, 1 dye and 1 gold nugget ([Collars](Collars#crafting)).
2. Right-click your pet with the collar, or place it in the pet's Collar slot.
3. Open the pet inventory and use the **Collar** tab on the left to change settings ([Pet Inventory](Pet-Inventory#collar-tab)).

## 6. Where to go next

- Let a pet come back after death: [Collar Respawn](Collar-Respawn).
- Ride your pet: [Riding](Riding).
- Tame the Ender Dragon: [Golden Dragon Egg](Golden-Dragon-Egg).

> [!NOTE]
> The client configuration hides every Petting chat notification by default (taming success, healing, sitting, and so on). If you see no messages, that is expected. See [Chat Messages](Chat-Messages) for how to turn them on.
