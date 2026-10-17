package net.yigitguven.petting.inventory;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Saddleable;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SaddleItem;
import net.minecraft.world.level.block.SkullBlock;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public enum PetSlotType {
    HEAD(EquipmentSlot.HEAD, net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_HELMET),
    CHEST(EquipmentSlot.CHEST, net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE),
    LEGS(EquipmentSlot.LEGS, net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS),
    FEET(EquipmentSlot.FEET, net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS),
    BODY(EquipmentSlot.BODY, ResourceLocation.fromNamespaceAndPath(net.yigitguven.petting.Petting.MODID, "item/empty_slot_horse_armor")),
    MAINHAND(EquipmentSlot.MAINHAND, ResourceLocation.withDefaultNamespace("item/empty_slot_sword")),
    OFFHAND(EquipmentSlot.OFFHAND, net.minecraft.world.inventory.InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD),
    SADDLE(null, ResourceLocation.fromNamespaceAndPath(net.yigitguven.petting.Petting.MODID, "item/empty_slot_saddle")),
    CHEST_STORAGE(null, null),
    COLLAR(null, ResourceLocation.fromNamespaceAndPath(net.yigitguven.petting.Petting.MODID, "item/empty_slot_collar"));

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
        if (this == BODY && pet != null) {
            if (pet instanceof Llama) {
                return ResourceLocation.fromNamespaceAndPath(net.yigitguven.petting.Petting.MODID, "item/empty_slot_llama_armor");
            }
        }
        return defaultIcon;
    }

    public boolean mayPlace(ItemStack stack, @Nullable LivingEntity pet) {
        if (stack.isEmpty()) return false;
        if (this == COLLAR) {
            return stack.getItem() instanceof net.yigitguven.petting.item.CollarItem;
        }
        if (this == CHEST_STORAGE) {
            return stack.is(Items.CHEST) || stack.is(Items.TRAPPED_CHEST) || stack.is(Items.BARREL);
        }
        if (this == MAINHAND || this == OFFHAND) {
            return true;
        }
        if (this == SADDLE) {
            return stack.is(Items.SADDLE) || stack.getItem() instanceof SaddleItem;
        }
        if (this == BODY) {
            if (pet instanceof Wolf) {
                return stack.getItem() instanceof AnimalArmorItem aai && aai.getBodyType() == AnimalArmorItem.BodyType.CANINE;
            }
            if (pet instanceof Llama) {
                return stack.is(ItemTags.WOOL_CARPETS);
            }
            if (pet instanceof AbstractHorse) {
                return (stack.getItem() instanceof AnimalArmorItem aai && aai.getBodyType() == AnimalArmorItem.BodyType.EQUESTRIAN)
                        || stack.is(Items.IRON_HORSE_ARMOR) || stack.is(Items.GOLDEN_HORSE_ARMOR) || stack.is(Items.DIAMOND_HORSE_ARMOR);
            }
            return stack.getItem() instanceof AnimalArmorItem || stack.is(ItemTags.WOOL_CARPETS);
        }
        if (this.slot != null) {
            if (this == HEAD) {
                if (stack.is(Items.CARVED_PUMPKIN) || (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock)) {
                    return true;
                }
                Equipable eq = Equipable.get(stack);
                return eq != null && eq.getEquipmentSlot() == EquipmentSlot.HEAD;
            }
            if (this == CHEST) {
                if (stack.is(Items.ELYTRA)) {
                    return true;
                }
                Equipable eq = Equipable.get(stack);
                return eq != null && eq.getEquipmentSlot() == EquipmentSlot.CHEST;
            }
            if (this == LEGS) {
                Equipable eq = Equipable.get(stack);
                return eq != null && eq.getEquipmentSlot() == EquipmentSlot.LEGS;
            }
            if (this == FEET) {
                Equipable eq = Equipable.get(stack);
                return eq != null && eq.getEquipmentSlot() == EquipmentSlot.FEET;
            }
        }
        return false;
    }

    public static List<PetSlotType> getLeftSlots(@Nullable LivingEntity pet) {
        List<PetSlotType> slots = new ArrayList<>();
        if (pet == null || !net.yigitguven.petting.config.PettingServerConfig.isPetInventoryEnabled()) return slots;

        if (canEquipHumanoidArmor(pet)) {
            if (pet.canUseSlot(EquipmentSlot.HEAD)) slots.add(HEAD);
            if (pet.canUseSlot(EquipmentSlot.CHEST)) slots.add(CHEST);
            if (pet.canUseSlot(EquipmentSlot.LEGS)) slots.add(LEGS);
            if (pet.canUseSlot(EquipmentSlot.FEET)) slots.add(FEET);
        } else if (canEquipBodyArmor(pet) && pet.canUseSlot(EquipmentSlot.BODY)) {
            slots.add(BODY);
        }
        return slots;
    }

    public static boolean isDefaultSaddleable(@Nullable LivingEntity mob) {
        return mob instanceof AbstractHorse;
    }

    public static boolean canEquipCustomSaddle(@Nullable LivingEntity mob) {
        return mob != null
                && net.yigitguven.petting.util.PetHelper.isPetRidingEnabledFor(mob)
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
            if (pet.canUseSlot(EquipmentSlot.MAINHAND)) slots.add(MAINHAND);
            if (canUseOffHand(pet) && pet.canUseSlot(EquipmentSlot.OFFHAND)) slots.add(OFFHAND);
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
        Holder<EntityType<?>> typeHolder = mob.getType().builtInRegistryHolder();
        if (typeHolder.is(EntityTypeTags.ZOMBIES) ||
                typeHolder.is(EntityTypeTags.SKELETONS) ||
                typeHolder.is(EntityTypeTags.RAIDERS) ||
                typeHolder.is(EntityTypeTags.ILLAGER)) {
            return true;
        }
        if (mob instanceof AbstractPiglin ||
                mob instanceof Zombie ||
                mob instanceof AbstractSkeleton) {
            return true;
        }
        for (TagKey<EntityType<?>> tag : typeHolder.tags().toList()) {
            String path = tag.location().getPath();
            if (path.contains("humanoid") || path.contains("biped") || path.contains("zombie") || path.contains("skeleton")) {
                return true;
            }
        }
        String className = mob.getClass().getSimpleName().toLowerCase();
        return className.contains("humanoid") || className.contains("biped") ||
                className.contains("zombie") || className.contains("skeleton") ||
                className.contains("knight") || className.contains("guard") ||
                className.contains("goblin") || className.contains("orc") ||
                className.contains("npc");
    }

    public static boolean canEquipBodyArmor(LivingEntity mob) {
        if (!mob.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return true;
        }
        if (mob instanceof Wolf || mob instanceof Llama || mob instanceof AbstractHorse) {
            return true;
        }
        return mob.canUseSlot(EquipmentSlot.BODY);
    }

    public static boolean canEquipSaddle(LivingEntity mob) {
        if (mob instanceof AbstractHorse || mob instanceof Saddleable) {
            return true;
        }
        return canEquipCustomSaddle(mob);
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
        if (mob instanceof AbstractHorse ||
                mob instanceof Animal) {
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
