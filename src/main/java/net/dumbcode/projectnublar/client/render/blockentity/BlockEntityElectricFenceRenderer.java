package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFence;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

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
        double ts = 16;

        double hw = 0.5F;
        double h = 17F/32F;

        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.setTranslation(0,0,0);
        int currentBound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/voltage_warning.png"));

        GlStateManager.disableLighting();

        GlStateManager.pushMatrix();
        double[] in = connection.getIn();
        GlStateManager.translate(-connection.getPosition().getX()+(in[0]+in[1])/2D, -connection.getPosition().getY()+(in[4]+in[5])/2D, -connection.getPosition().getZ()+(in[2]+in[3])/2D);
        double angle = -MathUtils.horizontalDegree(connection.getCompared() < 0 ? in[5]-in[4] : in[4]-in[5], connection.getXzlen(), true) + 90F;
        if(in[0] == in[1]) {
            angle = -angle;
        }
        GlStateManager.rotate((float) Math.toDegrees(Math.atan((in[3]-in[2]) / (in[1]-in[0]))), 0, -1, 0);
        for (int i = 0; i < 2; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(180*i, 0, 1, 0);
            GlStateManager.rotate((float) angle * (i==1?-1:1), 0, 0, 1);
            GlStateManager.translate(0, -h-connection.getType().getCableWidth()/2F, 0);
            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            buff.pos(-hw, 0, 0).tex((hw*32)/ts, (h*16)/ts).endVertex();
            buff.pos(-hw, h, 0).tex((hw*32)/ts, 0).endVertex();
            buff.pos(hw, h, 0).tex(0, 0).endVertex();
            buff.pos(hw, 0, 0).tex(0, (h*16)/ts).endVertex();

            Tessellator.getInstance().draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();

        GlStateManager.bindTexture(currentBound);
    }

    public static void renderConnection(Connection connection) {
        World world = Minecraft.getMinecraft().world;
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        if(!connection.isBroken()) {
            if (connection.isSign()) {
                renderVoltSign(connection);
            }
            Connection.VboCache cache = connection.getCache();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_NORMAL);


            boolean pb = connection.brokenSide(world, false);
            boolean nb = connection.brokenSide(world, true);



            if(nb) {
                buff.addVertexData(cache.getNextRotated());
                if(!pb) {
                    buff.addVertexData(cache.getNextFixed());
                }
            }

            if(pb) {
                buff.addVertexData(cache.getPrevRotated());
                if(!nb) {
                    buff.addVertexData(cache.getPrevFixed());
                }
            }
            if(!pb && !nb) {
                buff.addVertexData(cache.getData());
            }
            Tessellator.getInstance().draw();
        }
        buff.setTranslation(0,0,0);
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFence te) {
        return false;
    }
}
