package net.yigitguven.petting.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.Petting;
import net.yigitguven.petting.inventory.PetInventoryMenu;

public class PettingModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Petting.MODID);

    public static final RegistryObject<MenuType<PetInventoryMenu>> PET_MENU =
            MENUS.register("pet_inventory", () -> IForgeMenuType.create(PetInventoryMenu::new));
}
