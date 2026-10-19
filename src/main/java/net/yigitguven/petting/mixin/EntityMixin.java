package net.yigitguven.petting.mixin;

import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), cancellable = true)
    private void petting$isAlliedTo(Entity other, CallbackInfoReturnable<Boolean> cir) {
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

    @Inject(method = "getPassengersRidingOffset", at = @At("HEAD"), cancellable = true)
    private void petting$getPassengersRidingOffset(CallbackInfoReturnable<Double> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof EnderDragon dragon && PetHelper.isTamed(dragon)) {
            cir.setReturnValue(1.85D);
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void petting$onEntityInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof EnderDragonPart part) {
            if (part.parentMob != null && PetHelper.isTamed(part.parentMob)) {
                InteractionResult result = PetHelper.handleTamedDragonInteract(part.parentMob, player, hand);
                if (result.consumesAction()) {
                    cir.setReturnValue(result);
                }
            }
        } else if (self instanceof EnderDragon dragon && PetHelper.isTamed(dragon)) {
            InteractionResult result = PetHelper.handleTamedDragonInteract(dragon, player, hand);
            if (result.consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void petting$onSpawnItemLikeAtLocation(ItemLike itemLike, CallbackInfoReturnable<ItemEntity> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof FallingBlockEntity fallingBlock) {
            if (fallingBlock.getBlockState().is(PettingModBlocks.GOLDEN_DRAGON_EGG.get())) {
                ItemStack stack = new ItemStack(itemLike);
                if (fallingBlock.blockData != null) {
                    if (fallingBlock.blockData.hasUUID("OwnerUUID")) {
                        stack.getOrCreateTag().putUUID("petting:dragon_egg_owner", fallingBlock.blockData.getUUID("OwnerUUID"));
                    }
                    if (fallingBlock.blockData.contains("OwnerName", Tag.TAG_STRING)) {
                        stack.getOrCreateTag().putString("petting:dragon_egg_owner_name", fallingBlock.blockData.getString("OwnerName"));
                    }
                }
                cir.setReturnValue(self.spawnAtLocation(stack, 0.0F));
            }
        }
    }
}
