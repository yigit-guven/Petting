package net.yigitguven.petting;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.yigitguven.petting.init.PettingModAttachments;

@Mod(Petting.MODID)
public class Petting {
    public static final String MODID = "petting";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Petting(IEventBus modEventBus, ModContainer modContainer) {
        PettingModAttachments.ATTACHMENT_TYPES.register(modEventBus);
    }
}
