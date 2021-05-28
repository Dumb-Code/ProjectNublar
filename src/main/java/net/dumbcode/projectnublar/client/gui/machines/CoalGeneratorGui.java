package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.CoalGeneratorBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;

public class CoalGeneratorGui extends TabbedGuiContainer<MachineModuleContainer<CoalGeneratorBlockEntity>> {

    public CoalGeneratorGui(MachineModuleContainer<CoalGeneratorBlockEntity> inventorySlotsIn, PlayerInventory playerInventory, String containerName) {
        super(inventorySlotsIn, playerInventory, containerName);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);
    }


    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        this.minecraft.getTextureManager().bind(new ResourceLocation(ProjectNublar.MODID, "textures/gui/coal_generator.png"));
        blit(stack, this.leftPos, this.topPos, 0, 0, this.width, this.height);

        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        this.minecraft.textureManager.bind(slotLocation);
        for(Slot slot : this.menu.slots) {
            blit(stack, this.leftPos + slot.x - 1, this.topPos + slot.y- 1, 7, 17, 18, 18);
        }
    }
}
