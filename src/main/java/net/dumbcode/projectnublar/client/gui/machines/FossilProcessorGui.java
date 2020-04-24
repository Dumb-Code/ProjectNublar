package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.FossilProcessorBlockEntity;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class FossilProcessorGui extends TabbedGuiContainer {

    private final FossilProcessorBlockEntity blockEntity;

    public FossilProcessorGui(EntityPlayer player, FossilProcessorBlockEntity blockEntity, TabInformationBar info, int tab) {
        super(blockEntity.createContainer(player, tab), info);
        this.blockEntity = blockEntity;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        GlStateManager.disableBlend();
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite tas = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState()); //TODO cache
        MachineUtils.drawTiledTexture(this.guiLeft + 8F, this.guiTop + 8F + 52F * (1F - this.blockEntity.getTank().getFluidAmount() / (float) this.blockEntity.getTank().getCapacity()), this.guiLeft + 24F, this.guiTop + 58F, tas);

        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 8, 176, 0, 16, 102);

        int mX = mouseX - this.guiLeft;
        int mY = mouseY - this.guiTop;

        if(mX >= 8 && mX < 24 && mY >= 8 && mY < 59) {
            this.drawHoveringText(this.blockEntity.getTank().getFluidAmount() + "mB / " + this.blockEntity.getTank().getCapacity() + "mB", mouseX, mouseY);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }
}
