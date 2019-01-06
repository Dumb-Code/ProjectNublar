package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.client.utils.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Random;

public class BlockEntityElectricFenceRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFence> {
    @Override
    public void render(BlockEntityElectricFence te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1f,1f,1f,1f);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));
        for (Connection connection : te.fenceConnections) {
            renderConnection(connection);
        }
        GlStateManager.popMatrix();
    }

    public static void renderVoltSign(Connection connection) {
        Connection.Cache cache =  connection.getCache();
        double ts = 16;

        double hw = 0.5F;
        double h = 17F/32F;
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.setTranslation(0,0,0);
        int currentBound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/voltage_warning.png"));

        GlStateManager.disableLighting();

        if(cache != null) {
            GlStateManager.pushMatrix();
            double[] in = cache.getIn();
            GlStateManager.translate(-connection.getPosition().getX()+(in[0]+in[1])/2D, -connection.getPosition().getY()+(in[4]+in[5])/2D, -connection.getPosition().getZ()+(in[2]+in[3])/2D);
            double angle = -MathUtils.horizontalDegree(connection.getCompared() < 0 ? in[5]-in[4] : in[4]-in[5], cache.getXZlen(), true) + 90F;
            GlStateManager.rotate((float) Math.toDegrees(Math.atan((in[3]-in[2]) / (in[1]-in[0]))), 0, -1, 0);
            for (int i = 0; i < 2; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(180*i, 0, 1, 0);
                GlStateManager.rotate((float) angle * (i==1?-1:1), 0, 0, connection.getCompared());
                GlStateManager.translate(0, -h-cache.getFullThick()/2D, 0);
                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

                buff.pos(-hw, 0, 0).tex((hw*32)/ts, (h*16)/ts).endVertex();
                buff.pos(-hw, h, 0).tex((hw*32)/ts, 0).endVertex();
                buff.pos(hw, h, 0).tex(0, 0).endVertex();
                buff.pos(hw, 0, 0).tex(0, (h*16)/ts).endVertex();

                Tessellator.getInstance().draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
        }
        GlStateManager.enableLighting();

        GlStateManager.bindTexture(currentBound);
    }

    public static void renderConnection(Connection connection) {
        World world = Minecraft.getMinecraft().world;
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        int texSize = 16;
        if(!connection.isBroken()) {
            if (connection.isSign()) {
                renderVoltSign(connection);
            }

            Connection.Cache cache = connection.getCache();
            buff.setTranslation(-connection.getPosition().getX(), connection.getOffset(), -connection.getPosition().getZ());

            if (cache != null) {

                double[] ct = cache.getCt();
                double[] cb = cache.getCb();

                Vector3f xNorm = cache.getXNorm();
                Vector3f zNorm = cache.getZNorm();

                double len = cache.getTexLen();
                double ytop = cache.getYtop();
                double ybot = cache.getYbot();

                double yThick = cache.getYThick();

                double td = (16 * cache.getFullThick()) / texSize;
                double th = (16 * cache.getFullThick()) / texSize;


                boolean pb = connection.brokenSide(world, connection.getPrevious());
                boolean nb = connection.brokenSide(world, connection.getNext());
                Random rand = new Random(connection.getPosition().toLong() + (long)(connection.getOffset() * 1000));

                if(pb || nb) {
                    double[] cent = new double[] {
                            (ct[0] + ct[2])/2D,
                            (ct[1] + ct[3])/2D,
                            (ct[4] + ct[6])/2D,
                            (ct[5] + ct[7])/2D
                    };

                    double[] cenb = new double[] {
                            (cb[0] + cb[2])/2D,
                            (cb[1] + cb[3])/2D,
                            (cb[4] + cb[6])/2D,
                            (cb[5] + cb[7])/2D
                    };

                    boolean reversed = connection.getCompared() < 0;

                    if(pb == reversed) {
                        ct = new double[]{
                                ct[0], ct[1],
                                cent[0], cent[1],
                                cent[2], cent[3],
                                ct[6], ct[7]
                        };

                        cb = new double[] {
                                cb[0], cb[1],
                                cenb[0], cenb[1],
                                cenb[2], cenb[3],
                                cb[6], cb[7]
                        };

                        ybot = ybot + (ytop - ybot) / 2D;
                    }
                    if(nb == reversed) {
                        ct = new double[]{
                                cent[0], cent[1],
                                ct[2], ct[3],
                                ct[4], ct[5],
                                cent[2], cent[3],
                        };

                        cb = new double[] {
                                cenb[0], cenb[1],
                                cb[2], cb[3],
                                cb[4], cb[5],
                                cenb[2], cenb[3],
                        };
                        ytop = ybot + (ytop - ybot) / 2D;

                    }
                    len /= 2D;

                    double[] in = cache.getIn();
                    Vector3d point = new Vector3d(cache.getFullLen()/2, 0, 0);

                    Matrix4d rot = new Matrix4d();

                    rot.rotZ(Math.atan((in[4] - in[5]) / cache.getXZlen()));
                    rot.transform(point);

                    rot.rotY(in[1] == in[0] ? Math.PI/2D : Math.atan((in[2] - in[3]) / (in[1] - in[0])));
                    rot.transform(point);

                    rot.rotZ((rand.nextFloat()-0.5) * Math.PI/3F);
                    rot.transform(point);

                    rot.rotY((rand.nextFloat()-0.5F) * Math.PI/2F);
                    rot.transform(point);

                    if(pb == reversed) {
                        RenderUtils.drawSpacedCube(
                                ct[2], ybot + yThick, ct[3],
                                ct[2] - point.x, ybot + yThick - point.y, ct[3] - point.z,
                                ct[4] - point.x, ybot + yThick - point.y, ct[5] - point.z,
                                ct[4], ybot + yThick, ct[5],

                                cb[2], ybot - yThick, cb[3],
                                cb[2] - point.x, ybot - yThick - point.y, cb[3] - point.z,
                                cb[4] - point.x, ybot - yThick - point.y, cb[5] - point.z,
                                cb[4], ybot - yThick, cb[5],

                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,

                                len, th, td
                        );

                    }

                    //To prevent the line just being straight if both edges are broken, we need to redo the end point
                    if(pb && nb) {
                        point = new Vector3d(cache.getFullLen()/2, 0, 0);

                        rot.rotZ(Math.atan((in[4] - in[5]) / cache.getXZlen()));
                        rot.transform(point);

                        rot.rotY(in[1] == in[0] ? Math.PI/2D : Math.atan((in[2] - in[3]) / (in[1] - in[0])));
                        rot.transform(point);

                        rot.rotZ((rand.nextFloat()-0.5) * Math.PI/3F);
                        rot.transform(point);

                        rot.rotY((rand.nextFloat()-0.5F) * Math.PI/2F);
                        rot.transform(point);
                    }

                    if (nb == reversed) {
                        RenderUtils.drawSpacedCube(
                                ct[0] + point.x, ytop + yThick + point.y, ct[1] + point.z,
                                ct[0], ytop + yThick, ct[1],
                                ct[6], ytop + yThick, ct[7],
                                ct[6] + point.x, ytop + yThick + point.y, ct[7] + point.z,

                                cb[0] + point.x, ytop - yThick + point.y, cb[1] + point.z,
                                cb[0], ytop - yThick, cb[1],
                                cb[6], ytop - yThick, cb[7],
                                cb[6] + point.x, ytop - yThick + point.y, cb[7] + point.z,

                                rand.nextInt(16)/16D, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                                rand.nextInt(16)/16F, rand.nextInt(16)/16F,

                                len,th,td
                        );

                        //buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
                        //                        buff.pos(ct[0], ytop + yThick, ct[1]).color(1f, 1f,1f,1f).endVertex();
                        //                        buff.pos(ct[0] + point.x, ytop + yThick + point.y, ct[1] + point.z).color(1f, 1f, 1f, 1f).endVertex();
                        //
                        //                        buff.pos(ct[6], ytop + yThick, ct[7]).color(1f, 1f,1f,1f).endVertex();
                        //                        buff.pos(ct[6] + point.x, ytop + yThick + point.y, ct[7] + point.z).color(1f, 1f, 1f, 1f).endVertex();
                        //
                        //                        buff.pos(cb[0], ytop - yThick, cb[1]).color(1f, 1f,1f,1f).endVertex();
                        //                        buff.pos(cb[0] + point.x, ytop - yThick + point.y, cb[1] + point.z).color(1f, 1f, 1f, 1f).endVertex();
                        //
                        //                        buff.pos(cb[6], ytop - yThick, cb[7]).color(1f, 1f,1f,1f).endVertex();
                        //                        buff.pos(cb[6] + point.x, ytop - yThick + point.y, cb[7] + point.z).color(1f, 1f, 1f, 1f).endVertex();
                        //                        Tessellator.getInstance().draw();

                    }

                }

                RenderUtils.drawSpacedCube(
                        ct[0], ytop + yThick, ct[1],
                        ct[2], ybot + yThick, ct[3],
                        ct[4], ybot + yThick, ct[5],
                        ct[6], ytop + yThick, ct[7],

                        cb[0], ytop - yThick, cb[1],
                        cb[2], ybot - yThick, cb[3],
                        cb[4], ybot - yThick, cb[5],
                        cb[6], ytop - yThick, cb[7],

                        rand.nextInt(16)/16D, rand.nextInt(16)/16F,
                        rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                        rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                        rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                        rand.nextInt(16)/16F, rand.nextInt(16)/16F,
                        rand.nextInt(16)/16F, rand.nextInt(16)/16F,

                        len,th,td
                );


//                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
//                double u = rand.nextInt(16) / 16F;
//                double v = rand.nextInt(16) / 16F;
//
//
//                //Chunks of code are in U-D-N-S-E-W order
//                buff.pos(ct[0], ytop + yThick, ct[1]).tex(u, v).normal(0, 1, 0).endVertex();
//                buff.pos(ct[2], ybot + yThick, ct[3]).tex(u, len + v).normal(0, 1, 0).endVertex();
//                buff.pos(ct[4], ybot + yThick, ct[5]).tex(u + td, len + v).normal(0, 1, 0).endVertex();
//                buff.pos(ct[6], ytop + yThick, ct[7]).tex(u + td, v).normal(0, 1, 0).endVertex();
//
//                u = rand.nextInt(16) / 16F;
//                v = rand.nextInt(16) / 16F;
//
//                buff.pos(cb[2], ybot - yThick, cb[3]).tex(u, v).normal(0, -1, 0).endVertex();
//                buff.pos(cb[0], ytop - yThick, cb[1]).tex(u, len + v).normal(0, -1, 0).endVertex();
//                buff.pos(cb[6], ytop - yThick, cb[7]).tex(u + td, len + v).normal(0, -1, 0).endVertex();
//                buff.pos(cb[4], ybot - yThick, cb[5]).tex(u + td, v).normal(0, -1, 0).endVertex();
//
//                u = rand.nextInt(16) / 16F;
//                v = rand.nextInt(16) / 16F;
//
//                buff.pos(ct[0], ytop + yThick, ct[1]).tex(u , v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
//                buff.pos(cb[0], ytop - yThick, cb[1]).tex(u+ th, v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
//                buff.pos(cb[2], ybot - yThick, cb[3]).tex(u + th, len + v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
//                buff.pos(ct[2], ybot + yThick, ct[3]).tex(u, len + v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
//
//                u = rand.nextInt(16) / 16F;
//                v = rand.nextInt(16) / 16F;
//
//                buff.pos(ct[6], ytop+yThick, ct[7]).tex(u, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
//                buff.pos(cb[6], ytop-yThick, cb[7]).tex(u, th+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
//                buff.pos(cb[0], ytop-yThick, cb[1]).tex(u+td, th+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
//                buff.pos(ct[0], ytop+yThick, ct[1]).tex(u+td, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
//
//                u = rand.nextInt(16) / 16F;
//                v = rand.nextInt(16) / 16F;
//
//                buff.pos(ct[4], ybot + yThick, ct[5]).tex(u + th, v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
//                buff.pos(cb[4], ybot - yThick, cb[5]).tex(u, v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
//                buff.pos(cb[6], ytop - yThick, cb[7]).tex(u, len + v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
//                buff.pos(ct[6], ytop + yThick, ct[7]).tex(u + th, len + v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
//
//                u = rand.nextInt(16) / 16F;
//                v = rand.nextInt(16) / 16F;
//
//                buff.pos(ct[2], ybot+yThick, ct[3]).tex(u, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
//                buff.pos(cb[2], ybot-yThick, cb[3]).tex(u, th+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
//                buff.pos(cb[4], ybot-yThick, cb[5]).tex(u+td, th+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
//                buff.pos(ct[4], ybot+yThick, ct[5]).tex(u+td, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
//
//                Tessellator.getInstance().draw();
            }
        }
        buff.setTranslation(0,0,0);
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFence te) {
        return false;
    }
}
