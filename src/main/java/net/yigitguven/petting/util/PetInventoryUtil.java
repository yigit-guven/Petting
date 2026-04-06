package net.yigitguven.petting.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
    
    public static boolean isBlacklisted(Entity entity) {
        if (entity == null || !PettingConfig.BLACKLIST_ENABLED.get()) return false;
        String id = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        List<? extends String> blacklist = PettingConfig.TAMING_BLACKLIST.get();
        return blacklist.contains(id);
    }

    /**
     * Resolves an item from a registry ID string. Fallback to default if invalid.
     */
    public static net.minecraft.world.item.Item getItemFromID(String id, net.minecraft.world.item.Item fallback) {
        if (id == null || id.isEmpty()) return fallback;
        try {
            net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null && item != Items.AIR) {
                return item;
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    public static boolean isSlotSupported(Entity entity, EquipmentSlot slotType) {
        if (!isInventoryAllowed(entity)) return false;
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

    public static boolean isInventoryAllowed(Entity entity) {
        String name = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        
        // 1. Blacklist check (highest priority)
        if (net.yigitguven.petting.config.PettingConfig.INVENTORY_BLACKLIST_ENABLED.get()) {
            if (net.yigitguven.petting.config.PettingConfig.INVENTORY_BLACKLIST.get().contains(name)) {
                return false;
            }
        }
        
        // 2. Whitelist check
        if (net.yigitguven.petting.config.PettingConfig.INVENTORY_WHITELIST_ONLY.get()) {
            return net.yigitguven.petting.config.PettingConfig.INVENTORY_WHITELIST.get().contains(name);
        }
        
        return true;
    }

    public static boolean isRidingAllowed(Entity entity) {
        if (!net.yigitguven.petting.config.PettingConfig.ALLOW_PET_RIDING.get()) return false;
        
        String name = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        
        // 1. Blacklist check
        if (net.yigitguven.petting.config.PettingConfig.RIDING_BLACKLIST_ENABLED.get()) {
            if (net.yigitguven.petting.config.PettingConfig.RIDING_BLACKLIST.get().contains(name)) {
                return false;
            }
        }
        
        // 2. Whitelist check
        if (net.yigitguven.petting.config.PettingConfig.RIDING_WHITELIST_ONLY.get()) {
            return net.yigitguven.petting.config.PettingConfig.RIDING_WHITELIST.get().contains(name);
        }
        
        return true;
    }

    public static boolean hasSaddle(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        return living.getCapability(net.yigitguven.petting.capability.PetInventoryCapability.PET_INVENTORY).map(handler -> {
            ItemStack stack = handler.getStackInSlot(0);
            return !stack.isEmpty() && (stack.is(Items.SADDLE) || stack.getItem().getDescriptionId().contains("saddle"));
        }).orElse(false);
    }

    public static boolean isFlyingMob(Entity entity) {
        if (!(entity instanceof Mob mob)) return false;
        
        // 1. Direct interface check
        if (mob instanceof net.minecraft.world.entity.animal.FlyingAnimal) return true;
        
        // 2. Navigation check
        if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.FlyingPathNavigation) return true;
        
        // 3. Move Control check
        if (mob.getMoveControl() instanceof net.minecraft.world.entity.ai.control.FlyingMoveControl) return true;
        
        // 4. Known flying mobs fallback (including Ender Dragon and Phantom)
        String name = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).toString();
        
        if (net.yigitguven.petting.config.PettingConfig.MANUAL_FLYING_MOBS.get().contains(name)) {
            return true;
        }

        return name.contains("ghast") || name.contains("bat") || name.contains("bee") || name.contains("parrot") || name.contains("vex") || 
               name.contains("ender_dragon") || name.contains("phantom");
    }
}
