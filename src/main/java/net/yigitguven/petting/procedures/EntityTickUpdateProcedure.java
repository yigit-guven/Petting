package net.yigitguven.petting.procedures;

import net.minecraftforge.event.entity.living.LivingEvent; // NEW IMPORT
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EntityTickUpdateProcedure {
    
    // FIXED: Use LivingTickEvent
    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        execute(event, event.getEntity());
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity == null) return;
        
        // --- GLOBAL BLACKLIST CHECK ---
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(entity)) return;
            
        if (entity.getPersistentData().getBoolean("pettingtamed")) {
            if (entity instanceof Mob mob) {
                if (!mob.isPersistenceRequired()) {
                    mob.setPersistenceRequired();
                }

                // Sitting Regeneration
                if (net.yigitguven.petting.config.PettingConfig.SIT_HEAL_ENABLED.get() && entity.getPersistentData().getBoolean("sitstill") && !entity.isVehicle()) {
                    if (entity.tickCount % net.yigitguven.petting.config.PettingConfig.SIT_HEAL_INTERVAL.get() == 0) {
                        if (mob.getHealth() < mob.getMaxHealth()) {
                            mob.heal(net.yigitguven.petting.config.PettingConfig.SIT_HEAL_AMOUNT.get().floatValue());
                        }
                    }
                }
            }
        }
    }
}
