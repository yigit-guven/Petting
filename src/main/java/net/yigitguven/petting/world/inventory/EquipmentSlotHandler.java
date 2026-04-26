package net.yigitguven.petting.world.inventory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

public class EquipmentSlotHandler extends Slot {
    private final Mob mob;
    private final EquipmentSlot slot;
    private final boolean active;

    public EquipmentSlotHandler(Mob mob, EquipmentSlot slot, int x, int y, boolean active) {
        super(new SimpleContainer(1), 0, x, y);
        this.mob = mob;
        this.slot = slot;
        this.active = active;
    }

    @Override
    public ItemStack getItem() {
        return mob.getItemBySlot(slot);
    }

    @Override
    public void set(ItemStack stack) {
        mob.setItemSlot(slot, stack);
        this.setChanged();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!active) return false;
        if (slot.isArmor()) {
            net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
            return equippable != null && equippable.slot() == slot;
        }
        return true; 
    }

    @Override
    public boolean mayPickup(Player player) {
        return active;
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack current = mob.getItemBySlot(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = current.split(amount);
        mob.setItemSlot(slot, current);
        return taken;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
