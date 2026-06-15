package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.block.Pet_BedBlock;

public class PettingModBlocks {
    public static Block PET_BED;

    public static void register() {
        PET_BED = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(PettingMod.MODID, "pet_bed"), new Pet_BedBlock(BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL)));
    }
}
