package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.living.LivingEvent; // NEW IMPORT
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;

@EventBusSubscriber
public class EntityTickUpdateProcedure {
    
    // FIXED: Use LivingTickEvent
    @SubscribeEvent
    public static void onEntityTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
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




