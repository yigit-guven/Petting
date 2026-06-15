package net.yigitguven.petting.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.SimpleMenuProvider;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.config.PettingConfig;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;
import net.minecraft.core.registries.BuiltInRegistries;

public class PettingServerNetworking {
    public static final ResourceLocation CANCEL_BINDING = new ResourceLocation(PettingMod.MODID, "cancel_binding");
    public static final ResourceLocation OPEN_PET_INVENTORY = new ResourceLocation(PettingMod.MODID, "open_pet_inventory");
    public static final ResourceLocation OPEN_PET_SETTINGS = new ResourceLocation(PettingMod.MODID, "open_pet_settings");
    public static final ResourceLocation PET_ATTACK = new ResourceLocation(PettingMod.MODID, "pet_attack");
    public static final ResourceLocation SAVE_PET_CONTROL_DEFAULTS = new ResourceLocation(PettingMod.MODID, "save_pet_control_defaults");
    public static final ResourceLocation UPDATE_PET_SETTING = new ResourceLocation(PettingMod.MODID, "update_pet_setting");

    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CANCEL_BINDING, (server, player, handler, buf, responseSender) -> {
            // PetBedBindingHandler.handleMenuCancellation is not available in Fabric port yet.
        });

        ServerPlayNetworking.registerGlobalReceiver(OPEN_PET_INVENTORY, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.getVehicle() != null) {
                    Entity pet = player.getVehicle();
                    boolean isOwner = false;
                    if (((net.yigitguven.petting.IEntityData)pet).getPersistentData().contains("ownerUUID") && ((net.yigitguven.petting.IEntityData)pet).getPersistentData().getString("ownerUUID").equals(player.getStringUUID())) {
                        isOwner = true;
                    } else if (pet instanceof net.minecraft.world.entity.TamableAnimal tamable) {
                        if (tamable.isOwnedBy(player)) isOwner = true;
                    }

                    if (isOwner || ((net.yigitguven.petting.IEntityData)pet).getPersistentData().getBoolean("pettingtamed")) {
                        // For ScreenHandler sync, Fabric needs ExtendedScreenHandlerFactory to send data
                        player.openMenu(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory() {
                            @Override
                            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
                                buf.writeInt(pet.getId());
                            }

                            @Override
                            public net.minecraft.network.chat.Component getDisplayName() {
                                return pet.getDisplayName();
                            }

                            @Override
                            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int syncId, net.minecraft.world.entity.player.Inventory inv, net.minecraft.world.entity.player.Player player) {
                                return new PetInventoryMenu(syncId, inv, pet);
                            }
                        });
                    }
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(OPEN_PET_SETTINGS, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            server.execute(() -> {
                EntitySettingsServer.handleOpenRequest(player, entityId);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(PET_ATTACK, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.getVehicle() != null) {
                    Entity pet = player.getVehicle();
                    boolean isOwner = false;
                    if (((net.yigitguven.petting.IEntityData)pet).getPersistentData().getBoolean("pettingtamed")) {
                        String ownerUUID = ((net.yigitguven.petting.IEntityData)pet).getPersistentData().getString("ownerUUID");
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
                            DragonFireball fireball = new DragonFireball(player.level(), (LivingEntity)pet, look.x, look.y, look.z);
                            fireball.setPos(pet.getX(), pet.getY() + pet.getBbHeight() / 2.0, pet.getZ());
                            player.level().addFreshEntity(fireball);
                            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
                        } else if (petId.equals("minecraft:wither")) {
                            WitherSkull skull = new WitherSkull(player.level(), (LivingEntity)pet, look.x, look.y, look.z);
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
        });

        ServerPlayNetworking.registerGlobalReceiver(SAVE_PET_CONTROL_DEFAULTS, (server, player, handler, buf, responseSender) -> {
            String rightClick = buf.readUtf(32767);
            String shiftRightClick = buf.readUtf(32767);
            server.execute(() -> {
                EntitySettingsServer.saveControlDefaults(player, rightClick, shiftRightClick);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_PET_SETTING, (server, player, handler, buf, responseSender) -> {
            int entityId = buf.readInt();
            String key = buf.readUtf(32767);
            String value = buf.readUtf(32767);
            server.execute(() -> {
                EntitySettingsServer.applyUpdate(player, entityId, key, value);
            });
        });
    }
}
