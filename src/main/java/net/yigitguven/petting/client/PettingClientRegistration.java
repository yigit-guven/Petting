package net.yigitguven.petting.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.bus.api.IEventBus;

public class PettingClientRegistration {
    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, screen) -> new ConfigGuiScreen(screen));
    }

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(PettingClientRegistration::registerScreens);
        net.yigitguven.petting.client.KeyBindings.register();
        startKeyPollingThread();
    }

    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(net.yigitguven.petting.init.PettingModMenus.PET_INVENTORY.get(), net.yigitguven.petting.client.gui.PetInventoryScreen::new);
    }

    private static void startKeyPollingThread() {
        Thread t = new Thread(() -> {
            boolean prev = false;
            while (true) {
                try {
                    Thread.sleep(50);
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc == null || mc.level == null) continue;

                    boolean pressed = false;
                    try {
                        if (net.yigitguven.petting.client.KeyBindings.OPEN_PET_SETTINGS != null) {
                            pressed = net.yigitguven.petting.client.KeyBindings.OPEN_PET_SETTINGS.isDown();
                        } else {
                            long window = mc.getWindow().getWindow();
                            pressed = org.lwjgl.glfw.GLFW.glfwGetKey(window, org.lwjgl.glfw.GLFW.GLFW_KEY_P) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
                        }
                    } catch (Throwable ignored) {
                    }

                    if (pressed && !prev) {
                        var hit = mc.hitResult;
                        if (hit instanceof net.minecraft.world.phys.EntityHitResult ehr) {
                            var ent = ehr.getEntity();
                            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new net.yigitguven.petting.network.OpenPetSettingsPayload(ent.getId()));
                        }
                    }
                    prev = pressed;
                } catch (InterruptedException e) {
                    return;
                } catch (Throwable t1) {
                    // swallow errors to keep thread alive
                }
            }
        }, "petting-keypoll");
        t.setDaemon(true);
        t.start();
    }

    
}
