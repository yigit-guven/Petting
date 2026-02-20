package net.yigitguven.petting.init;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.yigitguven.petting.PettingMod;

public class PettingModAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PettingMod.MODID);

    public static final RegistryObject<Attribute> MAX_PETS = REGISTRY.register("max_pets", 
        () -> new RangedAttribute("attribute.name.petting.max_pets", 1.0D, -1.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_PETS_C1 = REGISTRY.register("max_pets_category_1", () -> createCategoryAttr(1));
    public static final RegistryObject<Attribute> MAX_PETS_C2 = REGISTRY.register("max_pets_category_2", () -> createCategoryAttr(2));
    public static final RegistryObject<Attribute> MAX_PETS_C3 = REGISTRY.register("max_pets_category_3", () -> createCategoryAttr(3));
    public static final RegistryObject<Attribute> MAX_PETS_C4 = REGISTRY.register("max_pets_category_4", () -> createCategoryAttr(4));
    public static final RegistryObject<Attribute> MAX_PETS_C5 = REGISTRY.register("max_pets_category_5", () -> createCategoryAttr(5));
    public static final RegistryObject<Attribute> MAX_PETS_C6 = REGISTRY.register("max_pets_category_6", () -> createCategoryAttr(6));
    public static final RegistryObject<Attribute> MAX_PETS_C7 = REGISTRY.register("max_pets_category_7", () -> createCategoryAttr(7));
    public static final RegistryObject<Attribute> MAX_PETS_C8 = REGISTRY.register("max_pets_category_8", () -> createCategoryAttr(8));
    public static final RegistryObject<Attribute> MAX_PETS_C9 = REGISTRY.register("max_pets_category_9", () -> createCategoryAttr(9));
    public static final RegistryObject<Attribute> MAX_PETS_C10 = REGISTRY.register("max_pets_category_10", () -> createCategoryAttr(10));
    public static final RegistryObject<Attribute> MAX_PETS_C11 = REGISTRY.register("max_pets_category_11", () -> createCategoryAttr(11));
    public static final RegistryObject<Attribute> MAX_PETS_C12 = REGISTRY.register("max_pets_category_12", () -> createCategoryAttr(12));
    public static final RegistryObject<Attribute> MAX_PETS_C13 = REGISTRY.register("max_pets_category_13", () -> createCategoryAttr(13));
    public static final RegistryObject<Attribute> MAX_PETS_C14 = REGISTRY.register("max_pets_category_14", () -> createCategoryAttr(14));
    public static final RegistryObject<Attribute> MAX_PETS_C15 = REGISTRY.register("max_pets_category_15", () -> createCategoryAttr(15));
    public static final RegistryObject<Attribute> MAX_PETS_C16 = REGISTRY.register("max_pets_category_16", () -> createCategoryAttr(16));
    public static final RegistryObject<Attribute> MAX_PETS_C17 = REGISTRY.register("max_pets_category_17", () -> createCategoryAttr(17));
    public static final RegistryObject<Attribute> MAX_PETS_C18 = REGISTRY.register("max_pets_category_18", () -> createCategoryAttr(18));
    public static final RegistryObject<Attribute> MAX_PETS_C19 = REGISTRY.register("max_pets_category_19", () -> createCategoryAttr(19));
    public static final RegistryObject<Attribute> MAX_PETS_C20 = REGISTRY.register("max_pets_category_20", () -> createCategoryAttr(20));

    private static Attribute createCategoryAttr(int index) {
        return new RangedAttribute("attribute.name.petting.max_pets_category_" + index, 0.0D, 0.0D, 1024.0D).setSyncable(true);
    }
}
