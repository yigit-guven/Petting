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
    private final SimpleContainer dummyContainer = new SimpleContainer(1);

    public EquipmentSlotHandler(Mob mob, EquipmentSlot slot, int x, int y) {
        super(new SimpleContainer(1), 0, x, y);
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
    public ItemStack remove(int amount) {
        ItemStack current = mob.getItemBySlot(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = current.split(amount);
        mob.setItemSlot(slot, current);
        return taken;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        this.setChanged();
        super.onTake(player, stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
