package net.yigitguven.petting.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.attachment.PetInventoryAttachment;

public class PettingModAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, PettingMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PetInventoryAttachment>> PET_INVENTORY = REGISTRY.register("pet_inventory", 
        () -> AttachmentType.builder(PetInventoryAttachment::new)
            .serialize(new net.neoforged.neoforge.attachment.IAttachmentSerializer<CompoundTag, PetInventoryAttachment>() {
                @Override
                public CompoundTag write(PetInventoryAttachment attachment, HolderLookup.Provider provider) {
                    return attachment.serializeNBT(provider);
                }
                @Override
                public PetInventoryAttachment read(IAttachmentHolder holder, CompoundTag nbt, HolderLookup.Provider provider) {
                    PetInventoryAttachment attachment = new PetInventoryAttachment(holder);
                    attachment.deserializeNBT(provider, nbt);
                    return attachment;
                }
            }).build());
}
