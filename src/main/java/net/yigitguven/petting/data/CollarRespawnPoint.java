package net.yigitguven.petting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record CollarRespawnPoint(GlobalPos pos, boolean isCushion) {
    public static final Codec<CollarRespawnPoint> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    GlobalPos.CODEC.fieldOf("pos").forGetter(CollarRespawnPoint::pos),
                    Codec.BOOL.fieldOf("is_cushion").forGetter(CollarRespawnPoint::isCushion)
            ).apply(instance, CollarRespawnPoint::new)
    );

    public static final StreamCodec<ByteBuf, CollarRespawnPoint> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC,
            CollarRespawnPoint::pos,
            ByteBufCodecs.BOOL,
            CollarRespawnPoint::isCushion,
            CollarRespawnPoint::new
    );
}
