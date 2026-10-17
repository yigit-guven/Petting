package net.yigitguven.petting.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.yigitguven.petting.inventory.PetSlotType;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected M model;

    @Unique
    private boolean petting$isHumanoidPetSitting(LivingEntity entity) {
        if (!PetHelper.isTamed(entity) || !PetHelper.isSitting(entity) || entity.isPassenger()) {
            return false;
        }
        return (this.model instanceof HumanoidModel<?>) || PetSlotType.canEquipHumanoidArmor(entity);
    }

    @ModifyVariable(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("STORE"),
            ordinal = 0
    )
    private boolean petting$modifyShouldSit(boolean shouldSit, LivingEntity entity) {
        if (!shouldSit && petting$isHumanoidPetSitting(entity)) {
            return true;
        }
        return shouldSit;
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER)
    )
    private void petting$applySittingTranslation(
            LivingEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            CallbackInfo ci
    ) {
        if (petting$isHumanoidPetSitting(entity)) {
            float offset = PetHelper.getSittingOffset(entity);
            poseStack.translate(0.0D, -offset, 0.0D);
        }
    }
}
