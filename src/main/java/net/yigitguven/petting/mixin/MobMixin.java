package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.nbt.CompoundTag;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.PetAttackLogic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (target == null) return;

        Player attackerOwner = PetAttackLogic.getOwner(mob);
        Player targetOwner = null;
        if (target instanceof Mob targetMob) {
            targetOwner = PetAttackLogic.getOwner(targetMob);
        }

        // Prevent pets of the SAME owner from targeting each other
        if (attackerOwner != null && attackerOwner == targetOwner) {
            ci.cancel();
            return;
        }

        if (target == attackerOwner) {
            ci.cancel();
            return;
        }

        if (!PetAttackLogic.isCustomPet(mob)) return;

        if (target == mob) { ci.cancel(); return; }
        if (PetAttackLogic.isOwnerOf(target, attackerOwner)) { ci.cancel(); }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        PetAttackLogic.tick(mob);
    }
}
