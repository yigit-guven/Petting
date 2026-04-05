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
                
                // Improved logic: If mob is pettingtamed and we have a valid ownerUUID matching the player
                boolean isOwner = false;
                if (pet.getPersistentData().getBoolean("pettingtamed")) {
                    String ownerUUID = pet.getPersistentData().getString("ownerUUID");
                    if (ownerUUID.isEmpty() || ownerUUID.equals(player.getStringUUID())) {
                        isOwner = true;
                    }
                } else if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable && tamable.isOwnedBy(player)) {
                    isOwner = true;
                }

                if (isOwner) {
                    Vec3 look = player.getLookAngle();
                    
                    if (pet instanceof EnderDragon) {
                        // Ender Dragon firing (distance 10 ensures it's past the long neck/head)
                        double distance = 10.0;
                        double x = pet.getX() + look.x * distance;
                        double y = pet.getY() + pet.getEyeHeight() / 2 + look.y * distance;
                        double z = pet.getZ() + look.z * distance;

                        DragonFireball fireball = new DragonFireball(player.level(), (LivingEntity)pet, look.x, look.y, look.z);
                        fireball.setPos(x, y, z);
                        player.level().addFreshEntity(fireball);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    } else if (pet instanceof WitherBoss) {
                        // Wither firing (distance 4 ensures it's past the heads)
                        double distance = 4.0;
                        double x = pet.getX() + look.x * distance;
                        double y = pet.getY() + pet.getEyeHeight() + look.y * distance;
                        double z = pet.getZ() + look.z * distance;

                        WitherSkull skull = new WitherSkull(player.level(), (LivingEntity)pet, look.x, look.y, look.z);
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
