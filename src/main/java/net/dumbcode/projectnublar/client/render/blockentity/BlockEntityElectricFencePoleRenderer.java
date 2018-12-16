package net.dumbcode.projectnublar.client.render.blockentity;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntityElectricFencePole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLSync;

public class BlockEntityElectricFencePoleRenderer extends TileEntitySpecialRenderer<BlockEntityElectricFencePole> {

    @Override
    public void render(BlockEntityElectricFencePole te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if(true)return;
        GlStateManager.pushMatrix();

        GlStateManager.translate(x + 0.5F, y, z + 0.5F);

        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.color(1F,1F,1F,1F);
        BlockPos from = te.getPos();

        GlStateManager.disableCull();

        double texheight = 16;
        double texwidth = 16;

        Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/blocks/electric_fence.png"));
        Tessellator tess = Tessellator.getInstance();
//
        BufferBuilder buff = tess.getBuffer();
        for (BlockPos to : te.fenceConnections) {
            if(to.compareTo(from) <= 0) {
                continue;
            }

            int xdist = to.getX() - from.getX();
            int ydist = to.getY() - from.getY();
            int zdist = to.getZ() - from.getZ();

            GlStateManager.pushMatrix();
            double u = Math.abs(to.getX() - from.getX());
            double v = Math.abs(to.getZ() - from.getZ());

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

            buff.pos(0, 3, 0).tex(0, 0).endVertex();
            buff.pos(0, 0, 0).tex(0, v).endVertex();
            buff.pos(xdist, ydist, zdist).tex(u, v).endVertex();
            buff.pos(xdist, ydist + 3, zdist).tex(u, 0).endVertex();
            tess.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(BlockEntityElectricFencePole te) {
        return true;
    }
}
