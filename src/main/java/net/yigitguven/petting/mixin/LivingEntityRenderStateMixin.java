package net.yigitguven.petting.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.item.DyeColor;
import net.yigitguven.petting.client.render.PetRenderStateExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements PetRenderStateExtension {
    @Unique
    @Nullable
    private DyeColor petting$collarColor;

    @Unique
    private boolean petting$collarEnchanted;

    @Unique
    @Nullable
    private Float petting$sitGroundOffset;

    @Override
    public @Nullable DyeColor petting$getCollarColor() {
        return this.petting$collarColor;
    }

    @Override
    public void petting$setCollarColor(@Nullable DyeColor color) {
        this.petting$collarColor = color;
    }

    @Override
    public boolean petting$isCollarEnchanted() {
        return this.petting$collarEnchanted;
    }

    @Override
    public void petting$setCollarEnchanted(boolean enchanted) {
        this.petting$collarEnchanted = enchanted;
    }

    @Override
    public @Nullable Float petting$getSitGroundOffset() {
        return this.petting$sitGroundOffset;
    }

    @Override
    public void petting$setSitGroundOffset(@Nullable Float offset) {
        this.petting$sitGroundOffset = offset;
    }
}
