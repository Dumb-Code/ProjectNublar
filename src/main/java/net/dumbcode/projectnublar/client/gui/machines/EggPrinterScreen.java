package net.dumbcode.projectnublar.client.gui.machines;

import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.EggPrinterBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EggPrinterScreen extends MachineContainerScreen {

    private static final int TEXTURE_WIDTH = 219;
    private static final int TEXTURE_HEIGHT = 230;

    private final EggPrinterBlockEntity blockEntity;
    private final MachineModuleBlockEntity.MachineProcess<EggPrinterBlockEntity> process;

    public EggPrinterScreen(MachineModuleContainer inventorySlotsIn, EggPrinterBlockEntity blockEntity, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
        this.process = this.blockEntity.getProcess(0);
        this.imageHeight = TEXTURE_HEIGHT;
    }


    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        if(mouseX >= this.leftPos+16 && mouseY >= this.topPos+17 && mouseX < this.leftPos+16+14 && mouseY < this.topPos+17+54) {
            float amount = this.blockEntity.getBoneMatter() / EggPrinterBlockEntity.TOTAL_BONE_MATTER;
            assert minecraft != null;
            stack.renderTooltip(minecraft.font, Component.literal(Math.round(amount * 100) + "%"), mouseX, mouseY);
        }

        drawProcessIcon(this.process, stack, 80, 52);
        drawProcessTooltip(this.process, stack, 51, 23, 74, 74, mouseX, mouseY);

        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics stack, int p_230451_2_, int p_230451_3_) {

    }

    @Override
    protected void renderBg(GuiGraphics stack, float partialTicks, int mouseX, int mouseY) {
        ResourceLocation sprite = new ResourceLocation(ProjectNublar.MODID, "textures/gui/egg_printer.png");
        stack.blit(sprite, this.leftPos, this.topPos, this.getOffset(), 0, 0, this.imageWidth, this.imageHeight, TEXTURE_HEIGHT, TEXTURE_WIDTH); //There is a vanilla bug that mixes up width and height

        float boneProgress = this.blockEntity.getBoneMatter() / EggPrinterBlockEntity.TOTAL_BONE_MATTER;
        bottomUpSubPixelBlit(stack, this.leftPos + 16, this.topPos + 17, 176, 66, 14, 54, TEXTURE_WIDTH, TEXTURE_HEIGHT, boneProgress);
        float machineProgress = this.blockEntity.getProcess(0).getTimeDone();
        bottomUpSubPixelBlit(stack, this.leftPos + 65, this.topPos + 28, 176, 0, 43, 66, TEXTURE_WIDTH, TEXTURE_HEIGHT, machineProgress);

    }
}
