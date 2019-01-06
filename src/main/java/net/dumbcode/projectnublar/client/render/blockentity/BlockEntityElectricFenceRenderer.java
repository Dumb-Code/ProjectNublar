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

                double[] uvs = cache.getUvs();

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
                    boolean reversed = connection.getCompared() < 0;
                    len /= 2D;

                    if(pb == reversed) {
                        ct = cache.getPrev().getCt();
                        cb = cache.getPrev().getCb();
                        ybot = ybot + (ytop - ybot) / 2D;

                        Vector3d point = cache.getPrev().getPoint();
                        double[] puvs = cache.getPrev().getUvs();

                        RenderUtils.drawSpacedCube(
                                ct[2], ybot + yThick, ct[3],
                                ct[2] - point.x, ybot + yThick - point.y, ct[3] - point.z,
                                ct[4] - point.x, ybot + yThick - point.y, ct[5] - point.z,
                                ct[4], ybot + yThick, ct[5],

                                cb[2], ybot - yThick, cb[3],
                                cb[2] - point.x, ybot - yThick - point.y, cb[3] - point.z,
                                cb[4] - point.x, ybot - yThick - point.y, cb[5] - point.z,
                                cb[4], ybot - yThick, cb[5],

                                puvs[0], puvs[1],
                                puvs[2], puvs[3],
                                puvs[4], puvs[5],
                                puvs[6], puvs[7],
                                puvs[8], puvs[9],
                                puvs[10], puvs[11],

                                len, th, td
                        );

                    }
                    if(nb == reversed) {
                        ct = cache.getNext().getCt();
                        cb = cache.getNext().getCb();
                        ytop = ybot + (ytop - ybot) / 2D;

                        Vector3d point = cache.getNext().getPoint();
                        double[] nuvs = cache.getNext().getUvs();

                        RenderUtils.drawSpacedCube(
                                ct[0] + point.x, ytop + yThick + point.y, ct[1] + point.z,
                                ct[0], ytop + yThick, ct[1],
                                ct[6], ytop + yThick, ct[7],
                                ct[6] + point.x, ytop + yThick + point.y, ct[7] + point.z,

                                cb[0] + point.x, ytop - yThick + point.y, cb[1] + point.z,
                                cb[0], ytop - yThick, cb[1],
                                cb[6], ytop - yThick, cb[7],
                                cb[6] + point.x, ytop - yThick + point.y, cb[7] + point.z,

                                nuvs[0], nuvs[1],
                                nuvs[2], nuvs[3],
                                nuvs[4], nuvs[5],
                                nuvs[6], nuvs[7],
                                nuvs[8], nuvs[9],
                                nuvs[10], nuvs[11],

                                len,th,td
                        );

                    }
                }
                if(!pb || !nb) {
                    RenderUtils.drawSpacedCube(
                            ct[0], ytop + yThick, ct[1],
                            ct[2], ybot + yThick, ct[3],
                            ct[4], ybot + yThick, ct[5],
                            ct[6], ytop + yThick, ct[7],

                            cb[0], ytop - yThick, cb[1],
                            cb[2], ybot - yThick, cb[3],
                            cb[4], ybot - yThick, cb[5],
                            cb[6], ytop - yThick, cb[7],

                            uvs[0], uvs[1],
                            uvs[2], uvs[3],
                            uvs[4], uvs[5],
                            uvs[6], uvs[7],
                            uvs[8], uvs[9],
                            uvs[10], uvs[11],

                            len,th,td
                    );
                }
            }
        }
        buff.setTranslation(0,0,0);
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFence te) {
        return false;
    }
}
