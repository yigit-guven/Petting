package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.entity.GoldenDragonEggBlockEntity;

import java.util.Set;

public class PettingModBlockEntities {
    public static final RegistrySupplier<BlockEntityType<GoldenDragonEggBlockEntity>> GOLDEN_DRAGON_EGG = register(
            "golden_dragon_egg",
            new BlockEntityType<>(GoldenDragonEggBlockEntity::new, Set.of(PettingModBlocks.GOLDEN_DRAGON_EGG.get()))
    );

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, BlockEntityType<T> type) {
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Petting.MODID, name), type);
        return new RegistrySupplier<>(type);
    }

    public static void register() {
    }
}
