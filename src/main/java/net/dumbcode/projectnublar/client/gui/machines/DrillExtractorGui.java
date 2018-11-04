package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.server.block.entity.DrillExtractorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class DrillExtractorGui extends GuiContainer {

    private final DrillExtractorBlockEntity blockEntity;

    public DrillExtractorGui(EntityPlayer player, DrillExtractorBlockEntity blockEntity, int tab) {
        super(blockEntity.createContainer(player, tab));
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
        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        Minecraft.getMinecraft().renderEngine.bindTexture(slotLocation);
        for(Slot slot : this.inventorySlots.inventorySlots) {
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 7, 17, 18, 18);
        }
    }
}
