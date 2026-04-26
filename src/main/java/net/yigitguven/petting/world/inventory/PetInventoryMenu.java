package net.yigitguven.petting.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.attachment.PetInventoryAttachment;
import net.yigitguven.petting.util.PetInventoryUtil;

public class PetInventoryMenu extends AbstractContainerMenu {
    private final Entity pet;
    private final IItemHandler petInventory;

    private static final int SADDLE_SLOT = 0;
    private static final int PLAYER_START = 7;

    public PetInventoryMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, playerInventory.player.level().getEntity(extraData.readInt()));
    }

    public PetInventoryMenu(int id, Inventory playerInventory, Entity pet) {
        super(PettingModMenus.PET_INVENTORY.get(), id);
        this.pet = pet;
        
        if (pet != null) {
            this.petInventory = pet.getData(net.yigitguven.petting.init.PettingModAttachments.PET_INVENTORY);
        } else {
            this.petInventory = new net.neoforged.neoforge.items.ItemStackHandler(7);
        }

        // 1. Saddle Slot
        this.addSlot(new SlotItemHandler(petInventory, 0, 142, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.SADDLE);
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        if (pet instanceof Mob mob) {
            // 2. Armor Slots
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.HEAD, 17, 17, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.HEAD)));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.CHEST, 35, 17, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.CHEST)));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.LEGS, 17, 35, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.LEGS)));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.FEET, 35, 35, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.FEET)));
            
            // 3. Hand Slots
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.MAINHAND, 142, 35, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.MAINHAND)));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.OFFHAND, 142, 53, PetInventoryUtil.isSlotSupported(mob, EquipmentSlot.OFFHAND)));
        } else {
            // Dummy slots if not a mob
            for (int i = 0; i < 6; i++) {
                this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0, -1000, -1000) {
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
                    @Override
                    public boolean mayPickup(Player player) { return false; }
                });
            }
        }

        // Player Inventory
        int playerInvY = 84;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, playerInvY + 58));
        }
    }

    public Entity getPet() {
        return pet;
    }

    @Override
    public boolean stillValid(Player player) {
        return pet != null && pet.isAlive() && pet.distanceTo(player) < 8.0F;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            
            if (index < PLAYER_START) {
                if (!this.moveItemStackTo(itemstack1, PLAYER_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (itemstack1.is(Items.SADDLE)) {
                    if (!this.moveItemStackTo(itemstack1, SADDLE_SLOT, SADDLE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    net.minecraft.world.item.equipment.Equippable equippable = itemstack1.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
                    if (equippable != null && equippable.slot().isArmor()) {
                        int armorSlot = -1;
                        EquipmentSlot type = equippable.slot();
                        if (type == EquipmentSlot.HEAD) armorSlot = 1;
                        else if (type == EquipmentSlot.CHEST) armorSlot = 2;
                        else if (type == EquipmentSlot.LEGS) armorSlot = 3;
                        else if (type == EquipmentSlot.FEET) armorSlot = 4;
                        
                        if (armorSlot == -1 || !this.moveItemStackTo(itemstack1, armorSlot, armorSlot + 1, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}
