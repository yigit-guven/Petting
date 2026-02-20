package net.yigitguven.petting;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.yigitguven.petting.config.PettingConfig;
import net.yigitguven.petting.init.PettingModAttributes;

import java.util.List;

public class PlayerAttributeSyncHandler {

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof Player player && !world.isClientSide()) {
                syncAttributes(player);
            }
        });
    }

    private static void syncAttributes(Player player) {
        // Sync Global Max Pets
        AttributeInstance globalAttr = player.getAttribute(PettingModAttributes.MAX_PETS);
        if (globalAttr != null) {
            globalAttr.setBaseValue((double) PettingConfig.maxPetsPerPlayer);
        }

        // Sync Category Limits
        List<String> categoryConfigs = PettingConfig.petCategories;
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

    private static AttributeInstance getAttributeForSlot(Player player, int slot) {
        if (slot >= 1 && slot <= 20) {
            return player.getAttribute(PettingModAttributes.MAX_PETS_CATEGORIES[slot - 1]);
        }
        return null;
    }
}
