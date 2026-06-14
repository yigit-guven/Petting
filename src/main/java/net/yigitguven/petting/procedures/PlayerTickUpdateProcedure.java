package net.yigitguven.petting.procedures;

import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.monster.warden.Warden;

@EventBusSubscriber
public class PlayerTickUpdateProcedure {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
        Player player = event.player;
        if (player.level().isClientSide()) return;

        // Warden Darkness Suppression
        if (player.hasEffect(MobEffects.DARKNESS)) {
            double range = 30.0;
            AABB area = player.getBoundingBox().inflate(range);
            java.util.List<Warden> wardens = player.level().getEntitiesOfClass(Warden.class, area);
            
            for (Warden warden : wardens) {
                if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(warden)) continue;

                if (warden.getPersistentData().getBoolean("pettingtamed")) {
                    String ownerUUID = warden.getPersistentData().getString("ownerUUID");
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
