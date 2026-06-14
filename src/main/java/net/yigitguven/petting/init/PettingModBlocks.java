package net.yigitguven.petting.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.core.registries.Registries;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.block.Pet_BedBlock;

public class PettingModBlocks {
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(Registries.BLOCK, PettingMod.MODID);

    public static final RegistryObject<net.minecraft.world.level.block.Block> PET_BED = REGISTRY.register("pet_bed", 
        () -> new Pet_BedBlock(BlockBehaviour.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.WOOD).strength(1.0f, 10.0f).sound(SoundType.WOOD).noOcclusion()));
}
