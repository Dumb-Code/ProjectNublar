package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C16DisplayTabbedGui;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.List;

public class SequencingSynthesizerInputsGui extends GuiContainer {

    private final SequencingSynthesizerBlockEntity blockEntity;
    private GuiButton changeTab;

    public SequencingSynthesizerInputsGui(EntityPlayer player, SequencingSynthesizerBlockEntity blockEntity, int tab) {
        super(blockEntity.createContainer(player, tab));
        this.blockEntity = blockEntity;
        this.xSize = 208;
        this.ySize = 217;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.changeTab = this.addButton(new GuiButton(0, this.guiLeft + + 10, this.height / 2 - 10, 20, 20, "<"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button == this.changeTab) {
            ProjectNublar.NETWORK.sendToServer(new C16DisplayTabbedGui(this.blockEntity.getPos(), 0));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/sequencing_synthesizer.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        Minecraft.getMinecraft().renderEngine.bindTexture(slotLocation);
        for(Slot slot : this.inventorySlots.inventorySlots) {
            if(slot.isEnabled()) {
                this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 7, 17, 18, 18);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        TextureAtlasSprite tas = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.WATER.getDefaultState()); //TODO cache
        double total = SequencingSynthesizerBlockEntity.TOTAL_AMOUNT;
        MachineUtils.drawTiledTexture(this.guiLeft + 37, this.guiTop + 5 + 102F * (1F - this.blockEntity.getTank().getFluidAmount() / (float)this.blockEntity.getTank().getCapacity()), this.guiLeft + 53, this.guiTop + 107, 16, 16, tas.getMinU(), tas.getMinV(), tas.getMaxU(), tas.getMaxV());
        MachineUtils.drawTiledTexture(this.guiLeft + 75, (float) (this.guiTop + 5 + 102F * (1F - this.blockEntity.getSugarAmount() / total)), this.guiLeft + 91, this.guiTop + 107, 16, 16, tas.getMinU(), tas.getMinV(), tas.getMaxU(), tas.getMaxV());
        MachineUtils.drawTiledTexture(this.guiLeft + 113, (float) (this.guiTop + 5 + 102F * (1F - this.blockEntity.getBoneAmount() / total)), this.guiLeft + 129, this.guiTop + 107, 16, 16, tas.getMinU(), tas.getMinV(), tas.getMaxU(), tas.getMaxV());
        MachineUtils.drawTiledTexture(this.guiLeft + 151, (float) (this.guiTop + 5 + 102F * (1F - this.blockEntity.getPlantAmount() / total)), this.guiLeft + 167, this.guiTop + 107, 16, 16, tas.getMinU(), tas.getMinV(), tas.getMaxU(), tas.getMaxV());

        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/fossil_processor.png"));
        this.drawTexturedModalRect(this.guiLeft + 37, this.guiTop + 5, 176, 0, 16, 102);
        this.drawTexturedModalRect(this.guiLeft + 75, this.guiTop + 5, 176, 0, 16, 102);
        this.drawTexturedModalRect(this.guiLeft + 113, this.guiTop + 5, 176, 0, 16, 102);
        this.drawTexturedModalRect(this.guiLeft + 151, this.guiTop + 5, 176, 0, 16, 102);
    }
}
