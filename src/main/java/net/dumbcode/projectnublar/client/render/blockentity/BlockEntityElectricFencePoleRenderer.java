package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class BlockEntityElectricFencePoleRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFencePole> {

    @Override
    public void render(BlockEntityElectricFencePole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        BlockPos tePos = te.getPos();
        GlStateManager.translate(x - tePos.getX() + .5, y - tePos.getY() + .5, z - tePos.getZ() + .5);

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
        }

        GlStateManager.popMatrix();
    }
}
