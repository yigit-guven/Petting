package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.warden.Warden;

@EventBusSubscriber
public class PlayerTickUpdateProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        // Warden Darkness Suppression
        if (player.hasEffect(MobEffects.DARKNESS)) {
            double range = 30.0;
            AABB area = player.getBoundingBox().inflate(range);
            java.util.List<Warden> wardens = player.level().getEntitiesOfClass(Warden.class, area);
            
            for (Warden warden : wardens) {
                if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(warden)) continue;

                if (warden.getPersistentData().getBooleanOr("pettingtamed", false)) {
                    String ownerUUID = warden.getPersistentData().getStringOr("ownerUUID", "");
                    if (ownerUUID.equals(player.getStringUUID())) {
                        player.removeEffect(MobEffects.DARKNESS);
                        break;
                    }
                }
            }
        }

		GoldenWheatItemInHandTickProcedure.execute(player.level(), player);
	}
}
