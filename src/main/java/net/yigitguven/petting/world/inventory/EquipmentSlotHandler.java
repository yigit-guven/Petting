package net.yigitguven.petting.world.inventory;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

public class EquipmentSlotHandler extends Slot {
    private final Mob mob;
    private final EquipmentSlot slot;
    private static final SimpleContainer DUMMY_CONTAINER = new SimpleContainer(6);

    public EquipmentSlotHandler(Mob mob, EquipmentSlot slot, int x, int y) {
        super(DUMMY_CONTAINER, slot.getIndex(), x, y);
        this.mob = mob;
        this.slot = slot;
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
        if (slot.getType() == EquipmentSlot.Type.ARMOR) {
            return stack.getItem() instanceof ArmorItem armor && armor.getType().getSlot() == slot;
        }
        return true; // Hands can hold anything
    }

    @Override
    public boolean mayPickup(Player player) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
