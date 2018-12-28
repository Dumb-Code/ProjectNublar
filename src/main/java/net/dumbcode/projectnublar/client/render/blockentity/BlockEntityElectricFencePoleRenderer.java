package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.dumbcode.projectnublar.server.utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;

public class BlockEntityElectricFencePoleRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFencePole> {

    @Override
    public void render(BlockEntityElectricFencePole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));
        for (Connection connection : te.fenceConnections) {
            BlockEntityElectricFenceRenderer.renderConnection(connection);
        }
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockPos pos = te.getPos();
        IBlockState state = te.getWorld().getBlockState(pos);
        Block block = state.getBlock();
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        if (block instanceof BlockElectricFencePole && state.getValue(BlockElectricFencePole.INDEX_PROPERTY) == 0) {
            if (te.vbo == null) {
                te.vbo = new VertexBuffer(DefaultVertexFormats.BLOCK);
                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                buff.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
                Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(),
                        Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state), state, pos, Tessellator.getInstance().getBuffer(), false);
                buff.finishDrawing();
                buff.reset();
                te.vbo.bufferData(buff.getByteBuffer());
            }


            GlStateManager.pushMatrix();
            float rotation = 90F; //Expensive calls ahead. Maybe try and cache them?
            if(!te.fenceConnections.isEmpty()) {
                Iterator<Connection> iter = te.fenceConnections.iterator();
                if (te.fenceConnections.size() == 1) {
                    Connection connection = iter.next();
                    double[] in = connection.getCache(0).getIn();
                    rotation = (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0])));
                } else {
                    Connection connection1 = iter.next();
                    Connection connection2 = iter.next();

                    double[] in1 = connection1.getCache(0).getIn();
                    double[] in2 = connection2.getCache(0).getIn();

                    double angle1 = MathUtils.horizontalDegree(in1[1] - in1[0], in1[2] - in1[3], connection1.getPosition().equals(connection1.getMin()));
                    double angle2 = MathUtils.horizontalDegree(in2[1] - in2[0], in2[2] - in2[3], connection2.getPosition().equals(connection2.getMin()));

                    rotation = (float) (angle1 + (angle2-angle1)/2D) + 90F;

                }
            }
            rotation += ((BlockElectricFencePole) block).getType().getRotationOffset() + 90F;
            if(te.rotatedAround) {
                rotation += 180;
            }
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(rotation, 0, 1, 0);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            te.vbo.bindBuffer();

            int stride = DefaultVertexFormats.BLOCK.getNextOffset();

            GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);
            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);

            GlStateManager.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, Float.BYTES * 3);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glTexCoordPointer(2, GL11.GL_FLOAT, stride, Float.BYTES * 3 + 4);
            GlStateManager.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + 1);
            GlStateManager.glTexCoordPointer(2, GL11.GL_SHORT, stride, Float.BYTES * 5 + 4);

            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);

            te.vbo.drawArrays(7);
            te.vbo.unbindBuffer();

            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);

            GlStateManager.popMatrix();

        }
        buff.setTranslation(0, 0, 0);
        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFencePole te) {
        return true;
    }
}
