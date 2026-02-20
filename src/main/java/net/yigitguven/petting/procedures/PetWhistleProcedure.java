package net.yigitguven.petting.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yigitguven.petting.config.PettingConfig;

import java.util.List;

@Mod.EventBusSubscriber
public class PetWhistleProcedure {
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isShiftKeyDown()) return;

        if (!PettingConfig.ENABLE_GOAT_HORN_WHISTLE.get()) return;

        if (event.getItemStack().getItem() == net.minecraft.world.item.Items.GOAT_HORN) {
            if (player.level() instanceof ServerLevel serverLevel) {
                int summonedCount = 0;
                String playerUUID = player.getStringUUID();

                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof Mob pet) {
                        if (pet.getPersistentData().getBoolean("pettingtamed")) {
                            String ownerUUID = pet.getPersistentData().getString("ownerUUID");
                            if (ownerUUID.equals(playerUUID)) {
                                // Teleport pet to owner
                                pet.teleportTo(player.getX(), player.getY(), player.getZ());
                                
                                // Reset its waiting state to ensure it follows again
                                pet.getPersistentData().putBoolean("waiting", false);
                                pet.getPersistentData().putBoolean("sitstill", false);

                                // Visual feedback
                                serverLevel.sendParticles(ParticleTypes.PORTAL, 
                                    pet.getX(), pet.getY() + 1.0D, pet.getZ(), 
                                    20, 0.5, 0.5, 0.5, 0.1);
                                
                                summonedCount++;
                            }
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
}
