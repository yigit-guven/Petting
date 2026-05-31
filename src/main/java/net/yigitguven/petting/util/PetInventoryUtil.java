package net.yigitguven.petting.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.attachment.PetInventoryAttachment;
import net.minecraft.core.registries.BuiltInRegistries;
import java.lang.reflect.Method;
import java.util.List;

public class PetInventoryUtil {
    private static Method CAN_EQUIP_STACK_METHOD;

    static {
        try {
            // In 1.21.1, we try to find the method for checking if a mob can equip a stack
            CAN_EQUIP_STACK_METHOD = ObfuscationReflectionHelper.findMethod(Mob.class, "canEquipStack", ItemStack.class);
            CAN_EQUIP_STACK_METHOD.setAccessible(true);
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    public static boolean isBlacklisted(Entity entity) {
        if (entity == null || !PettingConfig.BLACKLIST_ENABLED.get()) return false;
        return matchesBlacklistEntry(entity, PettingConfig.TAMING_BLACKLIST.get());
    }

    /**
     * Returns true if the entity's registry ID matches any entry in the given list.
     * Supported formats:
     *   "create:mechanical_arm"  — exact match
     *   "create" or "create:*"  — entire mod namespace
     *   "create:mechanical*"    — prefix wildcard
     */
    private static boolean matchesBlacklistEntry(Entity entity, List<? extends String> list) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String id = key.toString();
        String namespace = key.getNamespace();
        for (String entry : list) {
            if (entry == null || entry.isBlank()) continue;
            String e = entry.trim();
            if (e.equals(id)) return true;
            if (e.equals(namespace) || e.equals(namespace + ":*")) return true;
            if (e.endsWith("*") && id.startsWith(e.substring(0, e.length() - 1))) return true;
        }
        return false;
    }

    public static net.minecraft.world.item.Item getItemFromID(String id, net.minecraft.world.item.Item fallback) {
        if (id == null || id.isEmpty()) return fallback;
        try {
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            if (item != null && item != Items.AIR) {
                return item;
            }
        } catch (Exception ignored) {}
        return fallback;
    }

    public static boolean isSlotSupported(Entity entity, EquipmentSlot slotType) {
        if (!isInventoryAllowed(entity)) return false;
        if (slotType == null) return true; // Saddle
        
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String registryName = key.toString();
        
        if (net.yigitguven.petting.config.PettingConfig.EXTRA_EQUIPPABLE_MOBS.get().contains(registryName)) {
            return true;
        }

        if (net.yigitguven.petting.config.PettingConfig.ALWAYS_SHOW_EQUIPMENT_SLOTS.get() && entity.getPersistentData().getBoolean("pettingtamed")) {
            return true;
        }

        if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand) return true;
        
        if (entity instanceof Mob mob) {
            if (CAN_EQUIP_STACK_METHOD != null) {
                try {
                    ItemStack testStack;
                    if (slotType.isArmor()) {
                        testStack = new ItemStack(Items.IRON_CHESTPLATE);
                    } else {
                        testStack = new ItemStack(Items.IRON_SWORD);
                    }
                    return (boolean) CAN_EQUIP_STACK_METHOD.invoke(mob, testStack);
                } catch (Exception e) {
                    // Fallback
                }
            }
            
            if (mob instanceof net.minecraft.world.entity.monster.Zombie || 
                mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton ||
                mob instanceof net.minecraft.world.entity.monster.piglin.AbstractPiglin) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isInventoryAllowed(Entity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String name = key.toString();
        
        if (net.yigitguven.petting.config.PettingConfig.INVENTORY_BLACKLIST_ENABLED.get()) {
            if (matchesBlacklistEntry(entity, net.yigitguven.petting.config.PettingConfig.INVENTORY_BLACKLIST.get())) {
                return false;
            }
        }
        
        if (net.yigitguven.petting.config.PettingConfig.INVENTORY_WHITELIST_ONLY.get()) {
            return net.yigitguven.petting.config.PettingConfig.INVENTORY_WHITELIST.get().contains(name);
        }
        
        return true;
    }

    public static boolean isRidingAllowed(Entity entity) {
        if (!net.yigitguven.petting.config.PettingConfig.ALLOW_PET_RIDING.get()) return false;
        
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String name = key.toString();
        
        if (net.yigitguven.petting.config.PettingConfig.RIDING_BLACKLIST_ENABLED.get()) {
            if (matchesBlacklistEntry(entity, net.yigitguven.petting.config.PettingConfig.RIDING_BLACKLIST.get())) {
                return false;
            }
        }
        
        if (net.yigitguven.petting.config.PettingConfig.RIDING_WHITELIST_ONLY.get()) {
            return net.yigitguven.petting.config.PettingConfig.RIDING_WHITELIST.get().contains(name);
        }
        
        return true;
    }

    public static boolean hasSaddle(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        PetInventoryAttachment attachment = entity.getData(net.yigitguven.petting.init.PettingModAttachments.PET_INVENTORY);
        if (attachment != null) {
            ItemStack stack = attachment.getStackInSlot(0);
            return !stack.isEmpty() && (stack.is(Items.SADDLE) || stack.getItem().getDescriptionId().contains("saddle"));
        }
        return false;
    }

    public static boolean isFlyingMob(Entity entity) {
        if (!(entity instanceof Mob mob)) return false;
        if (mob instanceof net.minecraft.world.entity.animal.FlyingAnimal) return true;
        if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.FlyingPathNavigation) return true;
        if (mob.getMoveControl() instanceof net.minecraft.world.entity.ai.control.FlyingMoveControl) return true;
        
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String name = key.toString();
        
        if (net.yigitguven.petting.config.PettingConfig.MANUAL_FLYING_MOBS.get().contains(name)) {
            return true;
        }

        return name.contains("ghast") || name.contains("bat") || name.contains("bee") || name.contains("parrot") || name.contains("vex") || 
               name.contains("ender_dragon") || name.contains("phantom");
    }

    public static boolean isSwimmingMob(Entity entity) {
        if (!(entity instanceof Mob mob)) return false;
        if (mob.getNavigation() instanceof net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation) return true;
        if (mob instanceof net.minecraft.world.entity.animal.WaterAnimal) return true;
        
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        if (key == null) return false;
        String name = key.toString();
        
        if (net.yigitguven.petting.config.PettingConfig.MANUAL_SWIMMING_MOBS.get().contains(name)) {
            return true;
        }

        return name.contains("fish") || name.contains("squid") || name.contains("dolphin") || name.contains("guardian") || 
               name.contains("turtle") || name.contains("drowned") || name.contains("axolotl") || name.contains("glow_squid");
    }
}
