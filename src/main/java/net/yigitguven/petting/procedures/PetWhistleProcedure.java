package net.yigitguven.petting.procedures;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yigitguven.petting.IEntityData;
import net.yigitguven.petting.PetAttackLogic;
import net.yigitguven.petting.config.PettingConfig;

public class PetWhistleProcedure {

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (!world.isClientSide() && player.isShiftKeyDown() && PettingConfig.enableGoatHornWhistle) {
                if (stack.getItem() == Items.GOAT_HORN) {
                    execute(player);
                }
            }
            return InteractionResultHolder.pass(stack);
        });
    }

    private static void execute(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            int summonedCount = 0;
            String playerUUID = player.getStringUUID();

            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Mob pet && PetAttackLogic.isCustomPet(pet)) {
                    String ownerUUID = ((IEntityData) pet).getPersistentData().getString("ownerUUID");
                    if (ownerUUID.equals(playerUUID)) {
                        boolean ignoreWhistle = ((IEntityData) pet).getPersistentData().getBoolean("ignoreWhistle");
                        if (ignoreWhistle) continue;

                        boolean isBound = ((IEntityData) pet).getPersistentData().getBoolean("pettingbound");
                        if (isBound && !PettingConfig.whistleTeleportsTethered) continue;

                        pet.teleportTo(player.getX(), player.getY(), player.getZ());
                        ((IEntityData) pet).getPersistentData().putBoolean("waiting", false);
                        ((IEntityData) pet).getPersistentData().putBoolean("sitstill", false);

                        serverLevel.sendParticles(ParticleTypes.PORTAL, 
                            pet.getX(), pet.getY() + 1.0D, pet.getZ(), 
                            20, 0.5, 0.5, 0.5, 0.1);
                        summonedCount++;
                    }
                }
            }

            if (summonedCount > 0) {
                player.displayClientMessage(Component.literal("§aSummoned " + summonedCount + " pet(s) to your location!"), true);
                serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            } else {
                player.displayClientMessage(Component.literal("§cYou have no custom pets in this dimension to summon."), true);
            }
        }
    }
}
