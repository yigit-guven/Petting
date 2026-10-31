package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.yigitguven.petting.event.PetGoalEvents;
import net.yigitguven.petting.event.PetRidingEvents;
import net.yigitguven.petting.event.PetSafetyEvents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void petting$onSetTarget(@Nullable LivingEntity target, CallbackInfo ci) {
        if (target != null) {
            Mob self = (Mob) (Object) this;
            if (PetSafetyEvents.shouldCancelTarget(self, target)) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void petting$onMobTickPre(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        PetSafetyEvents.onEntityTick(self);
        PetGoalEvents.onEntityTickPre(self);
        PetRidingEvents.onEntityTickPre(self);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void petting$onMobTickPost(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        PetGoalEvents.onEntityTickPost(self);
        PetRidingEvents.onEntityTickPost(self);
    }
}
