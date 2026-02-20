package net.yigitguven.petting.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.yigitguven.petting.PettingMod;

public class PettingModAttributes {
    public static final Attribute MAX_PETS = new RangedAttribute("attribute.name.petting.max_pets", 1.0D, 0.0D, 1024.0D).setSyncable(true);
    
    public static final Attribute[] MAX_PETS_CATEGORIES = new Attribute[20];

    static {
        for (int i = 0; i < 20; i++) {
            MAX_PETS_CATEGORIES[i] = new RangedAttribute("attribute.name.petting.max_pets_category_" + (i + 1), 0.0D, 0.0D, 1024.0D).setSyncable(true);
        }
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(PettingMod.MODID, "max_pets"), MAX_PETS);
        for (int i = 0; i < 20; i++) {
            Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation(PettingMod.MODID, "max_pets_category_" + (i + 1)), MAX_PETS_CATEGORIES[i]);
        }
    }
}
