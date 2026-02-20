package net.yigitguven.petting.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.init.PettingModAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, net.minecraft.world.level.Level level) {
        super(entityType, level);
    }

    @Inject(method = "createAttributes", at = @At("RETURN"), cancellable = true)
    private static void petting$addCustomAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        AttributeSupplier.Builder builder = info.getReturnValue();
        builder.add(PettingModAttributes.MAX_PETS);
        for (int i = 0; i < 20; i++) {
            builder.add(PettingModAttributes.MAX_PETS_CATEGORIES[i]);
        }
    }
}
