package net.yigitguven.petting;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.init.PettingModAttributes;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerAttributeSyncHandler {
    @SubscribeEvent
    public static void onPlayerJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
            // Sync Global Max Pets
            AttributeInstance globalAttr = player.getAttribute(PettingModAttributes.MAX_PETS.get());
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
            case 1 -> player.getAttribute(PettingModAttributes.MAX_PETS_C1.get());
            case 2 -> player.getAttribute(PettingModAttributes.MAX_PETS_C2.get());
            case 3 -> player.getAttribute(PettingModAttributes.MAX_PETS_C3.get());
            case 4 -> player.getAttribute(PettingModAttributes.MAX_PETS_C4.get());
            case 5 -> player.getAttribute(PettingModAttributes.MAX_PETS_C5.get());
            case 6 -> player.getAttribute(PettingModAttributes.MAX_PETS_C6.get());
            case 7 -> player.getAttribute(PettingModAttributes.MAX_PETS_C7.get());
            case 8 -> player.getAttribute(PettingModAttributes.MAX_PETS_C8.get());
            case 9 -> player.getAttribute(PettingModAttributes.MAX_PETS_C9.get());
            case 10 -> player.getAttribute(PettingModAttributes.MAX_PETS_C10.get());
            case 11 -> player.getAttribute(PettingModAttributes.MAX_PETS_C11.get());
            case 12 -> player.getAttribute(PettingModAttributes.MAX_PETS_C12.get());
            case 13 -> player.getAttribute(PettingModAttributes.MAX_PETS_C13.get());
            case 14 -> player.getAttribute(PettingModAttributes.MAX_PETS_C14.get());
            case 15 -> player.getAttribute(PettingModAttributes.MAX_PETS_C15.get());
            case 16 -> player.getAttribute(PettingModAttributes.MAX_PETS_C16.get());
            case 17 -> player.getAttribute(PettingModAttributes.MAX_PETS_C17.get());
            case 18 -> player.getAttribute(PettingModAttributes.MAX_PETS_C18.get());
            case 19 -> player.getAttribute(PettingModAttributes.MAX_PETS_C19.get());
            case 20 -> player.getAttribute(PettingModAttributes.MAX_PETS_C20.get());
            default -> null;
        };
    }
}
