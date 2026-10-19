package net.yigitguven.petting.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.GoldenDragonEggBlock;

public class PettingModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Petting.MODID);

    public static final RegistryObject<GoldenDragonEggBlock> GOLDEN_DRAGON_EGG =
            BLOCKS.register("golden_dragon_egg",
                    () -> new GoldenDragonEggBlock(BlockBehaviour.Properties.copy(Blocks.DRAGON_EGG)
                            .lightLevel(state -> 6)));
}
