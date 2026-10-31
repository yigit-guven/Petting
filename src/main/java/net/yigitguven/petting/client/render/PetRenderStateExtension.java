package net.yigitguven.petting.client.render;

import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public interface PetRenderStateExtension {
    @Nullable
    DyeColor petting$getCollarColor();

    void petting$setCollarColor(@Nullable DyeColor color);

    boolean petting$isCollarEnchanted();

    void petting$setCollarEnchanted(boolean enchanted);

    @Nullable
    Float petting$getSitGroundOffset();

    void petting$setSitGroundOffset(@Nullable Float offset);
}
