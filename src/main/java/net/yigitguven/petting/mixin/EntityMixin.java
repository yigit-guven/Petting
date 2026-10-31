package net.yigitguven.petting.mixin;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.yigitguven.petting.config.PettingServerConfig;
import net.yigitguven.petting.entity.PetRidingHandler;
import net.yigitguven.petting.event.PetRidingEvents;
import net.yigitguven.petting.init.PettingDataComponents;
import net.yigitguven.petting.init.PettingModBlocks;
import net.yigitguven.petting.util.PetHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Inject(
            method = "getPassengerAttachmentPoint(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/EntityDimensions;F)Lnet/minecraft/world/phys/Vec3;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void petting$getPassengerAttachmentPoint(Entity passenger, net.minecraft.world.entity.EntityDimensions dimensions, float scale, CallbackInfoReturnable<net.minecraft.world.phys.Vec3> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon && PetHelper.isTamed(dragon)) {
            cir.setReturnValue(new net.minecraft.world.phys.Vec3(0.0D, 1.85D, 0.0D));
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void petting$onEntityInteract(Player player, net.minecraft.world.InteractionHand hand, net.minecraft.world.phys.Vec3 location, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragonPart part) {
            if (part.parentMob instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon && PetHelper.isTamed(dragon)) {
                net.minecraft.world.InteractionResult result = PetHelper.handleTamedDragonInteract(dragon, player, hand);
                if (result.consumesAction()) {
                    cir.setReturnValue(result);
                }
            }
        } else if (self instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon && PetHelper.isTamed(dragon)) {
            net.minecraft.world.InteractionResult result = PetHelper.handleTamedDragonInteract(dragon, player, hand);
            if (result.consumesAction()) {
                cir.setReturnValue(result);
            }
        }
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void petting$onSpawnItemLikeAtLocation(ServerLevel level, ItemLike itemLike, CallbackInfoReturnable<ItemEntity> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof FallingBlockEntity fallingBlock) {
            if (fallingBlock.getBlockState().is(PettingModBlocks.GOLDEN_DRAGON_EGG.get())) {
                ItemStack stack = new ItemStack(itemLike);
                if (fallingBlock.blockData != null) {
                    fallingBlock.blockData.read("OwnerUUID", UUIDUtil.CODEC).ifPresent(uuid -> {
                        stack.set(PettingDataComponents.DRAGON_EGG_OWNER.get(), uuid);
                    });
                    fallingBlock.blockData.getString("OwnerName").ifPresent(name -> {
                        stack.set(PettingDataComponents.DRAGON_EGG_OWNER_NAME.get(), name);
                    });
                }
                cir.setReturnValue(self.spawnAtLocation(level, stack, 0.0F));
            }
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("HEAD"), cancellable = true)
    private void petting$startRiding(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (self instanceof Player player && vehicle instanceof LivingEntity mob) {
            if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                if (!PetRidingEvents.canMount(mob, player)) {
                    cir.setReturnValue(false);
                    return;
                }
                PetRidingHandler.onMount(mob, player);
            }
        }
    }

    @Inject(method = "removePassenger", at = @At("HEAD"), cancellable = true)
    private void petting$removePassenger(Entity passenger, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (passenger instanceof Player player && self instanceof LivingEntity mob) {
            if (PetHelper.isTamed(mob) && PetHelper.isOwner(mob, player)) {
                if (PetRidingEvents.shouldCancelDismount(mob, player)) {
                    ci.cancel();
                    return;
                }
                PetRidingHandler.onDismount(mob);
            }
        }
    }
}
