package net.yigitguven.petting.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ComponentSupplier<T> extends RegistrySupplier<DataComponentType<T>> implements DataComponentType<T> {
    public ComponentSupplier(DataComponentType<T> value) {
        super(value);
    }

    @Override
    public Codec<T> codec() {
        return get().codec();
    }

    @Override
    public boolean ignoreSwapAnimation() {
        return get().ignoreSwapAnimation();
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
        return get().streamCodec();
    }
}
