package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin {

    @Inject(method = "tame", at = @At("HEAD"), cancellable = true)
    private void petting$onTame(Player player, CallbackInfo ci) {
        TamableAnimal self = (TamableAnimal) (Object) this;
        if (PetHelper.isTamed(self)) {
            ci.cancel();
        }
    }
}
