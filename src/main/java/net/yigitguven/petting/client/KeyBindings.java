package net.yigitguven.petting.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public class KeyBindings {
    public static net.minecraft.client.KeyMapping OPEN_PET_SETTINGS;

    public static void register() {
        OPEN_PET_SETTINGS = KeyBindingHelper.registerKeyBinding(new net.minecraft.client.KeyMapping(
            "controls.petting.open_settings", 
            org.lwjgl.glfw.GLFW.GLFW_KEY_P, 
            "key.categories.petting"
        ));
    }
}
