package net.yigitguven.petting.compat.jei;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.yigitguven.petting.util.PetEggHelper;

import java.util.List;

public class PetTamingRecipe {
    private final ItemStack treat;
    private final TagKey<EntityType<?>> treatTag;
    private List<ItemStack> cachedMobs;

    public PetTamingRecipe(ItemStack treat, TagKey<EntityType<?>> treatTag) {
        this.treat = treat;
        this.treatTag = treatTag;
    }

    public ItemStack treat() {
        return treat;
    }

    public TagKey<EntityType<?>> treatTag() {
        return treatTag;
    }

    public List<ItemStack> tameableMobs() {
        if (cachedMobs == null) {
            cachedMobs = List.copyOf(PetEggHelper.getRepresentativeItemsForTreat(BuiltInRegistries.ENTITY_TYPE, treatTag));
        }
        return cachedMobs;
    }
}
