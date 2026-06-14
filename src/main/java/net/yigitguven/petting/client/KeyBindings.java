package net.yigitguven.petting.client;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.yigitguven.petting.PettingMod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = PettingMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping OPEN_PET_SETTINGS = new KeyMapping("controls.petting.open_settings", GLFW.GLFW_KEY_P, "key.categories.petting");

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_PET_SETTINGS);
    }
}
