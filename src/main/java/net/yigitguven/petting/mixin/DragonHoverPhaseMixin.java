package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoverPhase;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonHoverPhase.class)
public abstract class DragonHoverPhaseMixin extends AbstractDragonPhaseInstance {

    public DragonHoverPhaseMixin(EnderDragon dragon) {
        super(dragon);
    }

    @Inject(method = "getFlyTargetLocation", at = @At("HEAD"), cancellable = true)
    private void petting$getFlyTargetLocation(CallbackInfoReturnable<Vec3> cir) {
        if (PetHelper.isTamed(this.dragon)) {
            Vec3 target = PetHelper.getTamedDragonFlyTarget(this.dragon);
            cir.setReturnValue(target);
        }
    }

    @Inject(method = "isSitting", at = @At("HEAD"), cancellable = true)
    private void petting$isSitting(CallbackInfoReturnable<Boolean> cir) {
        if (PetHelper.isTamed(this.dragon)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getFlySpeed", at = @At("HEAD"), cancellable = true)
    private void petting$getFlySpeed(CallbackInfoReturnable<Float> cir) {
        if (PetHelper.isTamed(this.dragon) && this.dragon.getTarget() != null) {
            cir.setReturnValue(1.25F);
        }
    }
}
