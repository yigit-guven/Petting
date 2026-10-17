package net.yigitguven.petting.client.render;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yigitguven.petting.Petting;

public class PettingClientRender {

    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            EntityRenderer<?> renderer = event.getRenderer(type);
            if (renderer instanceof LivingEntityRenderer livingRenderer) {
                attachLayer(livingRenderer);
            }
        }

        for (PlayerSkin.Model skin : event.getSkins()) {
            EntityRenderer<?> playerRenderer = event.getSkin(skin);
            if (playerRenderer instanceof LivingEntityRenderer livingRenderer) {
                attachLayer(livingRenderer);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void attachLayer(LivingEntityRenderer livingRenderer) {
        try {
            livingRenderer.addLayer(new PetCollarLayer(livingRenderer));
        } catch (Throwable t) {
            Petting.LOGGER.error("Failed to attach PetCollarLayer to {}", livingRenderer.getClass().getSimpleName(), t);
        }
    }
}
