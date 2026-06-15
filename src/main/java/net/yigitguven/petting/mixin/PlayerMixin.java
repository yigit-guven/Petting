package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.yigitguven.petting.init.PettingModAttributes;

@Mixin(Player.class)
public class PlayerMixin {
    @Inject(method = "createAttributes", at = @At("RETURN"))
    private static void addPettingAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        AttributeSupplier.Builder builder = cir.getReturnValue();
        builder.add(PettingModAttributes.MAX_PETS);
        for (int i = 0; i < PettingModAttributes.MAX_PETS_CATEGORIES.length; i++) {
            builder.add(PettingModAttributes.MAX_PETS_CATEGORIES[i]);
        }
    }
}

