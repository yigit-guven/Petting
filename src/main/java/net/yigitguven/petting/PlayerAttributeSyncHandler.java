package net.yigitguven.petting;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.init.PettingModAttributes;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME)
public class PlayerAttributeSyncHandler {
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
            // Sync Global Max Pets
            AttributeInstance globalAttr = player.getAttribute(PettingModAttributes.MAX_PETS);
            if (globalAttr != null) {
                globalAttr.setBaseValue((double) PettingConfig.MAX_PETS_PER_PLAYER.get());
            }

            // Sync Category Limits
            List<? extends String> categoryConfigs = PettingConfig.PET_CATEGORIES.get();
            for (String config : categoryConfigs) {
                String[] parts = config.split("\\|");
                if (parts.length >= 4) {
                    try {
                        int slot = Integer.parseInt(parts[0].trim());
                        double defaultLimit = Double.parseDouble(parts[3].trim());
                        
                        AttributeInstance catAttr = getAttributeForSlot(player, slot);
                        if (catAttr != null) {
                            catAttr.setBaseValue(defaultLimit);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    private static AttributeInstance getAttributeForSlot(Player player, int slot) {
        return switch (slot) {
            case 1 -> player.getAttribute(PettingModAttributes.MAX_PETS_C1);
            case 2 -> player.getAttribute(PettingModAttributes.MAX_PETS_C2);
            case 3 -> player.getAttribute(PettingModAttributes.MAX_PETS_C3);
            case 4 -> player.getAttribute(PettingModAttributes.MAX_PETS_C4);
            case 5 -> player.getAttribute(PettingModAttributes.MAX_PETS_C5);
            case 6 -> player.getAttribute(PettingModAttributes.MAX_PETS_C6);
            case 7 -> player.getAttribute(PettingModAttributes.MAX_PETS_C7);
            case 8 -> player.getAttribute(PettingModAttributes.MAX_PETS_C8);
            case 9 -> player.getAttribute(PettingModAttributes.MAX_PETS_C9);
            case 10 -> player.getAttribute(PettingModAttributes.MAX_PETS_C10);
            case 11 -> player.getAttribute(PettingModAttributes.MAX_PETS_C11);
            case 12 -> player.getAttribute(PettingModAttributes.MAX_PETS_C12);
            case 13 -> player.getAttribute(PettingModAttributes.MAX_PETS_C13);
            case 14 -> player.getAttribute(PettingModAttributes.MAX_PETS_C14);
            case 15 -> player.getAttribute(PettingModAttributes.MAX_PETS_C15);
            case 16 -> player.getAttribute(PettingModAttributes.MAX_PETS_C16);
            case 17 -> player.getAttribute(PettingModAttributes.MAX_PETS_C17);
            case 18 -> player.getAttribute(PettingModAttributes.MAX_PETS_C18);
            case 19 -> player.getAttribute(PettingModAttributes.MAX_PETS_C19);
            case 20 -> player.getAttribute(PettingModAttributes.MAX_PETS_C20);
            default -> null;
        };
    }
}



