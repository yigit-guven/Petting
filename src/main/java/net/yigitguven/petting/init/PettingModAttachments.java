package net.yigitguven.petting.init;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.PetData;

import java.util.function.Supplier;

public class PettingModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Petting.MODID);

    public static final Supplier<AttachmentType<PetData>> PET_DATA =
            ATTACHMENT_TYPES.register("pet_data", () -> AttachmentType.builder(PetData::new)
                    .serialize(PetData.MAP_CODEC)
                    .sync(PetData.STREAM_CODEC)
                    .build());
}
