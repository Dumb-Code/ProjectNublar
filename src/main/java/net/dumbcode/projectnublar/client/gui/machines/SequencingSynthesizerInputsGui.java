package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.Optional;

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

        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);

        this.zLevel = 100;
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(this.guiLeft + 21F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 59F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 97F, this.guiTop + 5F, 176, 0, 16, 52);
        this.drawTexturedModalRect(this.guiLeft + 135F, this.guiTop + 5F, 176, 0, 16, 52);

        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockModelShapes shapes = mc.getBlockRendererDispatcher().getBlockModelShapes();
        this.zLevel = 0;
        String waterTooltip = this.drawTankWithTooltip(mouseX, mouseY, 21, (float) this.blockEntity.getTank().getFluidAmount() / this.blockEntity.getTank().getCapacity(), shapes.getTexture(Blocks.WATER.getDefaultState()));
        String sugarTooltip = this.drawTankWithTooltip(mouseX, mouseY, 59, this.blockEntity.getSugarAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.REEDS.getDefaultState()));
        String boneTooltip = this.drawTankWithTooltip(mouseX, mouseY, 97, this.blockEntity.getBoneAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.BONE_BLOCK.getDefaultState()));
        GlStateManager.color(0.0941F, 0.7098F, 0.2823F, 1f);
        String plantTooltip = this.drawTankWithTooltip(mouseX, mouseY, 135, this.blockEntity.getPlantAmount() / this.blockEntity.getTotalStorage(), shapes.getTexture(Blocks.LEAVES.getDefaultState()));

        if(waterTooltip != null) {
            this.drawHoveringText(waterTooltip, mouseX, mouseY);
        } else if(sugarTooltip != null) {
            this.drawHoveringText(sugarTooltip, mouseX, mouseY);
        } else if(boneTooltip != null) {
            this.drawHoveringText(boneTooltip, mouseX, mouseY);
        } else if(plantTooltip != null) {
            this.drawHoveringText(plantTooltip, mouseX, mouseY);
        }

        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/sequencing_synthesizer_inputs.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    private String drawTankWithTooltip(int mouseX, int mouseY, int left, double value, TextureAtlasSprite sprite) {
        GlStateManager.enableDepth();
        MachineUtils.drawTiledTexture(this.guiLeft + left, (float) (this.guiTop + 5F + 52F * (1F - value)), this.guiLeft + left + 16F, this.guiTop + 55F, sprite);
        if(mouseX > this.guiLeft + left && mouseX < this.guiLeft + left + 16F && mouseY > this.guiTop + 5 && mouseY < this.guiTop + 55F) {
            return Math.round(value * 100F) + "%";
        }
        return null;
    }
}
