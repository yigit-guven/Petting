package net.yigitguven.petting.init;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final RegistrySupplier<MenuType<PetInventoryMenu>> PET_MENU = register(
            "pet_inventory",
            new ExtendedMenuType<>(PetInventoryMenu::new, ByteBufCodecs.VAR_INT)
    );

    private static <T extends MenuType<?>> RegistrySupplier<T> register(String name, T menu) {
        Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Petting.MODID, name), menu);
        return new RegistrySupplier<>(menu);
    }

    public static void register() {
    }
}
