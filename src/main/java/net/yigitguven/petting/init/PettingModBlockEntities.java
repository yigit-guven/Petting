package net.yigitguven.petting.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.block.entity.GoldenDragonEggBlockEntity;

import java.util.Set;

public class PettingModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Petting.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GoldenDragonEggBlockEntity>> GOLDEN_DRAGON_EGG =
            BLOCK_ENTITY_TYPES.register("golden_dragon_egg", () ->
                    new BlockEntityType<>(GoldenDragonEggBlockEntity::new, Set.of(PettingModBlocks.GOLDEN_DRAGON_EGG.get())));
}
