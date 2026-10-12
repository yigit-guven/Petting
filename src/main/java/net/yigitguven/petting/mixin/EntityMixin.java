package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "considersEntityAsAlly", at = @At("HEAD"), cancellable = true)
    private void petting$considersEntityAsAlly(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;

        // If self is owner and other is pet:
        if (self instanceof Player player && PetHelper.isRootOwner(other, player)) {
            if (!PettingServerConfig.canOwnerAttackPets()) {
                cir.setReturnValue(true);
            }
            return;
        }

        // If other is owner and self is pet:
        if (other instanceof Player player && PetHelper.isRootOwner(self, player)) {
            if (!PettingServerConfig.canOwnerAttackPets()) {
                cir.setReturnValue(true);
            }
            return;
        }

        // Fellow pets/minions of the same owner
        if (PetHelper.arePetsOfSameOwner(self, other)) {
            if (!PettingServerConfig.canPetsAttackPets()) {
                cir.setReturnValue(true);
            }
            return;
        }

        // Vehicle / passenger relationship
        if (self.isPassengerOfSameVehicle(other) || self.hasPassenger(other) || other.hasPassenger(self)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "doTeamsAllowDamage", at = @At("HEAD"), cancellable = true)
    private void petting$doTeamsAllowDamage(Entity other, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;

        // If other is owner and self is pet: NEVER allow pet to damage owner
        if (other instanceof Player player && PetHelper.isRootOwner(self, player)) {
            cir.setReturnValue(false);
            return;
        }

        // If self is owner and other is pet: respect canOwnerAttackPets
        if (self instanceof Player player && PetHelper.isRootOwner(other, player)) {
            cir.setReturnValue(PettingServerConfig.canOwnerAttackPets());
            return;
        }

        // If both are pets/minions of the same owner: check canPetsAttackPets
        if (PetHelper.arePetsOfSameOwner(self, other)) {
            cir.setReturnValue(PettingServerConfig.canPetsAttackPets());
            return;
        }

        // Vehicle / passenger relationship: prevent inadvertent collision/team damage
        if (self.isPassengerOfSameVehicle(other) || self.hasPassenger(other) || other.hasPassenger(self)) {
            cir.setReturnValue(false);
        }
    }
}
