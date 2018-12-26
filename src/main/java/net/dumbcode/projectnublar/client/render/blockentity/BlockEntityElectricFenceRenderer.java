package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_BINDING_2D;

public class BlockEntityElectricFenceRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFence> {
    @Override
    public void render(BlockEntityElectricFence te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1f,1f,1f,1f);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));
        for (Connection connection : te.fenceConnections) {
            renderConnection(connection);
        }
        GlStateManager.popMatrix();
    }

    public static void renderVoltSign(Connection connection) {
        Connection.Cache cache =  connection.getCache(0);
        double ts = 16;

        double hw = 0.5F;
        double h = 17F/32F;
        BufferBuilder buff = Tessellator.getInstance().getBuffer();

        int currentBound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/voltage_warning.png"));
        if(cache != null) {
            GlStateManager.pushMatrix();
            double[] in = cache.getIn();
            GlStateManager.translate(-connection.getPosition().getX()+in[0], 0-connection.getPosition().getY()+in[4]+0.5F, -connection.getPosition().getZ()+in[2]);
            float angle = (float) Math.toDegrees(Math.atan((in[5]-in[4]) / cache.getXzlen()));
            GlStateManager.rotate((float) Math.toDegrees(Math.atan((in[3]-in[2]) / (in[1]-in[0]))), 0, -1, 0);
            for (int i = 0; i < 2; i++) {
                GlStateManager.pushMatrix();
                GlStateManager.rotate(180*i, 0, 1, 0);
                GlStateManager.rotate(angle, 0, 0, connection.getCompared());
                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

                buff.pos(-hw, 0, -0).tex(0, (h*16)/ts).endVertex();
                buff.pos(-hw, h, -0).tex(0, 0).endVertex();
                buff.pos(hw, h, 0).tex((hw*32)/ts, 0).endVertex();
                buff.pos(hw, 0, 0).tex((hw*32)/ts, (h*16)/ts).endVertex();

                Tessellator.getInstance().draw();
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.bindTexture(currentBound);
    }

    public static void renderConnection(Connection connection) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        int texSize = 16;
        if(connection.isHasSign()) {
            renderVoltSign(connection);
        }
        for (int i = 0; i < connection.getType().getOffsets().length; i++) {
            Connection.Cache cache = connection.getCache(i);
            GlStateManager.pushMatrix();
            GlStateManager.translate(-connection.getPosition().getX(), connection.getType().getOffsets()[i], -connection.getPosition().getZ());

            if(cache != null) {

                double[] ct = cache.getCt();
                double[] cb = cache.getCb();

                Vector3f xNorm = cache.getXNorm();
                Vector3f zNorm = cache.getZNorm();

                double len = cache.getLen();
                double ytop = cache.getYtop();
                double ybot = cache.getYbot();

                double yThick = cache.getYThick();

                double tw = (16*cache.getFullThick())/texSize;

                Random rand = new Random(connection.getPosition().toLong());

                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);
                double u = rand.nextInt(16) / 16F;
                double v = rand.nextInt(16) / 16F;

                //Chunks of code are in U-D-N-E-S-W order
                buff.pos(ct[0], ytop+yThick, ct[1]).tex(u, v).normal(0, 1, 0).endVertex();
                buff.pos(ct[2], ybot+yThick, ct[3]).tex(u, len+v).normal(0, 1, 0).endVertex();
                buff.pos(ct[4], ybot+yThick, ct[5]).tex(u+tw, len+v).normal(0, 1, 0).endVertex();
                buff.pos(ct[6], ytop+yThick, ct[7]).tex(u+tw, v).normal(0, 1, 0).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(cb[2], ybot-yThick, cb[3]).tex(u, v).normal(0, -1, 0).endVertex();
                buff.pos(cb[0], ytop-yThick, cb[1]).tex(u, len+v).normal(0, -1, 0).endVertex();
                buff.pos(cb[6], ytop-yThick, cb[7]).tex(u+tw, len+v).normal(0, -1, 0).endVertex();
                buff.pos(cb[4], ybot-yThick, cb[5]).tex(u+tw, v).normal(0, -1, 0).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(ct[0], ytop+yThick, ct[1]).tex(u+tw, v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(cb[0], ytop-yThick, cb[1]).tex(u, v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(cb[2], ybot-yThick, cb[3]).tex(u, len+v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();
                buff.pos(ct[2], ybot+yThick, ct[3]).tex(u+tw, len+v).normal(zNorm.x, zNorm.y, zNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(ct[6], ytop+yThick, ct[7]).tex(u, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(cb[6], ytop-yThick, cb[7]).tex(u, tw+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(cb[0], ytop-yThick, cb[1]).tex(u+tw, tw+v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();
                buff.pos(ct[0], ytop+yThick, ct[1]).tex(u+tw, v).normal(-xNorm.x, -xNorm.y, -xNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(ct[4], ybot+yThick, ct[5]).tex(u+tw, v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(cb[4], ybot-yThick, cb[5]).tex(u, +v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(cb[6], ytop-yThick, cb[7]).tex(u, len+v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();
                buff.pos(ct[6], ytop+yThick, ct[7]).tex(u+tw, len+v).normal(-zNorm.x, -zNorm.y, -zNorm.z).endVertex();

                u = rand.nextInt(16) / 16F;
                v = rand.nextInt(16) / 16F;

                buff.pos(ct[2], ybot+yThick, ct[3]).tex(u, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(cb[2], ybot-yThick, cb[3]).tex(u, tw+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(cb[4], ybot-yThick, cb[5]).tex(u+tw, tw+v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();
                buff.pos(ct[4], ybot+yThick, ct[5]).tex(u+tw, v).normal(xNorm.x, xNorm.y, xNorm.z).endVertex();

                Tessellator.getInstance().draw();
            }
            GlStateManager.popMatrix();
        }
        buff.setTranslation(0,0,0);
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFence te) {
        return true;
    }
}
