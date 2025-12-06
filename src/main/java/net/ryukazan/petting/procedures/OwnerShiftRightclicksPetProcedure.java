package net.ryukazan.petting.procedures;

import net.ryukazan.petting.world.inventory.PetConfigurationGUIMenu;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

@EventBusSubscriber
public class OwnerShiftRightclicksPetProcedure {
    @SubscribeEvent
    public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND)
            return;
        execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getTarget(), event.getEntity());
    }

    public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        execute(null, world, x, y, z, entity, sourceentity);
    }

    private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
        if (entity == null || sourceentity == null)
            return;
        
        // Check ownership
        if ((entity.getPersistentData().getStringOr("ownerUUID", "")).equals(sourceentity.getStringUUID())) {
            
            if (sourceentity.isShiftKeyDown()) {
                // Save server side just in case
                sourceentity.getPersistentData().putString("configUUID", (entity.getStringUUID()));
                entity.getPersistentData().putBoolean("sitstill", true);
                
                if (sourceentity instanceof ServerPlayer _ent) {
                    BlockPos _bpos = BlockPos.containing(x, y, z);
                    
                    _ent.openMenu(new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.literal("PetConfigurationGUI");
                        }

                        @Override
                        public boolean shouldTriggerClientSideContainerClosingOnOpen() {
                            return false;
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                            // FIX: Write the BlockPos AND the UUID to the buffer
                            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
                            buffer.writeBlockPos(_bpos);
                            buffer.writeUtf(entity.getStringUUID()); // <--- SEND UUID TO CLIENT
                            
                            return new PetConfigurationGUIMenu(id, inventory, buffer);
                        }
                    }, _bpos);
                }
            }
        }
    }
}