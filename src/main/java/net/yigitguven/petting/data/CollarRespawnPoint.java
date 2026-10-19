package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public record CollarRespawnPoint(GlobalPos pos, boolean isCushion) {
    public static final Codec<CollarRespawnPoint> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    GlobalPos.CODEC.fieldOf("pos").forGetter(CollarRespawnPoint::pos),
                    Codec.BOOL.fieldOf("is_cushion").forGetter(CollarRespawnPoint::isCushion)
            ).apply(instance, CollarRespawnPoint::new)
    );

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeGlobalPos(pos);
        buf.writeBoolean(isCushion);
    }

    public static CollarRespawnPoint fromBytes(FriendlyByteBuf buf) {
        GlobalPos globalPos = buf.readGlobalPos();
        boolean cushion = buf.readBoolean();
        return new CollarRespawnPoint(globalPos, cushion);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dim", pos.dimension().location().toString());
        tag.putLong("pos", pos.pos().asLong());
        tag.putBoolean("is_cushion", isCushion);
        return tag;
    }

    public static CollarRespawnPoint fromNbt(CompoundTag tag) {
        if (tag == null || !tag.contains("dim") || !tag.contains("pos")) return null;
        ResourceLocation dimLoc = new ResourceLocation(tag.getString("dim"));
        ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, dimLoc);
        BlockPos p = BlockPos.of(tag.getLong("pos"));
        return new CollarRespawnPoint(GlobalPos.of(dim, p), tag.getBoolean("is_cushion"));
    }
}
