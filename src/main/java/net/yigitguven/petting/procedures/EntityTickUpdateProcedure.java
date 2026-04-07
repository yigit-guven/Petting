package net.yigitguven.petting.procedures;

import net.minecraftforge.event.entity.living.LivingEvent; // NEW IMPORT
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.UUID;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class EntityTickUpdateProcedure {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("7e3e9a40-349f-4d33-a3d2-3c118b84346e");
    private static final UUID TOUGHNESS_MODIFIER_UUID = UUID.fromString("b0c74f5d-7a6e-4f1e-9e7a-9f8d7c6b5a41");
    
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

                // Apply Base Armor/Toughness Bonus
                updateAttribute(mob, Attributes.ARMOR, ARMOR_MODIFIER_UUID, "Pet Base Armor Bonus", net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR.get());
                updateAttribute(mob, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_UUID, "Pet Base Toughness Bonus", net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR_TOUGHNESS.get());

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

    private static void updateAttribute(Mob mob, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid, String name, double value) {
        var inst = mob.getAttribute(attribute);
        if (inst == null) return;

        AttributeModifier existing = inst.getModifier(uuid);
        if (value > 0) {
            if (existing == null || existing.getAmount() != value) {
                if (existing != null) inst.removeModifier(uuid);
                inst.addTransientModifier(new AttributeModifier(uuid, name, value, AttributeModifier.Operation.ADDITION));
            }
        } else if (existing != null) {
            inst.removeModifier(uuid);
        }
    }
}
