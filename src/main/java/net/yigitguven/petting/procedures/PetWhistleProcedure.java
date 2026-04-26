package net.yigitguven.petting.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.yigitguven.petting.config.PettingConfig;

@EventBusSubscriber
public class PetWhistleProcedure {
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isShiftKeyDown()) return;

        if (!PettingConfig.ENABLE_GOAT_HORN_WHISTLE.get()) return;

        net.minecraft.world.item.Item whistleItem = net.yigitguven.petting.util.PetInventoryUtil.getItemFromID(PettingConfig.GLOBAL_WHISTLE_TOOL_ID.get(), net.minecraft.world.item.Items.GOAT_HORN);
        if (event.getItemStack().getItem() == whistleItem) {
            if (player.level() instanceof ServerLevel serverLevel) {
                int summonedCount = 0;
                String playerUUID = player.getStringUUID();

                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity instanceof Mob pet) {
                        if (net.yigitguven.petting.util.PetInventoryUtil.isBlacklisted(pet)) continue;

                        if (pet.getPersistentData().getBoolean("pettingtamed")) {
                            String ownerUUID = pet.getPersistentData().getString("ownerUUID");
                            if (ownerUUID.equals(playerUUID)) {
                                boolean ignoreWhistle = pet.getPersistentData().getBoolean("ignoreWhistle");
                                if (ignoreWhistle) {
                                    continue;
                                }

                                boolean isBound = pet.getPersistentData().getBoolean("pettingbound");
                                if (isBound && !PettingConfig.WHISTLE_TELEPORTS_TETHERED.get()) {
                                    continue;
                                }

                                pet.teleportTo(player.getX(), player.getY(), player.getZ());
                                
                                pet.getPersistentData().putBoolean("waiting", false);
                                pet.getPersistentData().putBoolean("sitstill", false);

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
