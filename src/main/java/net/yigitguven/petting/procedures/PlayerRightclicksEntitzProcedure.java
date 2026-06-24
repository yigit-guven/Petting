package net.yigitguven.petting.procedures;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.EventPriority;
import java.util.List;

@EventBusSubscriber
public class PlayerRightclicksEntitzProcedure {
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
        
        Entity target = event.getTarget();
        if (target instanceof net.minecraftforge.entity.PartEntity<?> part) {
            target = part.getParent();
        }
		
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(target)) return;

		// Server-side: authoritative action dispatch (must NOT cancel on client-side
		// because that prevents ServerboundInteractPacket from being sent to the server)
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
				// Use stored per-pet mapping, then player default, then hardcoded fallback
				String mapping;
				if (data.contains(key)) {
					mapping = data.getString(key);
				} else {
					String playerDefault = player.getPersistentData().getString(defaultKey);
					mapping = (playerDefault != null && !playerDefault.isEmpty()) ? playerDefault : (isShift ? "CYCLE|NONE" : "SIT|NONE");
				}
				if (player instanceof ServerPlayer serverPlayer) {
					for (String[] rule : parseRules(mapping)) {
						String action = rule[0];
						String condition = rule[1];
						String command = rule.length > 2 ? rule[2] : "";
						if (!conditionMatches(player, target, condition)) continue;
						boolean consumed = handleMappedAction(serverPlayer, target, action, command);
						if (consumed) {
							event.setCancellationResult(InteractionResult.SUCCESS);
							event.setCanceled(true);
							return;
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

	private static boolean handleMappedAction(ServerPlayer player, Entity target, String action, String command) {
		if (player == null || target == null) return false;
		CompoundTag data = target.getPersistentData();
		switch (action) {

		case "TOGGLE_FOLLOW_TELEPORT": {
			int f = data.contains("followdistance") ? data.getInt("followdistance") : 10;
			int t = data.contains("teleportdistance") ? data.getInt("teleportdistance") : 20;
			// swap values
			data.putInt("followdistance", t);
			data.putInt("teleportdistance", f);
			net.yigitguven.petting.PettingMod.PACKET_HANDLER.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> target), new net.yigitguven.petting.network.SendPetSettingsPacket(target.getId(), data.getBoolean("sitstill"), data.getBoolean("waiting"), data.getBoolean("pettingtamed"), data.getBoolean("attackifownerattacks"), data.getBoolean("attackifownerattacked"), data.getBoolean("attackifselfattacked"), data.getBoolean("damageOwner"), data.getBoolean("ignoreWhistle"), data.getInt("followdistance"), data.getInt("teleportdistance"), false, data.contains("control_right_click") ? data.getString("control_right_click") : "SIT", data.contains("control_shift_right_click") ? data.getString("control_shift_right_click") : "CYCLE"));
			return true;
		}

		case "RUN_COMMAND": {
			// Only execute if the player has permission to run the command themselves.
			// Parse the root command name, find it in the dispatcher, and check its
			// required permission level against the player's actual op level.
			if (command != null && !command.isEmpty()) {
				com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher =
						player.getServer().getCommands().getDispatcher();
				// Strip leading slash if present
				String cmd = command.startsWith("/") ? command.substring(1) : command;
				String rootName = cmd.split("\\s+")[0];
				com.mojang.brigadier.tree.CommandNode<net.minecraft.commands.CommandSourceStack> rootNode =
						dispatcher.getRoot().getChild(rootName);
				net.minecraft.commands.CommandSourceStack source = player.createCommandSourceStack();
				// If the command node exists and the player's source can use it, run it.
				// This respects op-level requirements defined by the command itself.
				if (rootNode != null && rootNode.canUse(source)) {
					player.getServer().getCommands().performPrefixedCommand(source, command);
					return true;
				}
				// Permission denied â€” silently skip (no feedback to avoid info leak)
				return true; // still consumed, just not executed
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
				net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player, new net.minecraft.world.SimpleMenuProvider(
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
				// Cycle through follow/teleport preset pairs: (5,10) â†’ (10,20) â†’ (20,50) â†’ (5,10)
				int f = data.contains("followdistance") ? data.getInt("followdistance") : (int)net.yigitguven.petting.config.PettingConfig.FOLLOW_DISTANCE.get().doubleValue();
				int nf, nt;
				if (f <= 5) { nf = 10; nt = 20; }
				else if (f <= 10) { nf = 20; nt = 50; }
				else { nf = 5; nt = 10; }
				net.yigitguven.petting.network.EntitySettingsServer.applyUpdate(player, target.getId(), "followdistance", Integer.toString(nf));
				net.yigitguven.petting.network.EntitySettingsServer.applyUpdate(player, target.getId(), "teleportdistance", Integer.toString(nt));
				return true;
			}
			case "NONE":
			default:
				return false;
		}
	}

	private static boolean conditionMatches(Player player, Entity target, String condition) {
		return switch (condition) {
			case "SADDLE" -> net.yigitguven.petting.util.PetInventoryUtil.hasSaddle(target);
			case "SNEAK" -> player.isShiftKeyDown();
			case "HEALTH_LT_50" -> {
				if (!(target instanceof net.minecraft.world.entity.LivingEntity living)) {
					yield false;
				}
				float max = living.getMaxHealth();
				if (max <= 0.0F) yield false;
				yield (living.getHealth() / max) < 0.5F;
			}
			case "HOLD_ITEM" -> !player.getMainHandItem().isEmpty();
			case "NONE" -> true;
			default -> false;
		};
	}

	private static List<String[]> parseRules(String mapping) {
		List<String[]> rules = new java.util.ArrayList<>();
		if (mapping == null || mapping.isBlank()) return rules;
		String[] rawRules = mapping.split(";");
		for (String raw : rawRules) {
			String token = raw.trim();
			if (token.isEmpty()) continue;
			String action;
			String condition = "NONE";
			String command = "";
			if (token.contains("|")) {
				String[] parts = token.split("\\|", 3);
				action = parts[0].trim();
				if (parts.length > 1 && !parts[1].isBlank()) {
					condition = parts[1].trim();
				}
				if (parts.length > 2) command = parts[2].trim();
			} else {
				action = token;
			}
			rules.add(new String[] { action, condition, command });
		}
		return rules;
	}
}
