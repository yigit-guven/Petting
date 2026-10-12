package net.yigitguven.petting.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
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
}
