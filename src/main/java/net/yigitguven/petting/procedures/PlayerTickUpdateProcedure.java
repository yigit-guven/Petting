package net.yigitguven.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class PlayerTickUpdateProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level(), event.player);
		}
	}

	public static void execute(LevelAccessor world, Entity entity) {
		execute(null, world, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, Entity entity) {
		if (entity == null || !(entity instanceof net.minecraft.world.entity.player.Player player))
			return;

        // Warden Darkness Suppression
        if (player.hasEffect(net.minecraft.world.effect.MobEffects.DARKNESS)) {
            double range = 30.0;
            net.minecraft.world.phys.AABB area = player.getBoundingBox().inflate(range);
            java.util.List<net.minecraft.world.entity.monster.warden.Warden> wardens = player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.warden.Warden.class, area);
            
            for (net.minecraft.world.entity.monster.warden.Warden warden : wardens) {
                if (warden.getPersistentData().getBoolean("pettingtamed")) {
                    String ownerUUID = warden.getPersistentData().getString("ownerUUID");
                    if (ownerUUID.equals(player.getStringUUID())) {
                        player.removeEffect(net.minecraft.world.effect.MobEffects.DARKNESS);
                        break;
                    }
                }
            }
        }

		GoldenWheatItemInHandTickProcedure.execute(world, entity);
	}
}
