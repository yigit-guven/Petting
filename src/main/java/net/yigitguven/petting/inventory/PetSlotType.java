package net.yigitguven.petting.inventory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SkullBlock;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.util.PetHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum PetSlotType {
    HEAD(EquipmentSlot.HEAD, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET),
    CHEST(EquipmentSlot.CHEST, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE),
    LEGS(EquipmentSlot.LEGS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS),
    FEET(EquipmentSlot.FEET, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS),
    BODY(null, null),
    MAINHAND(EquipmentSlot.MAINHAND, null),
    OFFHAND(EquipmentSlot.OFFHAND, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD),
    SADDLE(null, new ResourceLocation(Petting.MODID, "item/empty_slot_saddle")),
    CHEST_STORAGE(null, null),
    COLLAR(null, new ResourceLocation(Petting.MODID, "item/empty_slot_collar"));

    public static final ResourceLocation EMPTY_SLOT_SADDLE = new ResourceLocation(Petting.MODID, "item/empty_slot_saddle");
    public static final ResourceLocation EMPTY_SLOT_COLLAR = new ResourceLocation(Petting.MODID, "item/empty_slot_collar");

    private final @Nullable EquipmentSlot slot;
    private final @Nullable ResourceLocation defaultIcon;

    PetSlotType(@Nullable EquipmentSlot slot, @Nullable ResourceLocation defaultIcon) {
        this.slot = slot;
        this.defaultIcon = defaultIcon;
    }

    public @Nullable EquipmentSlot getEquipmentSlot() {
        return slot;
    }

    public @Nullable ResourceLocation getIcon(@Nullable LivingEntity pet) {
        return defaultIcon;
    }

    public boolean mayPlace(ItemStack stack, @Nullable LivingEntity pet) {
        if (stack.isEmpty()) return false;
        if (this == COLLAR) {
            return stack.getItem() instanceof CollarItem;
        }
        if (this == CHEST_STORAGE) {
            return stack.is(Items.CHEST) || stack.is(Items.TRAPPED_CHEST) || stack.is(Items.BARREL);
        }
        if (this == MAINHAND || this == OFFHAND) {
            return true;
        }
        if (this == SADDLE) {
            return stack.is(Items.SADDLE);
        }
        if (this == BODY) {
            return stack.getItem() instanceof HorseArmorItem;
        }
        if (this.slot != null) {
            if (this.slot == EquipmentSlot.HEAD) {
                if (stack.is(Items.CARVED_PUMPKIN) || (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock)) {
                    return true;
                }
            }
            if (this.slot == EquipmentSlot.CHEST && stack.is(Items.ELYTRA)) {
                return true;
            }
            if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == this.slot) {
                return true;
            }
            if (Mob.getEquipmentSlotForItem(stack) == this.slot) {
                return true;
            }
        }
        return false;
    }

    public static List<PetSlotType> getLeftSlots(@Nullable LivingEntity pet) {
        List<PetSlotType> slots = new ArrayList<>();
        if (pet == null || !net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()) return slots;

        if (canEquipHumanoidArmor(pet)) {
            if (canUseSlot(pet, EquipmentSlot.HEAD)) slots.add(HEAD);
            if (canUseSlot(pet, EquipmentSlot.CHEST)) slots.add(CHEST);
            if (canUseSlot(pet, EquipmentSlot.LEGS)) slots.add(LEGS);
            if (canUseSlot(pet, EquipmentSlot.FEET)) slots.add(FEET);
        } else if (canEquipBodyArmor(pet)) {
            slots.add(BODY);
        }
        return slots;
    }

    private static boolean canUseSlot(LivingEntity pet, EquipmentSlot slot) {
        if (pet instanceof Mob mob) {
            return mob.canTakeItem(new ItemStack(Items.IRON_HELMET)) || true;
        }
        return true;
    }

    public static boolean isDefaultSaddleable(@Nullable LivingEntity mob) {
        return mob instanceof AbstractHorse;
    }

    public static boolean canEquipCustomSaddle(@Nullable LivingEntity mob) {
        return mob != null
                && PetHelper.isPetRidingEnabledFor(mob)
                && !isDefaultSaddleable(mob);
    }

    public static List<PetSlotType> getRightSlots(@Nullable LivingEntity pet) {
        List<PetSlotType> slots = new ArrayList<>();
        if (pet == null || !net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()) return slots;

        if (net.yigitguven.petting.config.PettingServerConfig.isCollarSlotEnabled()) {
            slots.add(COLLAR);
        }

        if (canEquipCustomSaddle(pet)) {
            slots.add(SADDLE);
        }

        if (canUseHands(pet)) {
            slots.add(MAINHAND);
            if (canUseOffHand(pet)) {
                slots.add(OFFHAND);
            }
        }

        if (canEquipChest(pet)) {
            slots.add(CHEST_STORAGE);
        }

        return slots;
    }

    public static boolean canEquipHumanoidArmor(LivingEntity mob) {
        if (!mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() ||
                !mob.getItemBySlot(EquipmentSlot.CHEST).isEmpty() ||
                !mob.getItemBySlot(EquipmentSlot.LEGS).isEmpty() ||
                !mob.getItemBySlot(EquipmentSlot.FEET).isEmpty()) {
            return true;
        }
        if (mob instanceof AbstractPiglin ||
                mob instanceof Zombie ||
                mob instanceof AbstractSkeleton) {
            return true;
        }
        String className = mob.getClass().getSimpleName().toLowerCase();
        return className.contains("humanoid") || className.contains("biped") ||
                className.contains("zombie") || className.contains("skeleton") ||
                className.contains("knight") || className.contains("guard") ||
                className.contains("goblin") || className.contains("orc") ||
                className.contains("npc") || className.contains("pillager") ||
                className.contains("vindicator");
    }

    public static boolean canEquipBodyArmor(LivingEntity mob) {
        if (mob instanceof AbstractHorse) {
            return !(mob instanceof Llama);
        }
        return false;
    }

    public static boolean canEquipSaddle(LivingEntity mob) {
        return mob instanceof AbstractHorse;
    }

    public static boolean canEquipChest(LivingEntity mob) {
        return mob instanceof AbstractChestedHorse;
    }

    public static boolean canUseHands(LivingEntity mob) {
        if (!mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !mob.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
            return true;
        }
        if (canEquipHumanoidArmor(mob)) {
            return true;
        }
        if (mob instanceof AbstractHorse || mob instanceof Animal) {
            return mob.getType() == EntityType.FOX;
        }
        EntityType<?> type = mob.getType();
        return type == EntityType.FOX || type == EntityType.ALLAY ||
                type == EntityType.VEX || type == EntityType.ENDERMAN;
    }

    public static boolean canUseOffHand(LivingEntity mob) {
        EntityType<?> type = mob.getType();
        if (type == EntityType.FOX || type == EntityType.ALLAY || type == EntityType.ENDERMAN) {
            return false;
        }
        return canUseHands(mob);
    }
}
