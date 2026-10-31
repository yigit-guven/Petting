package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.data.CollarRespawnPoint;
import net.yigitguven.petting.data.CollarSettings;

import java.util.UUID;
import java.util.function.UnaryOperator;

public class PettingDataComponents {

    public static final ComponentSupplier<CollarSettings> COLLAR_SETTINGS =
            register("collar_settings", builder -> builder.persistent(CollarSettings.CODEC).networkSynchronized(CollarSettings.STREAM_CODEC));

    public static final ComponentSupplier<CollarRespawnPoint> COLLAR_RESPAWN_POINT =
            register("collar_respawn_point", builder -> builder.persistent(CollarRespawnPoint.CODEC).networkSynchronized(CollarRespawnPoint.STREAM_CODEC));

    public static final ComponentSupplier<UUID> DRAGON_EGG_OWNER =
            register("dragon_egg_owner", builder -> builder.persistent(net.minecraft.core.UUIDUtil.CODEC).networkSynchronized(net.minecraft.core.UUIDUtil.STREAM_CODEC));

    public static final ComponentSupplier<String> DRAGON_EGG_OWNER_NAME =
            register("dragon_egg_owner_name", builder -> builder.persistent(com.mojang.serialization.Codec.STRING).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8));

    private static <T> ComponentSupplier<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builderUnaryOperator) {
        DataComponentType<T> type = builderUnaryOperator.apply(DataComponentType.builder()).build();
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath(Petting.MODID, name), type);
        return new ComponentSupplier<>(type);
    }

    public static void register() {
    }
}
