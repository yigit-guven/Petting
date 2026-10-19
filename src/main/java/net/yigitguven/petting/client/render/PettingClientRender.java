package net.yigitguven.petting.client.render;

import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class PettingClientRender {

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            try {
                LivingEntityRenderer renderer = event.getRenderer((EntityType) type);
                if (renderer != null) {
                    renderer.addLayer(new PetCollarLayer(renderer));
                }
            } catch (Exception ignored) {}
        }

        for (String skin : event.getSkins()) {
            try {
                LivingEntityRenderer renderer = event.getSkin(skin);
                if (renderer != null) {
                    renderer.addLayer(new PetCollarLayer(renderer));
                }
            } catch (Exception ignored) {}
        }
    }
}
