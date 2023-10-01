package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class DrillExtractorScreen extends MachineContainerScreen {

    public DrillExtractorScreen(MachineModuleContainer inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        stack.blit(new ResourceLocation(ProjectNublar.MODID, "textures/gui/drill_extractor.png"), this.leftPos, this.topPos, 0, 0, this.width, this.height);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        for(Slot slot : this.menu.slots) {
            stack.blit(slotLocation, this.leftPos + slot.x - 1, this.topPos + slot.y- 1, 7, 17, 18, 18);
        }
    }


}
