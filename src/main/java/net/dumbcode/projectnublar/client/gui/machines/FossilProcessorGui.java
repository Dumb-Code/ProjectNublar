package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class FossilProcessorGui extends GuiContainer {

    private final FossilProcessorBlockEntity blockEntity;

    public FossilProcessorGui(EntityPlayer player, FossilProcessorBlockEntity blockEntity) {
        super(blockEntity.createContainer(player));
        this.xSize = 176;
        this.ySize = 220;
        this.blockEntity = blockEntity;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        Minecraft.getMinecraft().renderEngine.bindTexture(slotLocation);
        for(Slot slot : this.inventorySlots.inventorySlots) {
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 7, 17, 18, 18);
        }
        GlStateManager.disableBlend();
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        drawTiledTAS(i + 8, j + 8 + 102F * (1F - this.blockEntity.getTank().getFluidAmount() / (float)this.blockEntity.getTank().getCapacity()), i + 24, j + 110, 0, 0, 16, 16, Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState()));

        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(i + 8, j + 8, 176, 0, 16, 102);

        int mX = mouseX - i;
        int mY = mouseY - j;

        if(mX > 8 && mX < 24 && mY > 8 && mY < 110) {
            this.drawHoveringText(this.blockEntity.getTank().getFluidAmount() + "mB / " + this.blockEntity.getTank().getCapacity() + "mB", mouseX, mouseY);
        }

    }

    //Tiles the TextureAtlasSprite on the y axis. TO-DO: maybe do the x axis as well?
    public static void drawTiledTAS(float left, float top, float right, float bottom, float u, float v, int renderWidth, int renderHeight,  TextureAtlasSprite tex) {
        if(renderWidth == 0 || renderHeight == 0) {
            return;
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.disableBlend();
        float height = bottom - top;
        float endFullHeight = height - (height % renderHeight);

        float minU = tex.getInterpolatedU(u);
        float maxU = tex.getInterpolatedU(u + renderWidth);

        float minV = tex.getInterpolatedV(v);
        float maxV = tex.getInterpolatedV(v + renderHeight);
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        for(int renderH = 0; renderH < endFullHeight; renderH+=renderHeight) {
            float currentH = bottom - renderH;
            buffer.pos(left, currentH - renderHeight, 0.0D).tex(minU, minV).endVertex();
            buffer.pos(left, currentH, 0.0D).tex(minU, maxV).endVertex();
            buffer.pos(right, currentH, 0.0D).tex(maxU, maxV).endVertex();
            buffer.pos(right, currentH - renderHeight, 0.0D).tex(maxU, minV).endVertex();
        }

        float leftOver = height % renderHeight;
        float yStart = bottom - endFullHeight;

        float leftV = tex.getInterpolatedV((renderHeight - leftOver)/ (float)renderHeight * 16);

        buffer.pos(left, yStart - leftOver, 0.0D).tex(minU, leftV).endVertex();
        buffer.pos(left, yStart, 0.0D).tex(minU, maxV).endVertex();
        buffer.pos(right, yStart, 0.0D).tex(maxU, maxV).endVertex();
        buffer.pos(right, yStart - leftOver, 0.0D).tex(maxU, leftV).endVertex();

        tessellator.draw();
    }
}
