package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

import java.util.Random;

public class SequencerSynthesizerBaseScreen extends MachineContainerScreen {

    private static final ResourceLocation BASE_LOCATION = get("base");
    private static final ResourceLocation CENTER_PIECES = get("center_pieces");

    private static final int RING_SIZE = 175;

    private final Random random = new Random();

    public SequencerSynthesizerBaseScreen(MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.imageWidth = 351;
        this.imageHeight = 199;
    }

    @Override
    protected void renderBg(MatrixStack stack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, this.imageHeight, this.imageWidth); //There is a vanilla bug that mixes up width and height

        minecraft.textureManager.bind(CENTER_PIECES);
        int ringStartX = (this.imageWidth - RING_SIZE) / 2;
        int ringStartY = (this.imageHeight - RING_SIZE) / 2;

        for (int ring = 0; ring < 5; ring++) {
            int u = (ring % 3) * RING_SIZE;
            int v = (ring / 3) * RING_SIZE;

            stack.pushPose();
            stack.translate(this.leftPos, this.topPos, 0);
            stack.translate(this.imageWidth / 2F, this.imageHeight / 2F, 0);
            stack.mulPose(Vector3f.ZP.rotationDegrees((minecraft.player.tickCount + minecraft.getFrameTime()) * (ring % 2 == 0 ? 1 : -1) * this.random.nextFloat() + 0.5F));
            stack.translate(-this.imageWidth / 2F, -this.imageHeight / 2F, 0);
            blit(stack, ringStartX, ringStartY, this.getBlitOffset(), u, v, RING_SIZE, RING_SIZE, 350, 525);
            stack.popPose();
        }

        this.renderCenterPiece(stack);

    }

    protected void renderCenterPiece(MatrixStack stack) {
        blit(stack, this.leftPos + (this.imageWidth - 63) / 2, this.topPos + (this.imageHeight - 63) / 2, this.getBlitOffset(), RING_SIZE*2, RING_SIZE, 63, 63,  350, 525);
    }

    private static ResourceLocation get(String name) {
        return new ResourceLocation(ProjectNublar.MODID, "textures/gui/sequencer/" + name + ".png");
    }
}
