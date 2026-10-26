package net.yigitguven.petting.client.gui;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class ClampedTooltipPositioner implements ClientTooltipPositioner {
    public static final ClampedTooltipPositioner INSTANCE = new ClampedTooltipPositioner();

    @Override
    public Vector2ic positionTooltip(int screenWidth, int screenHeight, int mouseX, int mouseY, int tooltipWidth, int tooltipHeight) {
        int x = mouseX + 12;
        if (x + tooltipWidth > screenWidth - 4) {
            x = mouseX - 12 - tooltipWidth;
        }
        int y = mouseY + 12;
        if (y + tooltipHeight > screenHeight - 4) {
            y = mouseY - 12 - tooltipHeight;
        }

        int maxX = Math.max(4, screenWidth - tooltipWidth - 4);
        int maxY = Math.max(4, screenHeight - tooltipHeight - 4);
        int clampedX = Math.max(4, Math.min(x, maxX));
        int clampedY = Math.max(4, Math.min(y, maxY));
        return new Vector2i(clampedX, clampedY);
    }
}
