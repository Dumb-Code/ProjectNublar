package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockElectricFencePole;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.dumbcode.projectnublar.server.utils.Connection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLSync;

public class BlockEntityElectricFencePoleRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFencePole> {

    @Override
    public void render(BlockEntityElectricFencePole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));
        for (Connection connection : te.fenceConnections) {
            BlockEntityElectricFenceRenderer.renderConnection(connection);
        }
        Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

        BlockPos pos = te.getPos();
        IBlockState state = te.getWorld().getBlockState(pos);
        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        if (state.getValue(BlockElectricFencePole.TYPE_PROPERTY) == BlockElectricFencePole.Type.BASE) {
            if (te.renderList == -1) {
                GlStateManager.glNewList(te.renderList = GlStateManager.glGenLists(1), GL11.GL_COMPILE);
                GlStateManager.disableLighting();
                buff.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                buff.setTranslation(-pos.getX(), -pos.getY(), -pos.getZ());
                Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(te.getWorld(),
                        Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state), state, pos, Tessellator.getInstance().getBuffer(), false);
                Tessellator.getInstance().draw();
                GlStateManager.glEndList();
            }

            GlStateManager.pushMatrix();
            float rotation = 90F; //Expensive calls ahead. Maybe try and cache them?
            if (te.fenceConnections.size() == 1) {
                Connection connection = te.fenceConnections.get(0);
                double[] in = connection.getRenderCacheLow().getIn();
                rotation = (float) Math.toDegrees(Math.atan((in[2] - in[3]) / (in[1] - in[0])));
            } else if (te.fenceConnections.size() > 0) {
                Connection connection1 = te.fenceConnections.get(0);
                Connection connection2 = te.fenceConnections.get(1);

                BlockPos other1 = connection1.getPosition().equals(connection1.getFrom()) ? connection1.getTo() : connection1.getFrom();
                BlockPos other2 = connection2.getPosition().equals(connection2.getFrom()) ? connection2.getTo() : connection2.getFrom();

                BlockPos dist = other1.subtract(other2);

                rotation = (float) Math.toDegrees(Math.atan2(dist.getX(), dist.getZ())) - 90F;
            }
            if(te.rotatedAround) {
                rotation += 180;
            }
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            GlStateManager.rotate(rotation, 0, 1, 0);
            GlStateManager.translate(-0.5F, -0.5F, -0.5F);
            GlStateManager.callList(te.renderList);
            GlStateManager.popMatrix();

        }
        buff.setTranslation(0, 0, 0);
        GlStateManager.enableLighting();

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFencePole te) {
        return true;
    }
}
