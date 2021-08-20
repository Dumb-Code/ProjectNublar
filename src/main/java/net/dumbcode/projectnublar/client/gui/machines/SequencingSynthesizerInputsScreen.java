package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.client.gui.SimpleButton;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.containers.machines.slots.SlotCanBeDisabled;
import net.dumbcode.projectnublar.server.utils.MachineUtils;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class SequencingSynthesizerInputsScreen extends SequencerSynthesizerBaseScreen {

    private static final ResourceLocation BASE_LOCATION = get("input_page");

    private final SequencingSynthesizerBlockEntity blockEntity;

    private static final int OVERLAY_WIDTH = 479;
    private static final int OVERLAY_HEIGHT = 199;

    public SequencingSynthesizerInputsScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, ITextComponent title, TabInformationBar bar, PlayerInventory playerInventory) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
    }

    @Override
    public void init() {
        super.init();
        this.addButton(new SimpleButton(this.leftPos + 128, this.topPos + 21, 94, 16, ProjectNublar.translate("gui.machine.sequencer.begin"), b -> {
            System.out.println("Kash has smol pp");
        }));
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float state) {
        super.renderScreen(stack, mouseX, mouseY, state);
        drawCenteredString(stack, font, ProjectNublar.translate("gui.machine.sequencer.matter.water"), this.leftPos + 57, this.topPos + 56, -1);
        drawCenteredString(stack, font, ProjectNublar.translate("gui.machine.sequencer.matter.sugar"), this.leftPos + 57, this.topPos + 126, -1);
        drawCenteredString(stack, font, ProjectNublar.translate("gui.machine.sequencer.matter.bone"), this.leftPos + 293, this.topPos + 56, -1);
        drawCenteredString(stack, font, ProjectNublar.translate("gui.machine.sequencer.matter.plant"), this.leftPos + 293, this.topPos + 126, -1);

        if(this.minecraft != null && this.activeSlot == null) {
            if(this.isHovering(9, 68, 96, 8, mouseX, mouseY)) {
                GuiUtils.drawHoveringText(stack, Lists.newArrayList(new StringTextComponent(Math.round(this.blockEntity.waterPercent() * 100) + "%")), mouseX, mouseY, this.width, this.height, -1, this.font);
            } else if(this.isHovering(9, 138, 96, 8, mouseX, mouseY)) {
                GuiUtils.drawHoveringText(stack, Lists.newArrayList(new StringTextComponent(Math.round(this.blockEntity.sugarPercent() * 100) + "%")), mouseX, mouseY, this.width, this.height, -1, this.font);
            } else if(this.isHovering(245, 68, 96, 8, mouseX, mouseY)) {
                GuiUtils.drawHoveringText(stack, Lists.newArrayList(new StringTextComponent(Math.round(this.blockEntity.bonePercent() * 100) + "%")), mouseX, mouseY, this.width, this.height, -1, this.font);
            } else if(this.isHovering(245, 138, 96, 8, mouseX, mouseY)) {
                GuiUtils.drawHoveringText(stack, Lists.newArrayList(new StringTextComponent(Math.round(this.blockEntity.plantPercent() * 100) + "%")), mouseX, mouseY, this.width, this.height, -1, this.font);
            }
        }

    }

    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, OVERLAY_HEIGHT, OVERLAY_WIDTH); //There is a vanilla bug that mixes up width and height

        subPixelBlit(stack, this.leftPos + 9, this.topPos + 68, 351, 128, (float) (this.blockEntity.waterPercent() * 96), 8, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        subPixelBlit(stack, this.leftPos + 9, this.topPos + 138, 351, 128, (float) (this.blockEntity.sugarPercent() * 96), 8, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        subPixelBlit(stack, this.leftPos + 245, this.topPos + 68, 351, 128, (float) (this.blockEntity.bonePercent() * 96), 8, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        subPixelBlit(stack, this.leftPos + 245, this.topPos + 138, 351, 128, (float) (this.blockEntity.plantPercent() * 96), 8, OVERLAY_WIDTH, OVERLAY_HEIGHT);

    }

    @Override
    protected void renderCenterPiece(MatrixStack stack) {
        stack.pushPose();
        stack.translate(this.leftPos, this.topPos, 0);
        stack.translate(this.imageWidth / 2F, this.imageHeight / 2F, 0);
        stack.mulPose(Vector3f.ZP.rotationDegrees((minecraft.player.tickCount + minecraft.getFrameTime()) * 0.75F));
        stack.translate(-this.imageWidth / 2F, -this.imageHeight / 2F, 0);
        blit(stack, (this.imageWidth - 106) / 2, (this.imageHeight - 107) / 2, this.getBlitOffset(), 413, 175, 106, 107, 350, 525);
        stack.popPose();
    }

    private String drawTankWithTooltip(MatrixStack stack, int mouseX, int mouseY, int left, double value, TextureAtlasSprite sprite) {
        MachineUtils.drawTiledTexture(stack, this.leftPos + left, (float) (this.topPos + 5F + 52F * (1F - value)), this.leftPos + left + 16F, this.topPos + 55F, sprite);
        if(mouseX > this.leftPos + left && mouseX < this.leftPos + left + 16F && mouseY > this.topPos + 5 && mouseY < this.topPos + 55F) {
            return Math.round(value * 100F) + "%";
        }
        return null;
    }
}
