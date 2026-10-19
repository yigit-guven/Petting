package net.yigitguven.petting.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Monster.class)
public abstract class MonsterMixin {

    @Inject(method = "isPreventingPlayerRest", at = @At("HEAD"), cancellable = true)
    private void petting$isPreventingPlayerRest(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!PettingServerConfig.shouldTamedMonstersPreventSleep()) {
            Monster monster = (Monster) (Object) this;
            if (PetHelper.isTamed(monster) && monster.getTarget() != player) {
                cir.setReturnValue(false);
            }
        }
    }
}
