package net.yigitguven.petting.client.render;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.init.PettingDataComponents;
import net.yigitguven.petting.item.CollarItem;
import net.yigitguven.petting.util.PetHelper;

public class PettingClientRender {

    public static void modifyLivingEntityRenderState(LivingEntity entity, LivingEntityRenderState renderState) {
        if (PetHelper.isTamed(entity)) {
            PetRenderStateExtension ext = (renderState instanceof PetRenderStateExtension e) ? e : null;
            ItemStack collar = PetHelper.getCollar(entity);
            if (!collar.isEmpty() && collar.getItem() instanceof CollarItem collarItem) {
                if (ext != null) {
                    ext.petting$setCollarColor(collarItem.getColor());
                    boolean isEnchanted = collar.get(PettingDataComponents.COLLAR_RESPAWN_POINT.get()) != null
                            || collar.get(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE) != null;
                    ext.petting$setCollarEnchanted(isEnchanted);
                }
            }

            // Strict rotation lock when ridden with saddle
            if (entity instanceof Mob mob && mob.getFirstPassenger() instanceof Player rider && PetHelper.hasSaddle(mob)) {
                renderState.bodyRot = rider.getYRot();
                renderState.yRot = 0.0F;
                renderState.xRot = rider.getXRot();
            }

            if (PetHelper.isSitting(entity) && !entity.isPassenger()) {
                boolean isHumanoid = (renderState instanceof net.minecraft.client.renderer.entity.state.HumanoidRenderState);
                boolean isIllager = (renderState instanceof net.minecraft.client.renderer.entity.state.IllagerRenderState);
                boolean isFox = (renderState instanceof net.minecraft.client.renderer.entity.state.FoxRenderState);
                boolean isPanda = (renderState instanceof net.minecraft.client.renderer.entity.state.PandaRenderState);
                boolean isWolf = (renderState instanceof net.minecraft.client.renderer.entity.state.WolfRenderState);
                boolean isFeline = (renderState instanceof net.minecraft.client.renderer.entity.state.FelineRenderState);

                if (isHumanoid) {
                    ((net.minecraft.client.renderer.entity.state.HumanoidRenderState) renderState).isPassenger = true;
                } else if (isIllager) {
                    ((net.minecraft.client.renderer.entity.state.IllagerRenderState) renderState).isRiding = true;
                } else if (isFox) {
                    ((net.minecraft.client.renderer.entity.state.FoxRenderState) renderState).isSitting = true;
                } else if (isPanda) {
                    ((net.minecraft.client.renderer.entity.state.PandaRenderState) renderState).isSitting = true;
                } else if (isWolf) {
                    ((net.minecraft.client.renderer.entity.state.WolfRenderState) renderState).isSitting = true;
                } else if (isFeline) {
                    ((net.minecraft.client.renderer.entity.state.FelineRenderState) renderState).isSitting = true;
                }

                renderState.walkAnimationPos = 0.0F;
                renderState.walkAnimationSpeed = 0.0F;

                if (entity.onGround() && (isHumanoid || isIllager)) {
                    float baseOffset = (renderState instanceof net.minecraft.client.renderer.entity.state.EndermanRenderState) ? 0.3F : 0.5F;
                    float scale = renderState.scale > 0.0F ? renderState.scale : 1.0F;
                    float finalOffset = baseOffset * (renderState.isBaby ? 0.5F : 1.0F) * scale;
                    if (ext != null) {
                        ext.petting$setSitGroundOffset(finalOffset);
                    }

                    if (renderState.nameTagAttachment != null) {
                        renderState.nameTagAttachment = renderState.nameTagAttachment.subtract(0.0D, finalOffset, 0.0D);
                    }
                }
            }
        }
    }
}
