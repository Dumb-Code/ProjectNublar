package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.dumbcode.projectnublar.client.gui.icons.WeatherIcon;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.TabletBGImageHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Hand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.nio.file.Path;
import java.util.List;

//TabInformationBar
public abstract class BaseTabletScreen extends Screen {
    protected static final int HOME_ICON_SIZE = 24;
    protected static final int MAX_SCREEN_WIDTH = 250;
    protected static final float SCREEN_RATIO = TabletBGImageHandler.SCREEN_RATIO;

    protected static final int PADDING_SIDES = 50;

    protected int leftStart;
    protected int tabletWidth;
    protected int topStart;
    protected int tabletHeight;

    protected boolean homeButton = true;

    protected final Hand hand;

    protected BaseTabletScreen(Hand hand) {
        super(Component.literal("If you see this let me know I need to add it"));
        this.hand = hand;
    }

    @Override
    public void init() {
        super.init();

        this.leftStart = Math.max(PADDING_SIDES, this.width / 2 - MAX_SCREEN_WIDTH);
        this.tabletWidth = this.width - leftStart * 2;

        this.tabletHeight = (int) (this.tabletWidth / SCREEN_RATIO);
        this.topStart = this.height / 2 - this.tabletHeight / 2;
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        boolean stencil = this.allowStenciling();

        if(stencil) {
            StencilStack.pushSquareStencil(stack, this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight);
        }

        fill(stack,0, 0, this.width, this.height, -1);

        this.drawTabletScreen(stack, mouseX, mouseY, Minecraft.getInstance().getDeltaFrameTime());

        if(stencil) {
            StencilStack.popStencil();
        }

        if(this.homeButton) {
            this.renderHomePage(stack, mouseX, mouseY);
        }

        this.renderNotificationBar(stack);
        RenderUtils.renderBorderExclusive(stack, this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, 1, 0xFFFF00FF);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(this.homeButton) {
            double mouseRelX = mouseX - (this.leftStart + (this.tabletWidth-HOME_ICON_SIZE)/2F);
            double mouseRelY = mouseY - (this.topStart + this.tabletHeight - HOME_ICON_SIZE - 5);
            if(mouseRelX > 0 && mouseRelX < HOME_ICON_SIZE && mouseRelY > 0 && mouseRelY < HOME_ICON_SIZE) {
                Minecraft.getInstance().setScreen(new TabletHomeGui(this.hand));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    private void renderHomePage(GuiGraphics stack, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        minecraft.textureManager.bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/tablet_home_icon.png"));
        blit(stack,this.leftStart + (this.tabletWidth-HOME_ICON_SIZE)/2, this.topStart + this.tabletHeight - HOME_ICON_SIZE - 5, 0, 0, HOME_ICON_SIZE, HOME_ICON_SIZE, HOME_ICON_SIZE, HOME_ICON_SIZE);

        int left = this.leftStart + (this.tabletWidth-HOME_ICON_SIZE)/2;
        int top = this.topStart + this.tabletHeight - HOME_ICON_SIZE - 5;

        int mouseRelX = mouseX - left;
        int mouseRelY = mouseY - top;
        if(mouseRelX > 0 && mouseRelX < HOME_ICON_SIZE && mouseRelY > 0 && mouseRelY < HOME_ICON_SIZE) {
            stack.fill(left, top, left + HOME_ICON_SIZE, top + HOME_ICON_SIZE, 0x44000022);
        }
    }

    private void renderNotificationBar(GuiGraphics stack) {
        RenderSystem.color4f(1F , 1F, 1F, 1F);

        fillGradient(stack, this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + 16, -1072689136, -804253680);

        long time = (this.minecraft.level.getDayTime() + 6000) % 24000;
        this.minecraft.stack.drawString(font, this.thicken((time / 1000) % 24) + ":" + this.thicken((time % 1000) * 0.06D), this.leftStart + 3, this.topStart + 4, -1);

        WeatherIcon icon = WeatherIcon.guess(this.minecraft.level, this.minecraft.player.blockPosition());
        float[] uv = icon.getUV();
        this.minecraft.textureManager.bind(icon.getLocation());

        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        RenderUtils.drawTexturedQuad(stack, builder, this.leftStart + this.tabletWidth - 16F, this.topStart, this.leftStart + this.tabletWidth, this.topStart + 16F, uv[0], uv[1], uv[2], uv[3], this.getBlitOffset());
        Tessellator.getInstance().end();
    }

    private String thicken(Number number) {
        int num = number.intValue();
        return (num < 10 ? "0" : "") + num;
    }

    protected abstract void drawTabletScreen(GuiGraphics stack, int mouseX, int mouseY, float partialTicks);

    protected boolean allowStenciling() {
        return true;
    }
}
