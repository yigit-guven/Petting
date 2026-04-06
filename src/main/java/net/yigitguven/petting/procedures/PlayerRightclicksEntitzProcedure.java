package net.yigitguven.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerRightclicksEntitzProcedure {
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
        Entity target = event.getTarget();
        if (target instanceof net.minecraftforge.entity.PartEntity<?> part) {
            target = part.getParent();
        }
		execute(event, target, event.getEntity());
	}

	public static void execute(Entity entity, Entity sourceentity) {
		execute(null, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		
        // --- GLOBAL BLACKLIST CHECK ---
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;

		if (GoldenWheatRightclickedProcedure.execute(entity, sourceentity)) {
			if (event instanceof PlayerInteractEvent.EntityInteract interactEvent) {
				interactEvent.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
				interactEvent.setCanceled(true);
			} else if (event != null && event.isCancelable()) {
				event.setCanceled(true);
			}
		}
	}
}
