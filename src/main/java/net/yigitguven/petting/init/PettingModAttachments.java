package net.yigitguven.petting.init;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.PetData;

public class PettingModAttachments {
    public static final AttachmentType<PetData> PET_DATA = AttachmentRegistry.create(
            Identifier.fromNamespaceAndPath(Petting.MODID, "pet_data"),
            builder -> builder
                    .initializer(PetData::new)
                    .persistent(PetData.MAP_CODEC.codec())
                    .syncWith(PetData.STREAM_CODEC, AttachmentSyncPredicate.all())
    );

    public static void register() {
    }
}
