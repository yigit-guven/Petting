package net.yigitguven.petting.mixin;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.client.renderer.entity.state.FoxRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PandaRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.client.render.PetRenderStateData;
import net.yigitguven.petting.client.render.PettingClientRender;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("TAIL")
    )
    private void petting$onExtractRenderState(LivingEntity entity, LivingEntityRenderState renderState, float partialTick, CallbackInfo ci) {
        if (!PetHelper.isTamed(entity)) {
            return;
        }

        DyeColor collarColor = null;
        boolean isEnchanted = false;
        ItemStack collar = PetHelper.getCollar(entity);
        if (!collar.isEmpty() && collar.getItem() instanceof CollarItem collarItem) {
            collarColor = collarItem.getColor();
            isEnchanted = collar.has(net.yigitguven.petting.init.PettingDataComponents.COLLAR_RESPAWN_POINT.get())
                    || collar.has(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE);
        }

        // Strict rotation lock when ridden with saddle
        if (entity instanceof net.minecraft.world.entity.Mob mob && mob.getFirstPassenger() instanceof net.minecraft.world.entity.player.Player rider && PetHelper.hasSaddle(mob)) {
            renderState.bodyRot = rider.getYRot();
            renderState.yRot = 0.0F;
            renderState.xRot = rider.getXRot();
        }

        float sitGroundOffset = 0.0F;
        boolean isSittingPet = false;
        if (PetHelper.isSitting(entity) && !entity.isPassenger()) {
            isSittingPet = true;
            boolean isHumanoid = (renderState instanceof HumanoidRenderState);
            boolean isIllager = (renderState instanceof IllagerRenderState);
            boolean isFox = (renderState instanceof FoxRenderState);
            boolean isPanda = (renderState instanceof PandaRenderState);
            boolean isWolf = (renderState instanceof WolfRenderState);
            boolean isFeline = (renderState instanceof FelineRenderState);

            if (isHumanoid) {
                ((HumanoidRenderState) renderState).isPassenger = true;
            } else if (isIllager) {
                ((IllagerRenderState) renderState).isRiding = true;
            } else if (isFox) {
                ((FoxRenderState) renderState).isSitting = true;
            } else if (isPanda) {
                ((PandaRenderState) renderState).isSitting = true;
            } else if (isWolf) {
                ((WolfRenderState) renderState).isSitting = true;
            } else if (isFeline) {
                ((FelineRenderState) renderState).isSitting = true;
            }

            renderState.walkAnimationPos = 0.0F;
            renderState.walkAnimationSpeed = 0.0F;
            renderState.isDiscrete = true;

            if (entity.onGround() && (isHumanoid || isIllager)) {
                float baseOffset = (renderState instanceof EndermanRenderState) ? 0.3F : 0.5F;
                float scale = renderState.scale > 0.0F ? renderState.scale : 1.0F;
                sitGroundOffset = baseOffset * (renderState.isBaby ? 0.5F : 1.0F) * scale;

                if (renderState.nameTagAttachment != null) {
                    renderState.nameTagAttachment = renderState.nameTagAttachment.subtract(0.0D, sitGroundOffset, 0.0D);
                }
            }
        }

        PettingClientRender.putRenderData(renderState, new PetRenderStateData(collarColor, isEnchanted, sitGroundOffset, isSittingPet));
    }
}
