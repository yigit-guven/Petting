package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import java.util.function.Consumer;

@EventBusSubscriber
public class PlayerRightclicksEntitzProcedure {
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
        
        Entity target = event.getTarget();
        if (target instanceof net.neoforged.neoforge.entity.PartEntity<?> part) {
            target = part.getParent();
        }
		
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;

		// If this is an owned pet, handle per-pet control mappings first (server-side authoritative)
		Player player = event.getEntity();
		if (!player.level().isClientSide() && target != null) {
			CompoundTag data = target.getPersistentData();
			boolean isOwner = false;
			if (data.contains("ownerUUID") && data.getString("ownerUUID").equals(player.getStringUUID())) isOwner = true;
			else if (target instanceof net.minecraft.world.entity.TamableAnimal tamable) {
				if (tamable.isOwnedBy(player)) isOwner = true;
			}

			if (isOwner) {
				boolean isShift = player.isShiftKeyDown();
				String key = isShift ? "control_shift_right_click" : "control_right_click";
				String defaultKey = isShift ? "petting_default_control_shift_right_click" : "petting_default_control_right_click";
				String mapping = data.contains(key) ? data.getString(key) : player.getPersistentData().getString(defaultKey);
				if (mapping != null && !mapping.isEmpty()) {
					// parse ACTION|CONDITION or legacy ACTION
					String action = mapping;
					String condition = "NONE";
					if (mapping.contains("|")) {
						String[] parts = mapping.split("\\|", 2);
						action = parts[0];
						condition = parts[1];
					}

					// check condition
							if ("SADDLE".equals(condition) && !net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(target)) {
						// condition not met -> fallthrough to normal handling
							} else if ("SNEAK".equals(condition) && !player.isShiftKeyDown()) {
						// condition not met
							} else if ("HEALTH_LT_50".equals(condition)) {
								if (!(target instanceof net.minecraft.world.entity.LivingEntity living)) {
									// not a living entity
									// fallthrough
								} else {
									float hp = living.getHealth();
									float max = living.getMaxHealth();
									if ((hp / max) * 100.0F >= 50.0F) {
										// condition not met
										// fallthrough
										;
									}
								}
							} else if ("HOLD_ITEM".equals(condition)) {
								if (player.getMainHandItem().isEmpty()) {
									// not holding item => condition not met
								}
					} else {
						// perform action server-side
								if (player instanceof ServerPlayer serverPlayer) {
									boolean consumed = handleMappedAction(serverPlayer, target, action);
							if (consumed) {
								event.setCancellationResult(InteractionResult.SUCCESS);
								event.setCanceled(true);
								return;
							}
						}
					}
				}
			}
		}

		if (GoldenWheatRightclickedProcedure.execute(target, event.getEntity())) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}

	private static boolean handleMappedAction(ServerPlayer player, Entity target, String action) {
		if (player == null || target == null) return false;
		CompoundTag data = target.getPersistentData();
		switch (action) {

		case "TOGGLE_FOLLOW_TELEPORT": {
			int f = data.contains("followdistance") ? data.getInt("followdistance") : 10;
			int t = data.contains("teleportdistance") ? data.getInt("teleportdistance") : 20;
			// swap values
			data.putInt("followdistance", t);
			data.putInt("teleportdistance", f);
			net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingEntity(target, new net.yigitguven.petting.network.SendPetSettingsPayload(target.getId(), data.getBoolean("sitstill"), data.getBoolean("waiting"), data.getBoolean("pettingtamed"), data.getBoolean("attackifownerattacks"), data.getBoolean("attackifownerattacked"), data.getBoolean("attackifselfattacked"), data.getBoolean("damageOwner"), data.getBoolean("ignoreWhistle"), data.getInt("followdistance"), data.getInt("teleportdistance"), data.contains("control_right_click") ? data.getString("control_right_click") : "SIT", data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : "CYCLE"));
			return true;
		}

		case "RUN_COMMAND": {
			// run a per-pet command if configured in NBT key control_command_right_click
			String cmdKey = "control_command_" + (player.isShiftKeyDown() ? "shift_right_click" : "right_click");
			if (data.contains(cmdKey)) {
				String cmd = data.getString(cmdKey);
				if (cmd != null && !cmd.isEmpty()) {
					// Use dispatcher.execute to run command string
					player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), cmd);
					return true;
				}
			}
			return false;
		}
			case "SIT": {
				boolean current = data.getBoolean("sitstill");
				String newVal = Boolean.toString(!current);
				net.yigitguven.petting.network.EntitySettingsServer.applyUpdate(player, target.getId(), "sitstill", newVal);
				return true;
			}
			case "TOGGLE_WAIT": {
				boolean current = data.getBoolean("waiting");
				String newVal = Boolean.toString(!current);
				net.yigitguven.petting.network.EntitySettingsServer.applyUpdate(player, target.getId(), "waiting", newVal);
				return true;
			}
			case "RIDE": {
				if (!net.yigitguven.petting.util.PetInventoryUtil.isRidingAllowed(target)) return false;
				// If config requires saddle, ensure pet has saddle
				if (net.yigitguven.petting.config.PettingConfig.MOUNT_REQUIRE_SADDLE.get() && !net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(target)) return false;
				player.startRiding(target, true);
				return true;
			}
			case "OPEN_INV": {
				if (!net.yigitguven.petting.util.PetInventoryUtil.isInventoryAllowed(target)) return false;
				player.openMenu(new net.minecraft.world.SimpleMenuProvider(
					(id, inv, p) -> new net.yigitguven.petting.world.inventory.PetInventoryMenu(id, inv, target),
					target.getDisplayName()
				), buf -> buf.writeInt(target.getId()));
				return true;
			}
			case "OPEN_SETTINGS": {
				net.yigitguven.petting.network.EntitySettingsServer.handleOpenRequest(player, target.getId());
				return true;
			}
			case "CYCLE": {
				// cycle follow distance similarly to existing logic (5->10->20->50)
				int current = data.getInt("followdistance");
				if (current == 0) current = 5;
				int next = switch (current) {
					case 5 -> 10;
					case 10 -> 20;
					case 20 -> 50;
					default -> 5;
				};
				net.yigitguven.petting.network.EntitySettingsServer.applyUpdate(player, target.getId(), "followdistance", Integer.toString(next));
				return true;
			}
			case "NONE":
			default:
				return false;
		}
	}
}
