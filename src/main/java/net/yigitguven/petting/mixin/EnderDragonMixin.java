package net.yigitguven.petting.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.data.PetOrder;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {

    @Inject(method = "knockBack", at = @At("HEAD"))
    private void petting$preventOwnerKnockBack(ServerLevel level, List<Entity> entities, CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (PetHelper.isTamed(dragon)) {
            entities.removeIf(e -> PetHelper.areEntitiesAllied(dragon, e));
        }
    }

    @Inject(method = "hurt(Lnet/minecraft/server/level/ServerLevel;Ljava/util/List;)V", at = @At("HEAD"))
    private void petting$preventOwnerHurt(ServerLevel level, List<Entity> entities, CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (PetHelper.isTamed(dragon)) {
            entities.removeIf(e -> PetHelper.areEntitiesAllied(dragon, e));
        }
    }

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void petting$dragonCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (PetHelper.isTamed(dragon)) {
            if (target instanceof Player player && PetHelper.isRootOwner(dragon, player)) {
                cir.setReturnValue(false);
                return;
            }
            if (!PettingServerConfig.canPetsAttackPets() && PetHelper.arePetsOfSameOwner(dragon, target)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "checkWalls", at = @At("HEAD"), cancellable = true)
    private void petting$preventTamedBlockDestruction(ServerLevel level, AABB box, CallbackInfoReturnable<Boolean> cir) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (PetHelper.isTamed(dragon)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void petting$tamedDragonAiStepPre(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (PetHelper.isTamed(dragon) && !dragon.level().isClientSide()) {
            if (dragon.getPhaseManager().getCurrentPhase().getPhase() != EnderDragonPhase.HOVERING) {
                dragon.getPhaseManager().setPhase(EnderDragonPhase.HOVERING);
            }
            PetHelper.tickTamedDragon(dragon);
        }
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void petting$tamedDragonAiStep(CallbackInfo ci) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (!PetHelper.isTamed(dragon) || dragon.level().isClientSide()) {
            return;
        }

        // When ridden, PetRidingHandler handles movement
        if (dragon.isVehicle()) {
            return;
        }

        PetOrder order = PetHelper.getOrder(dragon);
        if (order == PetOrder.SIT) {
            dragon.setDeltaMovement(dragon.getDeltaMovement().multiply(0.7D, 0.5D, 0.7D));
            if (!dragon.onGround()) {
                dragon.setDeltaMovement(dragon.getDeltaMovement().add(0.0D, -0.05D, 0.0D));
            }
        }
    }
}
