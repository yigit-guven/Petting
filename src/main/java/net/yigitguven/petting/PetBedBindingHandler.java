package net.yigitguven.petting;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;

public class PetBedBindingHandler {

    private static final String TAG_BINDING_MODE = "PettingBindingMode";
    private static final String TAG_BED_X = "PettingTempBedX";
    private static final String TAG_BED_Y = "PettingTempBedY";
    private static final String TAG_BED_Z = "PettingTempBedZ";

    public static void register() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock());
            String blockIdStr = (blockId != null) ? blockId.toString() : "";

            IEntityData dataAccess = (IEntityData) player;
            CompoundTag playerNBT = dataAccess.getPersistentData();
            boolean isBinding = playerNBT.getBoolean(TAG_BINDING_MODE);

            if (blockIdStr.contains("pet_bed")) {
                playerNBT.putBoolean(TAG_BINDING_MODE, true);
                playerNBT.putDouble(TAG_BED_X, pos.getX());
                playerNBT.putDouble(TAG_BED_Y, pos.getY());
                playerNBT.putDouble(TAG_BED_Z, pos.getZ());

                player.displayClientMessage(Component.literal("§a[Petting] §fBinding Mode Active!"), true);
                player.sendSystemMessage(Component.literal("§eRight-click a tamed pet to bind it to this bed."));
                player.sendSystemMessage(Component.literal("§7(Right-click air or ground to cancel)"));
                
                world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.5f, 1.0f);
                return InteractionResult.SUCCESS; 
            }

            if (isBinding) {
                clearBindingState(player);
                player.displayClientMessage(Component.literal("§c[Petting] Binding Cancelled."), true);
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;

            IEntityData playerData = (IEntityData) player;
            CompoundTag playerNBT = playerData.getPersistentData();

            if (!playerNBT.getBoolean(TAG_BINDING_MODE)) {
                return InteractionResult.PASS;
            }

            boolean validPet = false;
            if (entity instanceof Mob mob) {
                CompoundTag petNBT = ((IEntityData) mob).getPersistentData();
                if (petNBT.getBoolean("pettingtamed")) {
                    String ownerUUIDStr = petNBT.getString("ownerUUID");
                    if (!ownerUUIDStr.isEmpty() && ownerUUIDStr.equals(player.getStringUUID())) {
                        validPet = true;
                    }
                }
            }

            if (validPet) {
                double bedX = playerNBT.getDouble(TAG_BED_X);
                double bedY = playerNBT.getDouble(TAG_BED_Y);
                double bedZ = playerNBT.getDouble(TAG_BED_Z);

                CompoundTag petNBT = ((IEntityData) entity).getPersistentData();
                petNBT.putDouble("pet_bed_loc_x", bedX);
                petNBT.putDouble("pet_bed_loc_y", bedY);
                petNBT.putDouble("pet_bed_loc_z", bedZ);

                player.sendSystemMessage(Component.literal("§a[Petting] §fSuccessfully bound " + entity.getName().getString() + " to the bed!"));
                world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                if (world instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, 
                        entity.getX(), entity.getY() + 0.5, entity.getZ(), 
                        10, 0.5, 0.5, 0.5, 0.1);
                }
            } else {
                player.sendSystemMessage(Component.literal("§c[Petting] Cancelled. That is not your custom pet!"));
            }

            clearBindingState(player);
            return InteractionResult.SUCCESS;
        });
    }

    private static void clearBindingState(Player player) {
        CompoundTag nbt = ((IEntityData) player).getPersistentData();
        nbt.remove(TAG_BINDING_MODE);
        nbt.remove(TAG_BED_X);
        nbt.remove(TAG_BED_Y);
        nbt.remove(TAG_BED_Z);
    }
}
