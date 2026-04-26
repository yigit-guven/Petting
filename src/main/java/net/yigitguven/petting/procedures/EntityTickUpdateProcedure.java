package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.config.PettingConfig;

@EventBusSubscriber
public class EntityTickUpdateProcedure {
    private static final ResourceLocation ARMOR_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "pet_base_armor");
    private static final ResourceLocation TOUGHNESS_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "pet_base_toughness");
    
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        
        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(living)) return;
            
        if (living.getPersistentData().getBooleanOr("pettingtamed", false)) {
            if (living instanceof Mob mob) {
                if (!mob.isPersistenceRequired()) {
                    mob.setPersistenceRequired();
                }

                updateAttribute(mob, Attributes.ARMOR, ARMOR_MODIFIER_ID, net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR.get());
                updateAttribute(mob, Attributes.ARMOR_TOUGHNESS, TOUGHNESS_MODIFIER_ID, net.yigitguven.petting.config.PettingConfig.PET_BASE_ARMOR_TOUGHNESS.get());

                if (net.yigitguven.petting.config.PettingConfig.SIT_HEAL_ENABLED.get() && living.getPersistentData().getBooleanOr("sitstill", false) && !living.isVehicle()) {
                    if (living.tickCount % net.yigitguven.petting.config.PettingConfig.SIT_HEAL_INTERVAL.get() == 0) {
                        if (mob.getHealth() < mob.getMaxHealth()) {
                            mob.heal(net.yigitguven.petting.config.PettingConfig.SIT_HEAL_AMOUNT.get().floatValue());
                        }
                    }
                }
            }
        }
    }

    private static void updateAttribute(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, ResourceLocation id, double value) {
        var inst = mob.getAttribute(attribute);
        if (inst == null) return;

        if (value > 0) {
            if (!inst.hasModifier(id)) {
                inst.addTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
            } else {
                AttributeModifier existing = inst.getModifier(id);
                if (existing != null && existing.amount() != value) {
                    inst.removeModifier(id);
                    inst.addTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
                }
            }
        } else if (inst.hasModifier(id)) {
            inst.removeModifier(id);
        }
    }
}
