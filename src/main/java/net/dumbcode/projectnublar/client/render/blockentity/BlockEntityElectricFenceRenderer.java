package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

public class BlockEntityElectricFenceRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFence> {

    @Override
    public void render(BlockEntityElectricFence te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        BlockPos tePos = te.getPos();
        GlStateManager.translate(x - tePos.getX(), y - tePos.getY(), z - tePos.getZ());

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1F,1F,1F,1F);

        BlockPos from = te.getPos();

        float radius = 12F;
        double timer = System.currentTimeMillis() / 5000D;

        Vector3d to = new Vector3d(from.getX() + Math.sin(timer)*12F, from.getY(), from.getZ() + Math.cos(timer) *12F);

        Tessellator tess = Tessellator.getInstance();

        BufferBuilder buff = tess.getBuffer();

        GlStateManager.glLineWidth(3f);

        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        buff.pos(from.getX(), from.getY(), from.getZ()).endVertex();
        buff.pos(to.getX(), to.getY(), to.getZ()).endVertex();
        tess.draw();


//        buff.pos((from.getX()+to.getX())/2D, to.getY() + 1, (from.getZ()+to.getZ())/2D).endVertex();
        for (Vector2d vec : LineUtils.bresenhamYCoords(from.getX(), to.getX(), from.getZ(), to.getZ())) {
            double[] p = LineUtils.liangBarskyIntersect(vec.x, vec.x + 1, vec.y, vec.y + 1, from.getX(), to.getX(), from.getZ(), to.getZ());
            if(p != null) {
                buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
                buff.pos(p[0], to.getY(), p[2]).endVertex();
                buff.pos(p[0], to.getY() + 1, p[2]).endVertex();
                tess.draw();

                buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
                buff.pos(p[1], to.getY(), p[3]).endVertex();
                buff.pos(p[1], to.getY() + 1, p[3]).endVertex();
                tess.draw();

            }
        }

        GlStateManager.popMatrix();
    }
}
