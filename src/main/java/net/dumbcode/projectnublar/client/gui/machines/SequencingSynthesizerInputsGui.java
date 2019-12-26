package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

public class SequencingSynthesizerInputsGui extends TabbedGuiContainer {

    private final SequencingSynthesizerBlockEntity blockEntity;

    public SequencingSynthesizerInputsGui(EntityPlayer player, SequencingSynthesizerBlockEntity blockEntity, TabInformationBar info, int tab) {
        super(blockEntity.createContainer(player, tab), info);
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
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/sequencing_synthesizer_inputs.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite water = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState());
        TextureAtlasSprite sugar = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.REEDS.getDefaultState());
        TextureAtlasSprite bone = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.BONE_BLOCK.getDefaultState());
        TextureAtlasSprite leaves = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.LEAVES.getDefaultState());

        double total = SequencingSynthesizerBlockEntity.TOTAL_AMOUNT;
        MachineUtils.drawTiledTexture(this.guiLeft + 21F, this.guiTop + 5F + 52F * (1F - this.blockEntity.getTank().getFluidAmount() / (float)this.blockEntity.getTank().getCapacity()), this.guiLeft + 37F, this.guiTop + 55F, 16, 16, water.getMinU(), water.getMinV(), water.getMaxU(), water.getMaxV());
        MachineUtils.drawTiledTexture(this.guiLeft + 59F, (float) (this.guiTop + 5F + 52F * (1F - this.blockEntity.getSugarAmount() / total)), this.guiLeft + 75F, this.guiTop + 55F, 16, 16, sugar.getMinU(), sugar.getMinV(), sugar.getMaxU(), sugar.getMaxV());
        MachineUtils.drawTiledTexture(this.guiLeft + 97F, (float) (this.guiTop + 5F + 52F * (1F - this.blockEntity.getBoneAmount() / total)), this.guiLeft + 113F, this.guiTop + 55F, 16, 16, bone.getMinU(), bone.getMinV(), bone.getMaxU(), bone.getMaxV());
        GlStateManager.color(0.0941F, 0.7098F, 0.2823F, 1f);
        MachineUtils.drawTiledTexture(this.guiLeft + 135F, (float) (this.guiTop + 5F + 52F * (1F - this.blockEntity.getPlantAmount() / total)), this.guiLeft + 151F, this.guiTop + 55F, 16, 16, leaves.getMinU(), leaves.getMinV(), leaves.getMaxU(), leaves.getMaxV());
        GlStateManager.color(1f, 1f, 1f, 1f);

        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(this.guiLeft + 21F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 59F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 97F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 135F, this.guiTop + 5F, 176, 0, 16, 52);
    }
}
