package net.yigitguven.petting.inventory;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SkullBlock;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum PetSlotType {
    HEAD(EquipmentSlot.HEAD, Identifier.withDefaultNamespace("container/slot/helmet")),
    CHEST(EquipmentSlot.CHEST, Identifier.withDefaultNamespace("container/slot/chestplate")),
    LEGS(EquipmentSlot.LEGS, Identifier.withDefaultNamespace("container/slot/leggings")),
    FEET(EquipmentSlot.FEET, Identifier.withDefaultNamespace("container/slot/boots")),
    BODY(EquipmentSlot.BODY, Identifier.withDefaultNamespace("container/slot/horse_armor")),
    MAINHAND(EquipmentSlot.MAINHAND, Identifier.withDefaultNamespace("container/slot/sword")),
    OFFHAND(EquipmentSlot.OFFHAND, Identifier.withDefaultNamespace("container/slot/shield")),
    SADDLE(EquipmentSlot.SADDLE, Identifier.withDefaultNamespace("container/slot/saddle")),
    CHEST_STORAGE(null, null);

    private final @Nullable EquipmentSlot slot;
    private final @Nullable Identifier defaultIcon;

    PetSlotType(@Nullable EquipmentSlot slot, @Nullable Identifier defaultIcon) {
        this.slot = slot;
        this.defaultIcon = defaultIcon;
    }

    public @Nullable EquipmentSlot getEquipmentSlot() {
        return slot;
    }

    public @Nullable Identifier getIcon(@Nullable Mob pet) {
        if (this == BODY && pet != null) {
            if (pet instanceof Llama) {
                return Identifier.withDefaultNamespace("container/slot/llama_armor");
            }
            if (pet.getType().builtInRegistryHolder().is(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR)) {
                return Identifier.withDefaultNamespace("container/slot/nautilus_armor");
            }
        }
        return defaultIcon;
    }

    public boolean mayPlace(ItemStack stack, @Nullable Mob pet) {
        if (stack.isEmpty()) return false;
        if (this == CHEST_STORAGE) {
            return stack.is(Items.CHEST) || stack.is(Items.TRAPPED_CHEST) || stack.is(Items.BARREL);
        }
        if (this == MAINHAND || this == OFFHAND) {
            return true;
        }
        if (this == SADDLE) {
            return stack.is(Items.SADDLE) ||
                    (stack.get(DataComponents.EQUIPPABLE) != null && stack.get(DataComponents.EQUIPPABLE).slot() == EquipmentSlot.SADDLE);
        }
        if (this.slot != null) {
            Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot() == this.slot) {
                if (pet != null) {
                    return equippable.canBeEquippedBy(pet.getType().builtInRegistryHolder());
                }
                return true;
            }
            if (this == HEAD && (stack.is(Items.CARVED_PUMPKIN) || (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock))) {
                return true;
            }
            if (this == CHEST && stack.is(Items.ELYTRA)) {
                return true;
            }
            if (pet != null && pet.isEquippableInSlot(stack, this.slot)) {
                return true;
            }
        }
        return false;
    }

    public static List<PetSlotType> getLeftSlots(@Nullable Mob pet) {
        List<PetSlotType> slots = new ArrayList<>();
        if (pet == null) return slots;

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

    public static List<PetSlotType> getRightSlots(@Nullable Mob pet) {
        List<PetSlotType> slots = new ArrayList<>();
        if (pet == null) return slots;

        if (canUseHands(pet)) {
            if (pet.canUseSlot(EquipmentSlot.MAINHAND)) slots.add(MAINHAND);
            if (canUseOffHand(pet) && pet.canUseSlot(EquipmentSlot.OFFHAND)) slots.add(OFFHAND);
        }

        if (canEquipSaddle(pet)) {
            slots.add(SADDLE);
        }

        if (canEquipChest(pet)) {
            slots.add(CHEST_STORAGE);
        }

        return slots;
    }

    public static boolean canEquipHumanoidArmor(Mob mob) {
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

    public static boolean canEquipBodyArmor(Mob mob) {
        if (!mob.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return true;
        }
        Holder<EntityType<?>> typeHolder = mob.getType().builtInRegistryHolder();
        if (typeHolder.is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) ||
                typeHolder.is(EntityTypeTags.CAN_WEAR_NAUTILUS_ARMOR) ||
                mob instanceof Wolf ||
                mob instanceof Llama) {
            return true;
        }
        for (Item item : List.of(Items.DIAMOND_HORSE_ARMOR, Items.WOLF_ARMOR)) {
            Equippable eq = item.components().get(DataComponents.EQUIPPABLE);
            if (eq != null && eq.canBeEquippedBy(typeHolder)) {
                return true;
            }
        }
        return false;
    }

    public static boolean canEquipSaddle(Mob mob) {
        if (!mob.getItemBySlot(EquipmentSlot.SADDLE).isEmpty()) {
            return true;
        }
        if (mob.getType().builtInRegistryHolder().is(EntityTypeTags.CAN_EQUIP_SADDLE) || mob instanceof AbstractHorse) {
            return true;
        }
        Equippable eq = Items.SADDLE.components().get(DataComponents.EQUIPPABLE);
        if (eq != null && eq.canBeEquippedBy(mob.getType().builtInRegistryHolder())) {
            return true;
        }
        return false;
    }

    public static boolean canEquipChest(Mob mob) {
        return mob instanceof AbstractChestedHorse;
    }

    public static boolean canUseHands(Mob mob) {
        if (!mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !mob.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
            return true;
        }
        if (canEquipHumanoidArmor(mob)) {
            return true;
        }
        if (mob.getType().builtInRegistryHolder().is(EntityTypeTags.CAN_EQUIP_SADDLE) ||
                mob instanceof AbstractHorse ||
                mob instanceof Animal) {
            return mob.getType() == EntityTypes.FOX;
        }
        EntityType<?> type = mob.getType();
        return type == EntityTypes.FOX || type == EntityTypes.ALLAY ||
                type == EntityTypes.VEX || type == EntityTypes.ENDERMAN;
    }

    public static boolean canUseOffHand(Mob mob) {
        EntityType<?> type = mob.getType();
        if (type == EntityTypes.FOX || type == EntityTypes.ALLAY || type == EntityTypes.ENDERMAN) {
            return false;
        }
        return canUseHands(mob);
    }
}
