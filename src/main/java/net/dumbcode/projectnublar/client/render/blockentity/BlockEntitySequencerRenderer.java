package net.dumbcode.projectnublar.client.render.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.dumbcode.dumblibrary.client.YRotatedModel;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModel;
import net.dumbcode.dumblibrary.server.utils.DCMUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.MachineModuleBlock;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class BlockEntitySequencerRenderer extends TileEntityRenderer<SequencingSynthesizerBlockEntity> {

    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(ProjectNublar.MODID, "models/block/sequencer_door.dcm");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(ProjectNublar.MODID, "textures/block/sequencer_base.png");

    private static final int MOVEMENT_TICKS = 10;


    private static DCMModel model;

    public BlockEntitySequencerRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(SequencingSynthesizerBlockEntity te, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light, int overlay) {
        light = WorldRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
        stack.pushPose();
        YRotatedModel.rotateStack(stack, te.getBlockState().getValue(MachineModuleBlock.FACING));
        stack.translate(0.5, 1.5, 0.5);
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));

        boolean open = !te.getOpenedUsers().isEmpty();

        float target = te.openState ? 1 : 0;
        if(open != te.openState) {
            te.openState = open;
            te.snapshot = te.previousSnapshot;
            te.movementTicksLeft = MOVEMENT_TICKS;
        } else if(te.movementTicksLeft > 0) {
            float perc = (te.movementTicksLeft - partialTicks) / MOVEMENT_TICKS;
            target += (te.snapshot - target) * perc;
        }

        te.previousSnapshot = target;

        model.getCube("frontpaneltop").yRot = (float) (target * Math.PI/2F);
        model.renderBoxes(stack, light, buffers, TEXTURE_LOCATION);

        stack.popPose();
    }

    public static void onResourceManagerReload(IResourceManager resourceManager) {
        model = DCMUtils.getModel(MODEL_LOCATION);
    }
}
