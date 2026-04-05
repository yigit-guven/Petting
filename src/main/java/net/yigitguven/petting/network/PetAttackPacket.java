package net.yigitguven.petting.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.yigitguven.petting.config.PettingConfig;

import java.util.function.Supplier;

public class PetAttackPacket {
    public PetAttackPacket() {
    }

    public PetAttackPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.getVehicle() != null && PettingConfig.ALLOW_PET_ATTACK_WHILE_RIDING.get()) {
                Entity pet = player.getVehicle();
                
                // Ownership check
                boolean isOwner = false;
                if (pet.getPersistentData().getString("ownerUUID").equals(player.getStringUUID())) {
                    isOwner = true;
                } else if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable && tamable.isOwnedBy(player)) {
                    isOwner = true;
                }

                if (isOwner) {
                    // Use pet's rotation to find the head position
                    float yaw = pet.getYRot();
                    float pitch = pet.getXRot();
                    Vec3 lookRel = Vec3.directionFromRotation(pitch, yaw);
                    Vec3 lookAction = player.getLookAngle();

                    if (pet instanceof EnderDragon) {
                        double offset = 8.0;
                        double x = pet.getX() + lookRel.x * offset;
                        double y = pet.getY() + pet.getEyeHeight() / 2 + lookRel.y * offset;
                        double z = pet.getZ() + lookRel.z * offset;

                        DragonFireball fireball = new DragonFireball(player.level(), (LivingEntity)pet, lookAction.x, lookAction.y, lookAction.z);
                        fireball.setPos(x, y, z);
                        player.level().addFreshEntity(fireball);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    } else if (pet instanceof WitherBoss) {
                        double offset = 3.0;
                        double x = pet.getX() + lookRel.x * offset;
                        double y = pet.getY() + pet.getEyeHeight() + lookRel.y * offset;
                        double z = pet.getZ() + lookRel.z * offset;

                        WitherSkull skull = new WitherSkull(player.level(), (LivingEntity)pet, lookAction.x, lookAction.y, lookAction.z);
                        skull.setPos(x, y, z);
                        if (player.getRandom().nextFloat() < 0.2F) {
                            skull.setDangerous(true);
                        }
                        player.level().addFreshEntity(skull);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
