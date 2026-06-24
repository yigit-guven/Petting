# Petting 4.2.3-beta.4

## Bug Fixes:
- **Interaction Fix**: Fixed a bug introduced in 4.2.3-beta.3 where tamed mobs, especially those with custom GUIs or on the GUI blacklist, became completely non-interactable with an empty hand due to aggressive event cancellation.

## Performance Optimizations:
- **Tick Handlers Optimization**: Reduced the execution frequency of heavy tick handlers (Warden Darkness suppression, Golden Wheat temptation, and Pet Attribute updates). These now run periodically (e.g., every 10-20 ticks) instead of every single tick, drastically reducing server CPU overhead.
- **Temptation Memory Leak Fix**: Golden Wheat temptation now properly cleans up `TemptedByGoldenWheat` tags and AI states when an animal wanders out of range or the player stops holding wheat.