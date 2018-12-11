package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.block.BlockHandler;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.dumbcode.projectnublar.server.utils.Vec2i;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class BlockEntityElectricFencePoleRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFencePole> {

    @Override
    public void render(BlockEntityElectricFencePole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        BlockPos tePos = te.getPos();
        GlStateManager.translate(x - tePos.getX() + 0.5, y - tePos.getY(), z - tePos.getZ() + 0.5);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1F,1F,1F,1F);

        BlockPos from = te.getPos();

        for (BlockPos to : te.fenceConnections) {
            Tessellator tess = Tessellator.getInstance();

            BufferBuilder buff = tess.getBuffer();

            GlStateManager.glLineWidth(3f);

            buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
            buff.pos(from.getX(), from.getY(), from.getZ()).endVertex();
            buff.pos(to.getX(), to.getY(), to.getZ()).endVertex();
            tess.draw();


            for (Vec2i vec : LineUtils.bresenhamYCoords(from.getX() + 0.5F, to.getX() + 0.5F, from.getZ() + 0.5F, to.getZ() + 0.5F)) {
                BlockPos pos = new BlockPos(vec.x, from.getY(), vec.z);
                TileEntity t = getWorld().getTileEntity(pos);
                if(t instanceof BlockEntityElectricFence) {
                    for (AxisAlignedBB bb : ((BlockEntityElectricFence) t).createBoundingBox()) {
                        RenderGlobal.drawSelectionBoundingBox(bb.offset(pos).offset(-0.5,0,-0.5), 1f, 1f, 1f, 1f);
                    }
                }
            }
        }

        GlStateManager.popMatrix();
    }
}
