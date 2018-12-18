package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.LineUtils;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

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

    public static void renderConnection(Connection connection) {
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        int texSize = 16;
        for (int i = 0; i < 2; i++) {
            Connection.Cache cache = i == 0 ? connection.getRenderCacheLow() : connection.getRenderCacheHigh();
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, 0.5F*i+0.25F, 0);

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
                buff.setTranslation(-connection.getPosition().getX(), 0, -connection.getPosition().getZ());

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
