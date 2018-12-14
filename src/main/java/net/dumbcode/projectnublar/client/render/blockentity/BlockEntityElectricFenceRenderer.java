package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

//ATM just used to see the bounding boxes
public class BlockEntityElectricFenceRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFence> {
    @Override
    public void render(BlockEntityElectricFence te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        BlockPos tePos = te.getPos();
        GlStateManager.translate(x - tePos.getX(), y - tePos.getY(), z - tePos.getZ());

        GlStateManager.glLineWidth(3f);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();

        for (AxisAlignedBB bb : te.createBoundingBox()) {
            RenderGlobal.drawSelectionBoundingBox(bb.offset(tePos), 1f, 1f, 1f, 1f);
        }

        GlStateManager.popMatrix();
    }
}
