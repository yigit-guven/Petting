package net.yigitguven.petting.init;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.GoldenDragonEggBlock;

public class PettingModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Petting.MODID);

    public static final DeferredBlock<GoldenDragonEggBlock> GOLDEN_DRAGON_EGG =
            BLOCKS.registerBlock("golden_dragon_egg",
                    GoldenDragonEggBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.DRAGON_EGG).lightLevel(state -> 6));
}
