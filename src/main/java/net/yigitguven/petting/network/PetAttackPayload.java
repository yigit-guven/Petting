package net.yigitguven.petting.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.yigitguven.petting.PettingMod;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import net.yigitguven.petting.config.PettingConfig;
import net.minecraft.core.registries.BuiltInRegistries;

public record PetAttackPayload() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PetAttackPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(PettingMod.MODID, "pet_attack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetAttackPayload> STREAM_CODEC = StreamCodec.unit(new PetAttackPayload());

    @Override
    public CustomPacketPayload.Type<PetAttackPayload> type() {
        return TYPE;
    }

    public static void handle(final PetAttackPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) context.player();
            if (player != null && player.getVehicle() != null && PettingConfig.ALLOW_PET_ATTACK_WHILE_RIDING.get()) {
                Entity pet = player.getVehicle();
                
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
                    ResourceLocation petType = BuiltInRegistries.ENTITY_TYPE.getKey(pet.getType());
                    if (petType == null) return;
                    String petId = petType.toString();

                    if (petId.equals("minecraft:ender_dragon")) {
                        DragonFireball fireball = new DragonFireball(player.level(), (LivingEntity)pet, new Vec3(look.x, look.y, look.z));
                        fireball.setPos(pet.getX(), pet.getY() + pet.getBbHeight() / 2.0, pet.getZ());
                        player.level().addFreshEntity(fireball);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    } else if (petId.equals("minecraft:wither")) {
                        WitherSkull skull = new WitherSkull(player.level(), (LivingEntity)pet, new Vec3(look.x, look.y, look.z));
                        skull.setPos(pet.getX(), pet.getY() + pet.getBbHeight() / 2.0, pet.getZ());
                        if (player.getRandom().nextFloat() < 0.2F) {
                            skull.setDangerous(true);
                        }
                        player.level().addFreshEntity(skull);
                        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                    }
                }
            }
        });
    }
}
