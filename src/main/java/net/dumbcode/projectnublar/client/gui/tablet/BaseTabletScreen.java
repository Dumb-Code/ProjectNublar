package net.dumbcode.projectnublar.client.gui.tablet;

import net.dumbcode.projectnublar.client.gui.icons.WeatherIcon;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

//TabInformationBar
public abstract class BaseTabletScreen extends GuiScreen {
    protected static final int MAX_SCREEN_WIDTH = 250;
    protected static final float SCREEN_RATIO = 16F/9F;

    protected static final int PADDING_SIDES = 50;


    protected int leftStart;
    protected int tabletWidth;
    protected int topStart;
    protected int tabletHeight;

    @Override
    public void initGui() {
        super.initGui();

        this.leftStart = Math.max(PADDING_SIDES, this.width / 2 - MAX_SCREEN_WIDTH);
        this.tabletWidth = this.width - leftStart * 2;

        this.tabletHeight = (int) (this.tabletWidth / SCREEN_RATIO);
        this.topStart = this.height / 2 - this.tabletHeight / 2;

    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.drawDefaultBackground();

        boolean stencil = this.allowStenciling();

        if(stencil) {
            GL11.glEnable(GL11.GL_STENCIL_TEST);
            RenderUtils.renderSquareStencil(this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, true, 1, GL11.GL_LEQUAL);
        }

        drawRect(0, 0, this.width, this.height, -1);

        this.drawTabletScreen(mouseX, mouseY, Minecraft.getMinecraft().getRenderPartialTicks());

        if(stencil) {
            GL11.glDisable(GL11.GL_STENCIL_TEST);
        }

        this.renderNotificationBar();
        RenderUtils.renderBorderExclusive(this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, 1, 0xFFFF00FF);

    }

    private void renderNotificationBar() {
        GlStateManager.color(1F , 1F, 1F, 1F);

        this.drawGradientRect(this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + 16, -1072689136, -804253680);

        long time = (this.mc.world.getWorldTime() + 6000) % 24000;
        this.mc.fontRenderer.drawString(this.thicken((time / 1000) % 24) + ":" + this.thicken((time % 1000) * 0.06D), this.leftStart + 3, this.topStart + 4, -1);

        WeatherIcon icon = WeatherIcon.guess(this.mc.world, this.mc.player.getPosition());
        float[] uv = icon.getUV();
        this.mc.renderEngine.bindTexture(icon.getLocation());
        RenderUtils.drawTexturedQuad(this.leftStart + this.tabletWidth - 16F, this.topStart, this.leftStart + this.tabletWidth, this.topStart + 16F, uv[0], uv[1], uv[2], uv[3], this.zLevel);
    }

    private String thicken(Number number) {
        int num = number.intValue();
        return (num < 10 ? "0" : "") + num;
    }

    protected abstract void drawTabletScreen(int mouseX, int mouseY, float partialTicks);

    protected boolean allowStenciling() {
        return true;
    }
}
