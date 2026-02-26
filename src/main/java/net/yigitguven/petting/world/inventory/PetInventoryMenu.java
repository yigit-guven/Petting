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
    private static final int STORAGE_COUNT = 16; 
    private static final int PLAYER_START = 23;

    public PetInventoryMenu(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(id, playerInventory, playerInventory.player.level().getEntity(extraData.readInt()));
    }

    public PetInventoryMenu(int id, Inventory playerInventory, Entity pet) {
        super(PettingModMenus.PET_INVENTORY.get(), id);
        this.pet = pet;
        
        if (pet != null) {
            this.petCapabilityInventory = pet.getCapability(ForgeCapabilities.ITEM_HANDLER)
                    .orElse(new ItemStackHandler(40));
        } else {
            this.petCapabilityInventory = new ItemStackHandler(40);
        }

        // 1. Equipment Column (Further Right)
        this.addSlot(new SlotItemHandler(petCapabilityInventory, 10, 80, 17) { // Saddle - Unbinded to index 10
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
            // 2. Armor Grid (2x2 on Left)
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.HEAD, 8, 17));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.CHEST, 26, 17));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.LEGS, 8, 35));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.FEET, 26, 35));
            
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.MAINHAND, 80, 35));
            this.addSlot(new EquipmentSlotHandler(mob, EquipmentSlot.OFFHAND, 80, 53));
        } else {
            for (int i = 0; i < 6; i++) {
                this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0, -1000, -1000) {
                    @Override
                    public boolean mayPlace(ItemStack stack) { return false; }
                    @Override
                    public boolean mayPickup(Player player) { return false; }
                });
            }
        }

        // 3. Storage Slots (Capability Slots 11-26 -> Menu Slots 7-22)
        int capabilitySize = petCapabilityInventory.getSlots();
        for (int i = 0; i < STORAGE_COUNT; i++) {
            int slotIdx = i + 11; // Offset to index 11+
            int row = i / 4;
            int col = i % 4;
            if (slotIdx < capabilitySize) {
                this.addSlot(new SlotItemHandler(petCapabilityInventory, slotIdx, 108 + col * 18, 18 + row * 18));
            } else {
                this.addSlot(new Slot(new net.minecraft.world.SimpleContainer(1), 0, -1000, -1000) {
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
                // 1. Try Saddle (Menu Slot 0 -> Capability Slot 10)
                if (itemstack1.is(Items.SADDLE)) {
                    if (this.moveItemStackTo(itemstack1, SADDLE_SLOT, SADDLE_SLOT + 1, false)) {
                        // Success
                    } else if (this.moveItemStackTo(itemstack1, STORAGE_START, STORAGE_START + STORAGE_COUNT, false)) {
                        // Fallback to storage
                    } else {
                        return ItemStack.EMPTY;
                    }
                } 
                // 2. Try Armor (Menu Slots 1-4)
                else if (itemstack1.getItem() instanceof net.minecraft.world.item.ArmorItem armor) {
                    int armorSlot = -1;
                    EquipmentSlot type = armor.getType().getSlot();
                    if (type == EquipmentSlot.HEAD) armorSlot = 1;
                    else if (type == EquipmentSlot.CHEST) armorSlot = 2;
                    else if (type == EquipmentSlot.LEGS) armorSlot = 3;
                    else if (type == EquipmentSlot.FEET) armorSlot = 4;
                    
                    if (armorSlot != -1 && !this.moveItemStackTo(itemstack1, armorSlot, armorSlot + 1, false)) {
                        // Fall back to storage if armor slot full
                        if (!this.moveItemStackTo(itemstack1, STORAGE_START, STORAGE_START + STORAGE_COUNT, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
                // 3. Try Storage (Menu Slots 7-22)
                else {
                    if (!this.moveItemStackTo(itemstack1, STORAGE_START, STORAGE_START + STORAGE_COUNT, false)) {
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
