package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
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
        TextureAtlasSprite tas = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState()); //TODO cache
        MachineUtils.drawTiledTexture(i + 8, j + 8 + 102F * (1F - this.blockEntity.getTank().getFluidAmount() / (float)this.blockEntity.getTank().getCapacity()), i + 24, j + 110, 16, 16, tas.getMinU(), tas.getMinV(), tas.getMaxU(), tas.getMaxV());

        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(i + 8, j + 8, 176, 0, 16, 102);

        int mX = mouseX - i;
        int mY = mouseY - j;

        if(mX > 8 && mX < 24 && mY > 8 && mY < 110) {
            this.drawHoveringText(this.blockEntity.getTank().getFluidAmount() + "mB / " + this.blockEntity.getTank().getCapacity() + "mB", mouseX, mouseY);
        }

    }
}
