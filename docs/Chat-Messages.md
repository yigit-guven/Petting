# Chat Messages

**Petting** features a granular client-side feedback filtering system. Players can independently toggle individual chat notifications on or off in `petting-client.toml` to suit their preferences without cluttering chat logs.

---

## Message Registry & Configuration Toggles

Below is the complete reference of every chat message produced by the mod, its English translation, why it triggers, and the exact configuration option that controls its display.

| Translation Key | English Text | Trigger Scenario | Controlling Config (`petting-client.toml`) |
|---|---|---|---|
| `petting.action.tamed_success` | `You tamed %s!` | Successfully taming any mob with a treat. | `enableTamedSuccessFeedback` (default `false`) |
| `petting.action.taming_failed` | `Taming attempt failed!` | Treat is consumed but RNG failed the `unsuccessfulTamingChance` roll. | `enableTamingFailedFeedback` (default `false`) |
| `petting.action.too_healthy` | `This mob is too healthy to tame! Weaken it below %d%% health first.` | Attempting to tame a hostile mob above `maxHealthPercentage`. | `enableTooHealthyFeedback` (default `false`) |
| `petting.action.already_tamed_other` | `This mob already belongs to someone else.` | Interacting with a pet owned by a different player. | `enableAlreadyTamedOtherFeedback` (default `false`) |
| `petting.action.already_vanilla_tamed` | `This mob has already bonded with someone through vanilla taming.` | Interacting with a mob bonded to someone else via vanilla taming. | `enableAlreadyVanillaTamedFeedback` (default `false`) |
| `petting.action.pet_healed` | `%s was healed!` | Right-clicking an injured owned pet with a valid treat. | `enablePetHealedFeedback` (default `false`) |
| `petting.action.pet_full_health` | `%s is already at full health.` | Right-clicking an owned pet with a treat when it is already at 100% HP. | `enablePetFullHealthFeedback` (default `false`) |
| `petting.action.pet_limit_reached` | `You have reached your pet limit (%s)!` | Attempting to tame a mob when you have reached `maxPetCount`. | `enablePetLimitReachedFeedback` (default `false`) |
| `petting.action.pet_sitting` | `%s is now sitting.` | Commanding an owned pet to sit. | `enablePetSittingFeedback` (default `false`) |
| `petting.action.pet_standing` | `%s is now following.` | Commanding a sitting pet to stand and follow. | `enablePetStandingFeedback` (default `false`) |
| `petting.action.collar_bound` | `Collar bound to %s at [%d, %d, %d].` | Right-clicking a bed or cushion with a collar in hand. | `enableCollarBoundFeedback` (default `false`) |
| `petting.action.mount_controlled` | `Mounted %s. Full steering control enabled.` | Mounting a pet that has an equipped saddle. | `enableMountControlledFeedback` (default `false`) |
| `petting.action.mount_uncontrolled` | `Mounted %s without a saddle. You cannot steer it.` | Mounting a pet without a saddle when unsaddled riding is permitted. | `enableMountUncontrolledFeedback` (default `false`) |
| `petting.action.saddle_required` | `This pet requires an equipped saddle to be ridden!` | Attempting to mount an unsaddled pet when `allowRidingWithoutSaddle = false`. | `enableSaddleRequiredFeedback` (default `false`) |
| `petting.action.pet_died` | `Your pet %s died!` | An owned pet dies without a valid respawn anchor. | `enablePetDiedFeedback` or `enablePetDeathMessages` |
| `petting.action.pet_respawned` | `Your pet %s died and respawned at its %s!` | An owned pet dies and resurrects at its bound bed or cushion. | `enablePetRespawnedFeedback` or `enablePetDeathMessages` |
| `petting.action.pet_respawn_failed` | `Your pet %s died and could not respawn because its %s was missing!` | An owned pet dies with a bound collar, but the anchor block was broken. | `enablePetRespawnFailedFeedback` or `enablePetDeathMessages` |
| `petting.action.egg_converted_to_golden` | `The Dragon Egg radiates golden energy and transforms into a Golden Dragon Egg!` | Right-clicking a vanilla Dragon Egg with a Golden Star. | `enableEggConvertedFeedback` |

---

## Unfiltered System Messages

The following gameplay messages are **always displayed** to ensure players receive vital ritual instructions and announcements:

| Translation Key | English Text | Trigger Scenario |
|---|---|---|
| `petting.action.dragon_cannot_be_tamed_directly` | `The Ender Dragon cannot be tamed directly! Infuse its egg with a Golden Star and summon it at the End Exit Portal.` | Right-clicking a wild, untamed Ender Dragon with any golden treat. |
| `petting.action.golden_dragon_hatched` | `Your Golden Ender Dragon has hatched and bonded with you!` | Sent to the owner upon successful completion of the End Exit Portal ritual. |
| `petting.action.golden_dragon_summoned` | `%s has summoned a tamed Golden Ender Dragon!` | Broadcast to all other players when a tamed dragon is summoned. |
| `petting.action.golden_egg_info_owner` | `This Golden Dragon Egg is infused by %s. Summon it at the End Exit Portal using 4 End Crystals.` | Shift + Right-Clicking a placed Golden Dragon Egg that has a registered owner. |
| `petting.action.golden_egg_info_unbound` | `This Golden Dragon Egg is ready to hatch! Place it at the End Exit Portal with 4 End Crystals.` | Shift + Right-Clicking a placed Golden Dragon Egg that has no owner data. |
| `commands.petting.summon.success` | `Summoned %s as a pet for %s` | Feedback to the admin executing `/summon pet`. |
| `commands.petting.summon.error.not_a_mob` | `Target entity is not a mob and cannot be tamed as a pet.` | Command error when trying to spawn a non-mob entity. |

---

## Server vs. Client Authority

1. **Server Authority**: The server configurations `enablePetDeathMessages` and `enableEggConvertedFeedback` determine whether the server dispatches the notification to the player. If the server disables them, no message is transmitted.
2. **Client Authority**: If the server sends the message, the player's client checks `petting-client.toml`. If the toggle for that specific key is `false`, the client suppresses it locally from the chat window.

---

*Related Pages*: [Configuration](Configuration) · [Taming](Taming) · [Collar Respawn](Collar-Respawn)
