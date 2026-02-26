package net.yigitguven.petting.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.yigitguven.petting.init.PettingModMenus;
public class PetInventoryMenu extends AbstractContainerMenu {
    private final Entity pet;
    private final IItemHandler petCapabilityInventory;

    private static final int SADDLE_SLOT = 0;
    private static final int EQUIPMENT_START = 1;
    private static final int EQUIPMENT_COUNT = 6;
    private static final int STORAGE_START = 7;
    private static final int STORAGE_COUNT = 15; // Capability has 16 slots, index 0 is saddle
    private static final int PLAYER_START = 22;

    private static final net.minecraft.world.SimpleContainer DUMMY_CONTAINER = new net.minecraft.world.SimpleContainer(6);

    public PetInventoryMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, playerInventory.player.level().getEntity(extraData.readInt()));
    }

    public PetInventoryMenu(int id, Inventory playerInventory, Entity pet) {
        super(PettingModMenus.PET_INVENTORY.get(), id);
        this.pet = pet;
        
        if (pet != null) {
            this.petCapabilityInventory = pet.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .orElse(new ItemStackHandler(1));
        } else {
            this.petCapabilityInventory = new ItemStackHandler(1);
        }

        // 1. Saddle Slot (Capability Slot 0) - x=8, y=18
        this.addSlot(new SlotItemHandler(petCapabilityInventory, 0, 8, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.SADDLE);
            }
            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // 2. Equipment Slots (Slots 1-6) - Arranged around portrait
        if (pet instanceof Mob mob) {
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.HEAD, 8, 36));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.CHEST, 8, 54));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.LEGS, 61, 18));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.FEET, 61, 36));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.MAINHAND, 61, 54));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.OFFHAND, 34, 54));
        } else {
            for (int i = 0; i < 6; i++) {
                this.addSlot(new Slot(DUMMY_CONTAINER, i, -1000, -1000) {
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
                    @Override
                    public boolean mayPickup(Player player) { return false; }
                });
            }
        }

        // 3. Storage Slots (Capability Slots 1-15 -> Menu Slots 7-21)
        int capabilitySize = petCapabilityInventory.getSlots();
        for (int i = 0; i < STORAGE_COUNT; i++) {
            int slotIdx = i + 1;
            int row = i / 5;
            int col = i % 5;
            if (slotIdx < capabilitySize) {
                this.addSlot(new SlotItemHandler(petCapabilityInventory, slotIdx, 80 + col * 18, 18 + row * 18));
            } else {
                this.addSlot(new Slot(DUMMY_CONTAINER, i, -1000, -1000) {
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
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

    public IItemHandler getPetInventory() {
        return petCapabilityInventory;
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
            if (index < PLAYER_START) { // From Pet to Player
                if (!this.moveItemStackTo(itemstack1, PLAYER_START, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else { // From Player to Pet
                if (itemstack1.is(Items.SADDLE)) {
                    if (!this.moveItemStackTo(itemstack1, SADDLE_SLOT, SADDLE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    // Try storage first
                    if (!this.moveItemStackTo(itemstack1, STORAGE_START, STORAGE_START + STORAGE_COUNT, false)) {
                        // Then equipment if applicable (too complex for simple quickMove, usually only for specific items)
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

    // Custom Slot for Entity Equipment
    private static class EquipmentSlotHandler extends Slot {
        private final Mob mob;
        private final EquipmentSlot slot;

        public EquipmentSlotHandler(Mob mob, EquipmentSlot slot, int x, int y) {
            super(DUMMY_CONTAINER, slot.getIndex(), x, y); // Use dummy container instead of null!
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
            if (!(mob instanceof Mob)) return false;
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                return stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor && armor.getType().getSlot() == slot;
            }
            return true; // Hands can hold anything
        }

        @Override
        public boolean mayPickup(Player player) {
            return true;
        }

        @Override
        public void setChanged() {
            // Entity equipment usually updates immediately
        }
        
        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
