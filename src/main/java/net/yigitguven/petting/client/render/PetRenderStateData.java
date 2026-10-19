package net.yigitguven.petting.client.render;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public record PetRenderStateData(
        @Nullable DyeColor collarColor,
        boolean collarEnchanted,
        float sitGroundOffset
) {}
