package net.dumbcode.projectnublar.server.entity.tracking;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.List;

public abstract class TooltipInformation extends TrackingDataInformation {


    @Nonnull
    @Override
    public Dimension getInfoDimensions() {
        List<String> lines = this.getTooltipLines();
        FontRenderer fontRenderer = Minecraft.getInstance().font;
        return new Dimension(lines.stream().mapToInt(fontRenderer::width).max().orElse(0), (fontRenderer.lineHeight+2) * lines.size());
    }

    @Override
    public void renderInfo(GuiGraphics stack, int x, int y, int relativeMouseX, int relativeMouseY) {
//        Gui.drawRect(x, y, x + this.getInfoDimensions().width, y + this.getInfoDimensions().height, -1);
        FontRenderer fontRenderer = Minecraft.getInstance().font;
        List<String> lines = this.getTooltipLines();

        int center = this.getInfoDimensions().height / 2;

        for (int i = 0; i < lines.size(); i++) {
            fontRenderer.draw(stack, lines.get(i), x, y + center - lines.size()*6 + i*(fontRenderer.lineHeight+2) + 3, 0xFF1E1E1E);
        }
        super.renderInfo(stack, x, y, relativeMouseX, relativeMouseY);
    }

    protected abstract List<String> getTooltipLines();
}
