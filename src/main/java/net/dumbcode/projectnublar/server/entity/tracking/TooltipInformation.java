package net.dumbcode.projectnublar.server.entity.tracking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public abstract class TooltipInformation extends TrackingDataInformation {

    protected static final FontRenderer FONT_RENDERER = Minecraft.getMinecraft().fontRenderer;

    @Nonnull
    @Override
    public Dimension getInfoDimensions() {
        List<String> lines = this.getTooltipLines();
        return new Dimension(lines.stream().mapToInt(FONT_RENDERER::getStringWidth).max().orElse(0), 11 * lines.size());
    }

    @Override
    public void renderInfo(int x, int y, int relativeMouseX, int relativeMouseY) {
//        Gui.drawRect(x, y, x + this.getInfoDimensions().width, y + this.getInfoDimensions().height, -1);
        List<String> lines = this.getTooltipLines();
        for (int i = 0; i < lines.size(); i++) {
            FONT_RENDERER.drawString(lines.get(i), x, y + i*11 + 2, 0xFF1E1E1E);
        }
        super.renderInfo(x, y, relativeMouseX, relativeMouseY);
    }

    protected abstract List<String> getTooltipLines();
}
