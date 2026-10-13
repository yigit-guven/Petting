package net.yigitguven.petting.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(EnderDragonRenderer.class)
public abstract class EnderDragonRendererMixin {

    @Unique
    private static final RenderType GREEN_EYES_RENDER_TYPE =
            RenderTypes.eyes(Identifier.fromNamespaceAndPath(Petting.MODID, "textures/entity/enderdragon/dragon_eyes_green.png"));

    @Unique
    private static final Map<EnderDragonRenderState, Boolean> petting$tamedStates =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Unique
    private static final ThreadLocal<EnderDragonRenderState> petting$currentSubmittingState = new ThreadLocal<>();

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;F)V", at = @At("TAIL"))
    private void petting$onExtractRenderState(EnderDragon dragon, EnderDragonRenderState state, float partialTick, CallbackInfo ci) {
        petting$tamedStates.put(state, PetHelper.isTamed(dragon));
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void petting$beforeSubmit(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        petting$currentSubmittingState.set(state);
    }

    @ModifyArg(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;III)V"),
            index = 3
    )
    private RenderType petting$modifyDragonEyesRenderType(RenderType original) {
        EnderDragonRenderState state = petting$currentSubmittingState.get();
        if (state != null && Boolean.TRUE.equals(petting$tamedStates.get(state))) {
            return GREEN_EYES_RENDER_TYPE;
        }
        return original;
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/EnderDragonRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    private void petting$afterSubmit(EnderDragonRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        petting$currentSubmittingState.remove();
    }
}
