package net.ryukazan.petting.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import javax.annotation.Nullable;

@EventBusSubscriber
public class EntityTickUpdateProcedure {
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        execute(event, event.getEntity());
    }

    public static void execute(Entity entity) {
        execute(null, entity);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity == null)
            return;
            
        if (entity.getPersistentData().getBooleanOr("pettingtamed", false)) {
            if (entity instanceof Mob mob) {
                // We only run this if persistence isn't set yet to avoid log spam
                if (!mob.isPersistenceRequired()) {
                    mob.setPersistenceRequired();
                }
            }
        }
    }
}