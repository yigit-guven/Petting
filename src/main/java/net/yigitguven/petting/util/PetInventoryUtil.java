package net.yigitguven.petting.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import java.lang.reflect.Method;

public class PetInventoryUtil {
    private static Method CAN_EQUIP_STACK_METHOD;

    static {
        try {
            // m_6072_ is the mapped name for canEquipStack in 1.20.1
            CAN_EQUIP_STACK_METHOD = ObfuscationReflectionHelper.findMethod(Mob.class, "m_6072_", ItemStack.class);
            CAN_EQUIP_STACK_METHOD.setAccessible(true);
        } catch (Exception e) {
            try {
                CAN_EQUIP_STACK_METHOD = ObfuscationReflectionHelper.findMethod(Mob.class, "canEquipStack", ItemStack.class);
                CAN_EQUIP_STACK_METHOD.setAccessible(true);
            } catch (Exception e2) {
                // Silently fail, fallback logic will be used
            }
        }
    }

    public static boolean isSlotSupported(Entity entity, EquipmentSlot slotType) {
        if (slotType == null) return true; // Saddle
        
        // Check Config Override first
        String registryName = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        if (net.yigitguven.petting.config.PettingConfig.EXTRA_EQUIPPABLE_MOBS.get().contains(registryName)) {
            return true;
        }

        if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand) return true;
        
        if (entity instanceof Mob mob) {
            if (CAN_EQUIP_STACK_METHOD != null) {
                try {
                    ItemStack testStack;
                    if (slotType.getType() == EquipmentSlot.Type.ARMOR) {
                        testStack = new ItemStack(Items.IRON_CHESTPLATE);
                    } else {
                        testStack = new ItemStack(Items.IRON_SWORD);
                    }
                    return (boolean) CAN_EQUIP_STACK_METHOD.invoke(mob, testStack);
                } catch (Exception e) {
                    // Fallback to humanoid check
                }
            }
            
            // Humanoid fallback
            if (mob instanceof net.minecraft.world.entity.monster.Zombie || 
                mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton ||
                mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin) {
                return true;
            }
        }
        
        return false;
    }
}
