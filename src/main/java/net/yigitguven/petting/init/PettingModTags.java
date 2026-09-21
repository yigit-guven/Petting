package net.yigitguven.petting.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.yigitguven.petting.Petting;

public class PettingModTags {
    public static class EntityTypes {
        public static final TagKey<EntityType<?>> GOLDEN_WHEAT = tag("tames_with/golden_wheat");
        public static final TagKey<EntityType<?>> GOLDEN_STAR = tag("tames_with/golden_star");
        public static final TagKey<EntityType<?>> GOLDEN_FLESH = tag("tames_with/golden_flesh");
        public static final TagKey<EntityType<?>> GOLDEN_BONE = tag("tames_with/golden_bone");
        public static final TagKey<EntityType<?>> GOLDEN_FISH = tag("tames_with/golden_fish");
        public static final TagKey<EntityType<?>> GOLDEN_KELP = tag("tames_with/golden_kelp");
        public static final TagKey<EntityType<?>> GOLDEN_FUNGUS = tag("tames_with/golden_fungus");
        public static final TagKey<EntityType<?>> GOLDEN_EYE = tag("tames_with/golden_eye");
        public static final TagKey<EntityType<?>> GOLDEN_SLIME = tag("tames_with/golden_slime");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(Petting.MODID, name));
        }
    }
}
