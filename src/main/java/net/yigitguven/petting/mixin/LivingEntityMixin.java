package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void petting$canAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Pets/minions can never consider their owner as attackable enemy
        if (target instanceof Player player && PetHelper.isRootOwner(self, player)) {
            cir.setReturnValue(false);
            return;
        }

        // Owner attacking their pet: check config
        if (self instanceof Player player && PetHelper.isRootOwner(target, player)) {
            cir.setReturnValue(PettingServerConfig.canOwnerAttackPets());
            return;
        }

        // Pets attacking fellow pets: check config
        if (PetHelper.arePetsOfSameOwner(self, target)) {
            cir.setReturnValue(PettingServerConfig.canPetsAttackPets());
            return;
        }
    }
}
