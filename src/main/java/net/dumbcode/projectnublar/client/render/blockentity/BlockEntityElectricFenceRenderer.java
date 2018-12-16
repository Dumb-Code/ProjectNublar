package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Random;

//ATM just used to see the bounding boxes
public class BlockEntityElectricFenceRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFence> {
    @Override
    public void render(BlockEntityElectricFence te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();

        BlockPos tePos = te.getPos();
        GlStateManager.translate(x, y+0.5F, z);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1f,1f,1f,1f);

        BufferBuilder buff = Tessellator.getInstance().getBuffer();

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));

        double halfthick = 1/16F;
        int texSize = 16;
        double tw = (32*halfthick)/texSize;

        for (BlockEntityElectricFence.Connection connection : te.fenceConnections) {
            Random rand = new Random(tePos.toLong());

            BlockPos to = connection.getMax();
            BlockPos from = connection.getMin();

            double yrange = (to.getY() - from.getY()) / this.distance(from, to.getX()+0.5F, to.getZ()+0.5F);
            double[] in = LineUtils.liangBarskyIntersect(tePos, from, to);
            if(in != null) {
                double tangrad = in[1] == in[0] ? Math.PI/2D : Math.atan((in[2] - in[3]) / (in[1] - in[0]));
                double xcomp = halfthick * Math.sin(tangrad);
                double zcomp = halfthick * Math.cos(tangrad);
                double[] c = new double[] { //TODO: cache this on the Connection class
                        in[0] - xcomp, in[2] - zcomp,
                        in[1] - xcomp, in[3] - zcomp,
                        in[1] + xcomp, in[3] + zcomp,
                        in[0] + xcomp, in[2] + zcomp
                };
                Vector3f xNorm = MathUtils.calcualeNormalF(
                        c[2], halfthick, c[3],
                        c[2], -halfthick, c[3],
                        c[4], -halfthick, c[5]
                );

                Vector3f zNorm = MathUtils.calcualeNormalF(
                        c[0], halfthick, c[1],
                        c[0], -halfthick, c[1],
                        c[2], -halfthick, c[3]
                );
                double ytop = yrange * this.distance(from, in[0], in[2]) - tePos.getY() + from.getY();
                double ybot = yrange * this.distance(from, in[1], in[3]) - tePos.getY() + from.getY();
                double len = Math.sqrt(Math.pow(c[0] == c[2] ? c[1]-c[3] : c[0]-c[2], 2) + (ytop-ybot)*(ytop-ybot));


                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                buff.setTranslation(-tePos.getX(), 0, -tePos.getZ());

                double u = rand.nextInt(16) / 16F;
                double v = rand.nextInt(16) / 16F;
                
                //Chunks of code are in U-D-N-E-S-W order
                buff.pos(c[0], ytop+halfthick, c[1]).tex(u, v).normal(0, 1, 0).endVertex();
                buff.pos(c[2], ybot+halfthick, c[3]).tex(u, len+v).normal(0, 1, 0).endVertex();
                buff.pos(c[4], ybot+halfthick, c[5]).tex(u+tw, len+v).normal(0, 1, 0).endVertex();
                buff.pos(c[6], ytop+halfthick, c[7]).tex(u+tw, v).normal(0, 1, 0).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(c[2], ybot-halfthick, c[3]).tex(u, v).normal(0, -1, 0).endVertex();
                buff.pos(c[0], ytop-halfthick, c[1]).tex(u, len+v).normal(0, -1, 0).endVertex();
                buff.pos(c[6], ytop-halfthick, c[7]).tex(u+tw, len+v).normal(0, -1, 0).endVertex();
                buff.pos(c[4], ybot-halfthick, c[5]).tex(u+tw, v).normal(0, -1, 0).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(c[0], ytop+halfthick, c[1]).tex(u+tw, v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(c[0], ytop-halfthick, c[1]).tex(u, halfthick+v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(c[2], ybot-halfthick, c[3]).tex(u, len+halfthick+v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(c[2], ybot+halfthick, c[3]).tex(u+tw, len+v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(c[6], ytop+halfthick, c[7]).tex(u, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(c[6], ytop-halfthick, c[7]).tex(u, tw+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(c[0], ytop-halfthick, c[1]).tex(u+tw, tw+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(c[0], ytop+halfthick, c[1]).tex(u+tw, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(c[4], ybot+halfthick, c[5]).tex(u+tw, v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(c[4], ybot-halfthick, c[5]).tex(u, -halfthick+v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(c[6], ytop-halfthick, c[7]).tex(u, len-halfthick+v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(c[6], ytop+halfthick, c[7]).tex(u+tw, len+v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(c[2], ybot+halfthick, c[3]).tex(u, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(c[2], ybot-halfthick, c[3]).tex(u, tw+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(c[4], ybot-halfthick, c[5]).tex(u+tw, tw+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(c[4], ybot+halfthick, c[5]).tex(u+tw, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();


                Tessellator.getInstance().draw();
            }
        }
        buff.setTranslation(0,0,0);
        GlStateManager.popMatrix();
    }

    private double distance(BlockPos from, double x, double z) {
        return Math.sqrt((from.getX()+0.5F-x)*(from.getX()+0.5F-x) + (from.getZ()+0.5F-z)*(from.getZ()+0.5F-z));
    }
}
