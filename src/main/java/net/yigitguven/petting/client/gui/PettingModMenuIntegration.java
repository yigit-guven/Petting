package net.yigitguven.petting.client.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import net.yigitguven.petting.config.PettingConfig;

public class PettingModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Petting Configuration"));

            builder.setSavingRunnable(PettingConfig::save);

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Disable Respawn On Tame"), PettingConfig.disableRespawnOnTame)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> PettingConfig.disableRespawnOnTame = newValue)
                    .build());

            general.addEntry(entryBuilder.startIntField(Component.literal("Interaction Cooldown"), PettingConfig.interactionCooldown)
                    .setDefaultValue(20)
                    .setSaveConsumer(newValue -> PettingConfig.interactionCooldown = newValue)
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Enable Particles"), PettingConfig.enableParticles)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> PettingConfig.enableParticles = newValue)
                    .build());
                    
            general.addEntry(entryBuilder.startBooleanToggle(Component.literal("Require Kill To Tame"), PettingConfig.requireKillToTame)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> PettingConfig.requireKillToTame = newValue)
                    .build());

            general.addEntry(entryBuilder.startDoubleField(Component.literal("Tame Chance"), PettingConfig.tameChance)
                    .setDefaultValue(0.33)
                    .setSaveConsumer(newValue -> PettingConfig.tameChance = newValue)
                    .build());
            
            general.addEntry(entryBuilder.startIntField(Component.literal("Max Pets Per Player"), PettingConfig.maxPetsPerPlayer)
                    .setDefaultValue(-1)
                    .setSaveConsumer(newValue -> PettingConfig.maxPetsPerPlayer = newValue)
                    .build());

            ConfigCategory behavior = builder.getOrCreateCategory(Component.literal("Behavior"));

            behavior.addEntry(entryBuilder.startBooleanToggle(Component.literal("Sit Heal Enabled"), PettingConfig.sitHealEnabled)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> PettingConfig.sitHealEnabled = newValue)
                    .build());

            behavior.addEntry(entryBuilder.startDoubleField(Component.literal("Sit Heal Amount"), PettingConfig.sitHealAmount)
                    .setDefaultValue(1.0)
                    .setSaveConsumer(newValue -> PettingConfig.sitHealAmount = newValue)
                    .build());

            behavior.addEntry(entryBuilder.startBooleanToggle(Component.literal("Prevent Pet To Owner Damage"), PettingConfig.preventPetToOwnerDamage)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> PettingConfig.preventPetToOwnerDamage = newValue)
                    .build());

            behavior.addEntry(entryBuilder.startDoubleField(Component.literal("Follow Distance"), PettingConfig.followDistance)
                    .setDefaultValue(10.0)
                    .setSaveConsumer(newValue -> PettingConfig.followDistance = newValue)
                    .build());

            behavior.addEntry(entryBuilder.startDoubleField(Component.literal("Teleport Distance"), PettingConfig.teleportDistance)
                    .setDefaultValue(20.0)
                    .setSaveConsumer(newValue -> PettingConfig.teleportDistance = newValue)
                    .build());

            return builder.build();
        };
    }
}
