package net.yigitguven.petting.client.gui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.yigitguven.petting.client.PettingConfigurationScreen;

public class PettingModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return PettingConfigurationScreen::new;
    }
}
