package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.GoldenDragonEggBlock;

public class PettingModBlocks {
    public static final BlockSupplier<GoldenDragonEggBlock> GOLDEN_DRAGON_EGG = register(
            "golden_dragon_egg",
            new GoldenDragonEggBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DRAGON_EGG).lightLevel(state -> 6))
    );

    private static <T extends Block> BlockSupplier<T> register(String name, T block) {
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Petting.MODID, name), block);
        return new BlockSupplier<>(block);
    }

    public static void register() {
    }
}
