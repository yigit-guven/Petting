package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

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

		if (GoldenWheatRightclickedProcedure.execute(target, event.getEntity())) {
			event.setCancellationResult(InteractionResult.SUCCESS);
			event.setCanceled(true);
		}
	}
}
