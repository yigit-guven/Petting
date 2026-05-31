## Changelog

### Added / Fixed
- Added a complete pet settings and key mapping UI system, including pet settings, control mappings, and command-input screens where there was no UI in the previous release.
- Fixed right-click owner pet mapping handling so custom `control_right_click` and `control_shift_right_click` rules now override legacy sit-toggle behavior reliably.
- Ensured owned-pet right-click mappings always use a default fallback (`SIT|NONE` for right-click, `CYCLE|NONE` for shift-right-click) when no per-pet or player default mapping exists.
- Added event priority to `PlayerRightclicksEntitzProcedure` so the new mapping handler runs before legacy pet interaction logic.
- Prevented the legacy `OwnerRightclicksPetProcedure` from processing events already cancelled by the mapping handler.
- Added `RUN_COMMAND` support in key mapping rules and implemented `CommandInputScreen` so command strings can be entered, stored, and executed.
- Fixed key mapping UI behavior to avoid stale mapping data and improved screen navigation when editing RUN_COMMAND rules.
- Fixed pet settings toggles and follow/teleport distance updates to send correct server payloads and sync properly.
- Fixed goat horn whistle handling so it now teleports pets when enabled, without requiring shift-click.
- Fixed golden wheat taming logic so failed tame attempts no longer update mob names or change the client-side entity display.
- Updated legacy tool-based pet boolean toggles to use server-side `EntitySettingsServer.applyUpdate` for proper validation and client sync.
- Ensured `ignoreWhistle` is correctly respected by pet whistle teleport logic.
- Added permission-aware `RUN_COMMAND` execution so pets only run embedded commands if the owner actually has permission to execute that command.
- Fixed blacklist handling for taming by default enabling pet blacklist support and making blacklisted entities fully transparent to Petting interactions, preventing Create package duplication glitches.
- Fixed bee death loop when bound to a pet bed after stinging an enemy; the bed save now resets the bee's stinger state so it no longer immediately re-triggers death.