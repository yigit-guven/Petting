# Petting 4.2.3-beta.2 (Forge 1.20.1)

This version is a comprehensive backport of the latest features from NeoForge 1.21.1 to Forge 1.20.1!

## Major Changes and Backported Features:
- **Registry System Overhaul**: Migrated from NeoForge's \DeferredHolder\ and \DeferredItem\ back to Forge's \RegistryObject\.
- **Networking Modernization**: Migrated the \Payload\ based networking to Forge's \SimpleChannel\ and \PacketDistributor\.
- **Capability System Reset**: Downgraded the NeoForge Attachment system back to the classic Forge \Capability\ system for pet inventories.
- **Attributes System**: Adjusted UUID-based \AttributeModifier\ system to match the legacy 1.20.1 signature. Reverted \Attribute\ registry logic to use Supplier-based definitions.
- **Config & GUI Enhancements**: Ported the \ConfigScreenFactory\ implementation to use the older \ConfigScreenHandler\. Also resolved numerous screen rendering changes, falling back to 1.20.1 compatible enderBackground\ and GUI drawing methods.
- **Event Bus Adjustments**: Re-mapped \LivingEvent\, \LivingDamageEvent\, and Menu Screen Registration events to their appropriate 1.20.1 equivalents.
- **Controls & Keybindings Overhaul**: Replaced brittle manual thread polling with native Forge \RegisterKeyMappingsEvent\ and \InputEvent.Key\ for responsive and reliable Pet Settings (P) activation.
- **Settings UI & Network Synchronization**: Resolved missing Network Packet registrations that caused the Settings UI to silently fail, fully restoring bidirectional UI-to-Server syncing.
- **Translation Keys & Localization**: Performed a massive extraction and injection of missing localization strings, tooltips, and dynamic action mapping states into `en_us.json` for proper English formatting.