package net.yigitguven.petting.procedures;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;

import javax.annotation.Nullable;

@EventBusSubscriber
public class PlayerRightclicksEntitzProcedure {
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		execute(event, event.getTarget(), event.getEntity());
	}

	public static void execute(Entity entity, Entity sourceentity) {
		execute(null, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (GoldenWheatRightclickedProcedure.execute(entity, sourceentity)) {
			if (event instanceof PlayerInteractEvent.EntityInteract interactEvent) {
				interactEvent.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
				if (event instanceof net.neoforged.bus.api.ICancellableEvent _c) _c.setCanceled(true);
			} else if (event instanceof net.neoforged.bus.api.ICancellableEvent _cx) _cx.setCanceled(true);
		}
	}
}




