package net.yigitguven.petting.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {

    @Unique
    private static final RenderType GREEN_EYES_RENDER_TYPE =
            RenderType.eyes(new ResourceLocation(Petting.MODID, "textures/entity/enderdragon/dragon_eyes_green.png"));

    @Unique
    private static final ThreadLocal<EnderDragon> petting$currentDragon = new ThreadLocal<>();

    @Inject(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void petting$beforeRender(EnderDragon dragon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        petting$currentDragon.set(dragon);
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"),
            index = 0
    )
    private RenderType petting$modifyDragonEyesRenderType(RenderType original) {
        EnderDragon dragon = petting$currentDragon.get();
        if (dragon != null && PetHelper.isTamed(dragon) && original == EnderDragonRendererAccessor.getEyes()) {
            return GREEN_EYES_RENDER_TYPE;
        }
        return original;
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void petting$afterRender(EnderDragon dragon, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        petting$currentDragon.remove();
    }
}
