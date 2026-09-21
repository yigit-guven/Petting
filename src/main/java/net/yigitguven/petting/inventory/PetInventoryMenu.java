package net.yigitguven.petting.inventory;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.yigitguven.petting.init.PettingModMenus;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PetInventoryMenu extends AbstractContainerMenu {
    private final @Nullable Mob pet;
    private final Player player;
    private final List<PetSlotType> petSlots;
    private final int petSlotCount;

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

        List<PetSlotType> left = PetSlotType.getLeftSlots(pet);
        List<PetSlotType> right = PetSlotType.getRightSlots(pet);

        this.petSlots = new ArrayList<>();
        this.petSlots.addAll(left);
        this.petSlots.addAll(right);
        this.petSlotCount = this.petSlots.size();

        Container petContainer = new PetSlotContainer(pet, this.petSlots);

        // Pet left slots: centered on Y between 18 and 90 (height = 72)
        int leftCount = left.size();
        int leftStartY = 18 + (72 - leftCount * 18) / 2;
        int slotIndex = 0;
        for (int i = 0; i < leftCount; i++) {
            PetSlotType type = left.get(i);
            int y = leftStartY + i * 18;
            this.addSlot(new DynamicPetSlot(petContainer, slotIndex++, 8, y, type, pet));
        }

        // Pet right slots: centered on Y between 18 and 90 (height = 72)
        int rightCount = right.size();
        int rightStartY = 18 + (72 - rightCount * 18) / 2;
        for (int i = 0; i < rightCount; i++) {
            PetSlotType type = right.get(i);
            int y = rightStartY + i * 18;
            this.addSlot(new DynamicPetSlot(petContainer, slotIndex++, 152, y, type, pet));
        }

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

    public int getPetSlotCount() {
        return this.petSlotCount;
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

            if (slotIndex < this.petSlotCount) {
                // From pet slot to player inventory
                if (!this.moveItemStackTo(slotStack, this.petSlotCount, this.petSlotCount + 36, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory to pet slots
                boolean moved = false;

                // First pass: try equipment slots other than hands
                for (int i = 0; i < this.petSlotCount; i++) {
                    PetSlotType type = this.petSlots.get(i);
                    if (type != PetSlotType.MAINHAND && type != PetSlotType.OFFHAND) {
                        if (type.mayPlace(slotStack, this.pet)) {
                            if (this.moveItemStackTo(slotStack, i, i + 1, false)) {
                                moved = true;
                                break;
                            }
                        }
                    }
                }

                // Second pass: try empty hand slots
                if (!moved) {
                    for (int i = 0; i < this.petSlotCount; i++) {
                        PetSlotType type = this.petSlots.get(i);
                        if (type == PetSlotType.MAINHAND || type == PetSlotType.OFFHAND) {
                            Slot targetSlot = this.slots.get(i);
                            if (!targetSlot.hasItem() && type.mayPlace(slotStack, this.pet)) {
                                if (this.moveItemStackTo(slotStack, i, i + 1, false)) {
                                    moved = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                // Fallback: move between player main storage and hotbar
                if (!moved) {
                    int playerMainStart = this.petSlotCount;
                    int playerMainEnd = this.petSlotCount + 27;
                    int playerHotbarEnd = this.petSlotCount + 36;

                    if (slotIndex >= playerMainStart && slotIndex < playerMainEnd) {
                        if (!this.moveItemStackTo(slotStack, playerMainEnd, playerHotbarEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (slotIndex >= playerMainEnd && slotIndex < playerHotbarEnd) {
                        if (!this.moveItemStackTo(slotStack, playerMainStart, playerMainEnd, false)) {
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

    public static class DynamicPetSlot extends Slot {
        private final Mob pet;
        private final PetSlotType slotType;

        public DynamicPetSlot(Container container, int index, int x, int y, PetSlotType slotType, @Nullable Mob pet) {
            super(container, index, x, y);
            this.pet = pet;
            this.slotType = slotType;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.slotType.mayPlace(stack, this.pet);
        }

        @Override
        public int getMaxStackSize() {
            return (this.slotType == PetSlotType.MAINHAND || this.slotType == PetSlotType.OFFHAND) ? 64 : 1;
        }

        @Override
        public @Nullable Identifier getNoItemIcon() {
            return this.slotType.getIcon(this.pet);
        }

        public PetSlotType getSlotType() {
            return this.slotType;
        }
    }

    private static class PetSlotContainer implements Container {
        private final @Nullable Mob pet;
        private final List<PetSlotType> slotTypes;

        public PetSlotContainer(@Nullable Mob pet, List<PetSlotType> slotTypes) {
            this.pet = pet;
            this.slotTypes = slotTypes;
        }

        @Override
        public int getContainerSize() {
            return this.slotTypes.size();
        }

        @Override
        public boolean isEmpty() {
            if (this.pet == null) return true;
            for (PetSlotType type : this.slotTypes) {
                if (!this.getItem(type).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        private ItemStack getItem(PetSlotType type) {
            if (this.pet == null) return ItemStack.EMPTY;
            if (type == PetSlotType.CHEST_STORAGE) {
                if (this.pet instanceof AbstractChestedHorse chested && chested.hasChest()) {
                    return new ItemStack(Items.CHEST);
                }
                return ItemStack.EMPTY;
            }
            EquipmentSlot eq = type.getEquipmentSlot();
            return eq != null ? this.pet.getItemBySlot(eq) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack getItem(int slotIndex) {
            if (slotIndex < 0 || slotIndex >= this.slotTypes.size()) {
                return ItemStack.EMPTY;
            }
            return getItem(this.slotTypes.get(slotIndex));
        }

        @Override
        public ItemStack removeItem(int slotIndex, int amount) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= this.slotTypes.size()) {
                return ItemStack.EMPTY;
            }
            PetSlotType type = this.slotTypes.get(slotIndex);
            if (type == PetSlotType.CHEST_STORAGE) {
                if (this.pet instanceof AbstractChestedHorse chested && chested.hasChest()) {
                    chested.setChest(false);
                    this.setChanged();
                    return new ItemStack(Items.CHEST);
                }
                return ItemStack.EMPTY;
            }
            EquipmentSlot eq = type.getEquipmentSlot();
            if (eq == null) return ItemStack.EMPTY;
            ItemStack current = this.pet.getItemBySlot(eq);
            if (current.isEmpty()) return ItemStack.EMPTY;
            ItemStack split = current.split(amount);
            this.pet.setItemSlot(eq, current.isEmpty() ? ItemStack.EMPTY : current);
            this.setChanged();
            return split;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slotIndex) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= this.slotTypes.size()) {
                return ItemStack.EMPTY;
            }
            PetSlotType type = this.slotTypes.get(slotIndex);
            if (type == PetSlotType.CHEST_STORAGE) {
                if (this.pet instanceof AbstractChestedHorse chested && chested.hasChest()) {
                    chested.setChest(false);
                    return new ItemStack(Items.CHEST);
                }
                return ItemStack.EMPTY;
            }
            EquipmentSlot eq = type.getEquipmentSlot();
            if (eq == null) return ItemStack.EMPTY;
            ItemStack current = this.pet.getItemBySlot(eq);
            if (current.isEmpty()) return ItemStack.EMPTY;
            this.pet.setItemSlot(eq, ItemStack.EMPTY);
            return current;
        }

        @Override
        public void setItem(int slotIndex, ItemStack stack) {
            if (this.pet == null || slotIndex < 0 || slotIndex >= this.slotTypes.size()) {
                return;
            }
            PetSlotType type = this.slotTypes.get(slotIndex);
            if (type == PetSlotType.CHEST_STORAGE) {
                if (this.pet instanceof AbstractChestedHorse chested) {
                    chested.setChest(!stack.isEmpty());
                }
                this.setChanged();
                return;
            }
            EquipmentSlot eq = type.getEquipmentSlot();
            if (eq != null) {
                this.pet.setItemSlot(eq, stack);
                this.pet.setDropChance(eq, 1.0F);
                this.setChanged();
            }
        }

        @Override
        public void setChanged() {
            if (this.pet != null) {
                for (PetSlotType type : this.slotTypes) {
                    EquipmentSlot eq = type.getEquipmentSlot();
                    if (eq != null) {
                        this.pet.setDropChance(eq, 1.0F);
                    }
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
                for (PetSlotType type : this.slotTypes) {
                    if (type == PetSlotType.CHEST_STORAGE) {
                        if (this.pet instanceof AbstractChestedHorse chested) {
                            chested.setChest(false);
                        }
                    } else {
                        EquipmentSlot eq = type.getEquipmentSlot();
                        if (eq != null) {
                            this.pet.setItemSlot(eq, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
}
