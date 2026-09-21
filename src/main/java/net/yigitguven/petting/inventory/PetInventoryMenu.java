package net.yigitguven.petting.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.SkullBlock;
import net.yigitguven.petting.init.PettingModMenus;
import net.yigitguven.petting.util.PetHelper;
import org.jspecify.annotations.Nullable;

public class PetInventoryMenu extends AbstractContainerMenu {
    public static final Identifier EMPTY_SLOT_SWORD = Identifier.withDefaultNamespace("container/slot/sword");

    private final Mob pet;
    private final Player player;
    private final Container petContainer;

    public PetInventoryMenu(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        this(windowId, playerInv, getPetFromBuf(playerInv.player, extraData));
    }

    private static @Nullable Mob getPetFromBuf(Player player, RegistryFriendlyByteBuf extraData) {
        int entityId = extraData.readVarInt();
        Entity entity = player.level().getEntity(entityId);
        return entity instanceof Mob mob ? mob : null;
    }

    public PetInventoryMenu(int windowId, Inventory playerInv, @Nullable Mob pet) {
        super(PettingModMenus.PET_MENU.get(), windowId);
        this.pet = pet;
        this.player = playerInv.player;
        this.petContainer = new PetEquipmentContainer(pet);

        if (pet != null) {
            for (EquipmentSlot slot : PetEquipmentContainer.SLOTS) {
                pet.setDropChance(slot, 1.0F);
            }
        }

        // Pet armor slots (left column: x = 8)
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.HEAD, 0, 8, 18, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET));
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.CHEST, 1, 8, 36, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE));
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.LEGS, 2, 8, 54, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS));
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.FEET, 3, 8, 72, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS));

        // Pet hand slots (right column: x = 152)
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.MAINHAND, 4, 152, 36, EMPTY_SLOT_SWORD));
        this.addSlot(new PetSlot(this.petContainer, pet, EquipmentSlot.OFFHAND, 5, 152, 54, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD));

        // Player standard inventory (27 slots: 3 rows of 9 at x = 8, y = 103)
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18));
            }
        }

        // Player hotbar (9 slots at x = 8, y = 161)
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 161));
        }
    }

    public static void open(ServerPlayer player, Mob pet) {
        player.openMenu(new SimpleMenuProvider(
                (windowId, playerInv, p) -> new PetInventoryMenu(windowId, playerInv, pet),
                pet.getDisplayName()
        ), buf -> buf.writeVarInt(pet.getId()));
    }

    public @Nullable Mob getPet() {
        return this.pet;
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.pet == null || !this.pet.isAlive()) {
            return false;
        }
        return player.distanceTo(this.pet) <= 8.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (slotIndex < 6) {
                // From pet slot to player inventory
                if (!this.moveItemStackTo(slotStack, 6, 42, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to pet slots
                boolean moved = false;
                if (isMatchingEquipment(slotStack, EquipmentSlot.HEAD, this.pet)) {
                    moved = this.moveItemStackTo(slotStack, 0, 1, false);
                } else if (isMatchingEquipment(slotStack, EquipmentSlot.CHEST, this.pet)) {
                    moved = this.moveItemStackTo(slotStack, 1, 2, false);
                } else if (isMatchingEquipment(slotStack, EquipmentSlot.LEGS, this.pet)) {
                    moved = this.moveItemStackTo(slotStack, 2, 3, false);
                } else if (isMatchingEquipment(slotStack, EquipmentSlot.FEET, this.pet)) {
                    moved = this.moveItemStackTo(slotStack, 3, 4, false);
                }

                if (!moved) {
                    // Try mainhand, then offhand
                    if (!this.slots.get(4).hasItem()) {
                        moved = this.moveItemStackTo(slotStack, 4, 5, false);
                    } else if (!this.slots.get(5).hasItem()) {
                        moved = this.moveItemStackTo(slotStack, 5, 6, false);
                    }
                }

                if (!moved) {
                    if (slotIndex >= 6 && slotIndex < 33) {
                        if (!this.moveItemStackTo(slotStack, 33, 42, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slotIndex >= 33 && slotIndex < 42) {
                        if (!this.moveItemStackTo(slotStack, 6, 33, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }
        return result;
    }

    public static boolean isMatchingEquipment(ItemStack stack, EquipmentSlot slot, @Nullable Mob mob) {
        if (stack.isEmpty()) return false;
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return true;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.slot() == slot) {
            return true;
        }
        if (mob != null) {
            EquipmentSlot itemSlot = mob.getEquipmentSlotForItem(stack);
            if (itemSlot == slot) return true;
        }
        if (slot == EquipmentSlot.HEAD && (stack.is(Items.CARVED_PUMPKIN) || (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof SkullBlock))) {
            return true;
        }
        if (slot == EquipmentSlot.CHEST && stack.is(Items.ELYTRA)) {
            return true;
        }
        return false;
    }

    private static class PetSlot extends Slot {
        private final Mob pet;
        private final EquipmentSlot slot;
        private final @Nullable Identifier emptyIcon;

        public PetSlot(Container container, @Nullable Mob pet, EquipmentSlot slot, int index, int x, int y, @Nullable Identifier emptyIcon) {
            super(container, index, x, y);
            this.pet = pet;
            this.slot = slot;
            this.emptyIcon = emptyIcon;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return isMatchingEquipment(stack, this.slot, this.pet);
        }

        @Override
        public int getMaxStackSize() {
            return (this.slot == EquipmentSlot.MAINHAND || this.slot == EquipmentSlot.OFFHAND) ? 64 : 1;
        }

        @Override
        public @Nullable Identifier getNoItemIcon() {
            return this.emptyIcon;
        }
    }

    private static class PetEquipmentContainer implements Container {
        public static final EquipmentSlot[] SLOTS = new EquipmentSlot[]{
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET,
                EquipmentSlot.MAINHAND,
                EquipmentSlot.OFFHAND
        };

        private final @Nullable Mob pet;

        public PetEquipmentContainer(@Nullable Mob pet) {
            this.pet = pet;
        }

        @Override
        public int getContainerSize() {
            return SLOTS.length;
        }

        @Override
        public boolean isEmpty() {
            if (this.pet == null) return true;
            for (EquipmentSlot slot : SLOTS) {
                if (!this.pet.getItemBySlot(slot).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slotIndex) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= SLOTS.length) {
                return ItemStack.EMPTY;
            }
            return this.pet.getItemBySlot(SLOTS[slotIndex]);
        }

        @Override
        public ItemStack removeItem(int slotIndex, int amount) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= SLOTS.length) {
                return ItemStack.EMPTY;
            }
            EquipmentSlot slot = SLOTS[slotIndex];
            ItemStack current = this.pet.getItemBySlot(slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack split = current.split(amount);
            this.pet.setItemSlot(slot, current.isEmpty() ? ItemStack.EMPTY : current);
            this.setChanged();
            return split;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slotIndex) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= SLOTS.length) {
                return ItemStack.EMPTY;
            }
            EquipmentSlot slot = SLOTS[slotIndex];
            ItemStack current = this.pet.getItemBySlot(slot);
            if (current.isEmpty()) {
                return ItemStack.EMPTY;
            }
            this.pet.setItemSlot(slot, ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int slotIndex, ItemStack stack) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= SLOTS.length) {
                return;
            }
            EquipmentSlot slot = SLOTS[slotIndex];
            this.pet.setItemSlot(slot, stack);
            this.setChanged();
        }

        @Override
        public void setChanged() {
            if (this.pet != null) {
                for (EquipmentSlot slot : SLOTS) {
                    this.pet.setDropChance(slot, 1.0F);
                }
            }
        }

        @Override
        public boolean stillValid(Player player) {
            return this.pet != null && this.pet.isAlive() && player.distanceTo(this.pet) <= 8.0D;
        }

        @Override
        public void clearContent() {
            if (this.pet != null) {
                for (EquipmentSlot slot : SLOTS) {
                    this.pet.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }
}
