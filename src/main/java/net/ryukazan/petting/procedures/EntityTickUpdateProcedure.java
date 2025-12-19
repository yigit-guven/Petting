package net.ryukazan.petting.procedures;

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
            
        if (entity.getPersistentData().getBoolean("pettingtamed")) {
            if (entity instanceof Mob mob) {
                if (!mob.isPersistenceRequired()) {
                    mob.setPersistenceRequired();
                }
            }
        }
    }
}