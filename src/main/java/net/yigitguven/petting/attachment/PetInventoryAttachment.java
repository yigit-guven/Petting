package net.yigitguven.petting.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.world.entity.Entity;

public class PetInventoryAttachment extends ItemStackHandler implements INBTSerializable<CompoundTag> {
    private final Entity holder;

    public PetInventoryAttachment(IAttachmentHolder holder) {
        super(7); // 1 Saddle, 4 Armor, 2 Hands
        this.holder = holder instanceof Entity ? (Entity) holder : null;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (holder != null) {
            holder.setData(net.yigitguven.petting.init.PettingModAttachments.PET_INVENTORY, this);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return super.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        super.deserializeNBT(provider, nbt);
    }
}
