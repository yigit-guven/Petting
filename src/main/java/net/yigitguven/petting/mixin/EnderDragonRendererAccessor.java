package net.yigitguven.petting.mixin;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnderDragonRenderer.class)
public interface EnderDragonRendererAccessor {
    @Accessor("EYES")
    static RenderType getEyes() {
        throw new AssertionError();
    }
}
