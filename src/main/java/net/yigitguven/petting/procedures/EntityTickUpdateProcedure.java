package net.yigitguven.petting.procedures;

import net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.config.PettingConfig;

@EventBusSubscriber
public class EntityTickUpdateProcedure {
    private static final java.util.UUID ARMOR_MODIFIER_ID = java.util.UUID.fromString("b1b2c3d4-e5f6-7777-8888-999999999999");
    private static final java.util.UUID TOUGHNESS_MODIFIER_ID = java.util.UUID.fromString("c1b2c3d4-e5f6-7777-8888-999999999999");
    
    /**
     * Immediately mark tamed pets as persistent when they join/load into a level,
     * rather than waiting for the first tick. This prevents chunk-unload removal.
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Mob mob)) return;
        if (mob.getPersistentData().getBoolean("pettingtamed") && !mob.isPersistenceRequired()) {
            mob.setPersistenceRequired();
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingTickEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (entity.level().isClientSide()) return;
        
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(living)) return;
            
        if (living.getPersistentData().getBoolean("pettingtamed")) {
            if (living instanceof Mob mob) {
                if (!mob.isPersistenceRequired()) {
                    mob.setPersistenceRequired();
                }

                // Prevent tamed creepers with a bed from exploding.
                // Creeper.explodeCreeper() calls discard() directly, bypassing LivingDeathEvent.
                // Resetting swellDir aborts the explosion before it can fire.
                if (mob instanceof Creeper creeper
                        && mob.getPersistentData().contains("pet_bed_loc_x")
                        && creeper.getSwellDir() > 0) {
                    creeper.setSwellDir(-1);
                }

                updateAttribute(mob, Attributes.ARMOR, ARMOR_MODIFIER_ID, net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR.get());
                updateAttribute(mob, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_ID, net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR_TOUGHNESS.get());

                if (net.yigitguven.petting.config.PettingConfig.SIT_HEAL_ENABLED.get() && living.getPersistentData().getBoolean("sitstill") && !living.isVehicle()) {
                    if (living.tickCount % net.yigitguven.petting.config.PettingConfig.SIT_HEAL_INTERVAL.get() == 0) {
                        if (mob.getHealth() < mob.getMaxHealth()) {
                            mob.heal(net.yigitguven.petting.config.PettingConfig.SIT_HEAL_AMOUNT.get().floatValue());
                        }
                    }
                }
            }
        }
    }

    private static void updateAttribute(Mob mob, net.minecraft.world.entity.ai.attributes.Attribute attribute, java.util.UUID id, double value) {
        var inst = mob.getAttribute(attribute);
        if (inst == null) return;

        if (value > 0) {
            if (inst.getModifier(id) == null) {
                inst.addTransientModifier(new AttributeModifier(id, "Petting Modifier", value, AttributeModifier.Operation.ADDITION));
            } else {
                AttributeModifier existing = inst.getModifier(id);
                if (existing != null && existing.getAmount() != value) {
                    inst.removeModifier(id);
                    inst.addTransientModifier(new AttributeModifier(id, "Petting Modifier", value, AttributeModifier.Operation.ADDITION));
                }
            }
        } else if (inst.getModifier(id) != null) {
            inst.removeModifier(id);
        }
    }
}
