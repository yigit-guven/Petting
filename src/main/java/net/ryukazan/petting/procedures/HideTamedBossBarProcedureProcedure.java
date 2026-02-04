package net.ryukazan.petting.procedures;

import net.ryukazan.petting.init.PettingModGameRules;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel; // Added this import
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber
public class HideTamedBossBarProcedureProcedure {
    
    // Cache to store the BossBar field for each entity class
    private static final Map<Class<?>, Field> BOSS_BAR_FIELD_CACHE = new HashMap<>();

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        execute(event, event.getEntity());
    }

    public static void execute() {
        execute(null, null);
    }

    private static void execute(@Nullable Event event, Entity entity) {
        if (entity == null) return;
        
        // 1. Run only on Server Side
        if (entity.level().isClientSide()) return;

        // 2. Optimization: Run once per second
        if (entity.tickCount % 20 != 0) return;

        // 3. Check if the entity is Tamed
        boolean isTamed = false;
        CompoundTag data = entity.getPersistentData();
        
        // FIX 1: Added .orElse(false) because your compiler says getBoolean returns Optional
        if (data.getBoolean("pettingtamed").orElse(false)) {
            isTamed = true;
        } else if (entity instanceof TamableAnimal tamable && tamable.isTame()) {
            isTamed = true;
        }

        if (!isTamed) return;

        // FIX 2: Cast to ServerLevel to safely access GameRules
        if (entity.level() instanceof ServerLevel serverLevel) {
            boolean hideBar = serverLevel.getGameRules().getBoolean(PettingModGameRules.HIDE_TAMED_BOSS_BAR);

            // 5. Update Boss Bar Visibility using Reflection
            try {
                Field bossEventField = getBossBarField(entity.getClass());
                
                if (bossEventField != null) {
                    ServerBossEvent bossEvent = (ServerBossEvent) bossEventField.get(entity);
                    if (bossEvent != null) {
                        if (bossEvent.isVisible() == hideBar) {
                            bossEvent.setVisible(!hideBar);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore reflection errors
            }
        }
    }

    private static Field getBossBarField(Class<?> clazz) {
        if (BOSS_BAR_FIELD_CACHE.containsKey(clazz)) {
            return BOSS_BAR_FIELD_CACHE.get(clazz);
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (ServerBossEvent.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                BOSS_BAR_FIELD_CACHE.put(clazz, field);
                return field;
            }
        }

        BOSS_BAR_FIELD_CACHE.put(clazz, null);
        return null;
    }
}