package net.yigitguven.petting.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.entity.GoldenDragonEggBlockEntity;

public class PettingModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Petting.MODID);

    public static final RegistryObject<BlockEntityType<GoldenDragonEggBlockEntity>> GOLDEN_DRAGON_EGG =
            BLOCK_ENTITY_TYPES.register("golden_dragon_egg", () ->
                    BlockEntityType.Builder.of(GoldenDragonEggBlockEntity::new, PettingModBlocks.GOLDEN_DRAGON_EGG.get()).build(null));
}
