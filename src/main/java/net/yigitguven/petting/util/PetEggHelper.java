package net.yigitguven.petting.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.yigitguven.petting.init.PettingModItems;
import net.yigitguven.petting.init.PettingModTags;

public class PetEggHelper {
    private static final Map<EntityType<?>, Item> CUSTOM_REPRESENTATIVE_ITEMS = new HashMap<>();

    static {
        CUSTOM_REPRESENTATIVE_ITEMS.put(EntityTypes.ENDER_DRAGON, Items.DRAGON_EGG);
    }

    private static Map<Item, TagKey<EntityType<?>>> treatTags;

    private static Map<Item, TagKey<EntityType<?>>> getTreatTags() {
        if (treatTags == null) {
            treatTags = Map.of(
                    PettingModItems.GOLDEN_WHEAT.get(), PettingModTags.EntityTypes.GOLDEN_WHEAT,
                    PettingModItems.GOLDEN_STAR.get(), PettingModTags.EntityTypes.GOLDEN_STAR,
                    PettingModItems.GOLDEN_FLESH.get(), PettingModTags.EntityTypes.GOLDEN_FLESH,
                    PettingModItems.GOLDEN_BONE.get(), PettingModTags.EntityTypes.GOLDEN_BONE,
                    PettingModItems.GOLDEN_FISH.get(), PettingModTags.EntityTypes.GOLDEN_FISH,
                    PettingModItems.GOLDEN_KELP.get(), PettingModTags.EntityTypes.GOLDEN_KELP,
                    PettingModItems.GOLDEN_FUNGUS.get(), PettingModTags.EntityTypes.GOLDEN_FUNGUS,
                    PettingModItems.GOLDEN_EYE.get(), PettingModTags.EntityTypes.GOLDEN_EYE,
                    PettingModItems.GOLDEN_SLIME.get(), PettingModTags.EntityTypes.GOLDEN_SLIME
            );
        }
        return treatTags;
    }

    public static void registerCustomRepresentative(EntityType<?> entityType, Item item) {
        CUSTOM_REPRESENTATIVE_ITEMS.put(entityType, item);
    }

    public static Item getRepresentativeItem(EntityType<?> entityType) {
        Item custom = CUSTOM_REPRESENTATIVE_ITEMS.get(entityType);
        if (custom != null) {
            return custom;
        }

        return SpawnEggItem.byId(entityType)
                .map(Holder::value)
                .orElse(Items.AIR);
    }

    public static Optional<TagKey<EntityType<?>>> getTagForTreat(Item treatItem) {
        return Optional.ofNullable(getTreatTags().get(treatItem));
    }

    public static boolean canTame(EntityType<?> entityType, Item treatItem) {
        TagKey<EntityType<?>> tag = getTreatTags().get(treatItem);
        return tag != null && entityType.builtInRegistryHolder().is(tag);
    }

    public static List<ItemStack> getRepresentativeItemsForTreat(Registry<EntityType<?>> entityTypeRegistry, TagKey<EntityType<?>> treatTag) {
        List<ItemStack> items = new ArrayList<>();
        for (Holder<EntityType<?>> holder : entityTypeRegistry.getTagOrEmpty(treatTag)) {
            Item repItem = getRepresentativeItem(holder.value());
            if (repItem != Items.AIR) {
                items.add(new ItemStack(repItem));
            }
        }
        return items;
    }
}
