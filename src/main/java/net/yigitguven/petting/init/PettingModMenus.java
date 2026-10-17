package net.yigitguven.petting.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Petting.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<PetInventoryMenu>> PET_MENU =
            MENUS.register("pet_inventory", () -> IMenuTypeExtension.create(PetInventoryMenu::new));
}
