package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.yigitguven.petting.PettingMod;
import net.yigitguven.petting.world.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static MenuType<PetInventoryMenu> PET_INVENTORY;

    public static void register() {
        PET_INVENTORY = Registry.register(
            BuiltInRegistries.MENU,
            new ResourceLocation(PettingMod.MODID, "pet_inventory"),
            new ExtendedScreenHandlerType<>((syncId, inventory, buf) -> new PetInventoryMenu(syncId, inventory, buf))
        );
    }
}
