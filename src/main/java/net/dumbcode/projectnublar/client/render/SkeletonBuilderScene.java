package net.dumbcode.projectnublar.client.render;

import com.mojang.blaze3d.matrix.GuiGraphics;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.model.dcm.DCMModelRenderer;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.Mth;
import net.minecraft.util.math.vector.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Map;

public class SkeletonBuilderScene {
    private final SkeletalBuilderBlockEntity te;
    @Getter
    private final Framebuffer framebuffer;

    private int lastMode;

    public SkeletonBuilderScene(SkeletalBuilderBlockEntity te) {
        this.te = te;
        this.framebuffer = new Framebuffer(1000, 500, true, Minecraft.ON_OSX);
    }

    public void update(long time, float partialTicks) {

        this.framebuffer.bindWrite(true);

        this.setup();

        GuiGraphics stack = new GuiGraphics();

        stack.pushPose();
        stack.translate(0, -15, 0);

        stack.mulPose(Vector3f.XP.rotationDegrees(15F));
        stack.mulPose(Vector3f.YP.rotationDegrees(45F));
        stack.translate(35, 0, -35);
        stack.mulPose(Vector3f.YP.rotationDegrees((time + partialTicks) / 2));

        RenderSystem.disableTexture();

        this.renderGrid(stack, 20, 40);

        RenderSystem.enableTexture();

        stack.scale(5, 5, 5);
        stack.translate(0f, 1.5f, 0f);
        stack.mulPose(Vector3f.ZP.rotationDegrees(180));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);

        if(te.getDinosaurEntity().isPresent()) {
            Map<String, TaxidermyHistory.CubeProps> poseData = te.getPoseData();
            if(te.getModel() != null) {
                for (DCMModelRenderer box : te.getModel().getAllCubes()) {
                    TaxidermyHistory.CubeProps cube = poseData.get(box.getName());
                    if (cube != null) {
                        cube.applyTo(box);
                    }
                }
                te.getDinosaurEntity().ifPresent(e -> te.getModel().renderImmediate(stack, 0x00F000F0, te.getTexture()));
            }
        }


        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();

        stack.pose().popPose();
        this.setdown();

        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        
    }
    
    private void renderGrid(GuiGraphics stack, int lineAmount, int size) {

        float diff  = (float) size / (lineAmount - 1);
        float off = size / 2F;

        Matrix4f pose = stack.last().pose();

        BufferBuilder buff = Tessellator.getInstance().getBuilder();
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int column = 0; column < lineAmount; column++) {
            buff.vertex(pose, -off, 0, column*diff-off).color(1f, 1f, 1f, 1f).endVertex();
            buff.vertex(pose, +off, 0, column*diff-off).color(1f, 1f, 1f, 1f).endVertex();
        }
        for (int row = 0; row < lineAmount; row++) {
            buff.vertex(pose, row*diff-off, 0, -off).color(1f, 1f, 1f, 1f).endVertex();
            buff.vertex(pose, row*diff-off, 0, +off).color(1f, 1f, 1f, 1f).endVertex();
        }
        Tessellator.getInstance().end();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        buff.vertex(pose, -off, -0.001F, -off).color(0, 41, 102, 255).endVertex();
        buff.vertex(pose, -off, -0.001F, off).color(0, 41, 102, 255).endVertex();
        buff.vertex(pose, off, -0.001F, off).color(0, 41, 102, 255).endVertex();
        buff.vertex(pose, off, -0.001F, -off).color(0, 41, 102, 255).endVertex();

        Tessellator.getInstance().end();
    }

    private void setup() {
        RenderSystem.clearColor(0F, 71/255F, 179/255f, 1f);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        RenderSystem.lineWidth(2f);

        RenderHelper.turnOff();

        this.lastMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GL11.glPushMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(30, 2, 0.05F, 100 * Mth.SQRT_OF_TWO));
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        RenderSystem.loadIdentity();
    }

    private void setdown() {
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.popMatrix();

        RenderSystem.matrixMode(this.lastMode);
        RenderSystem.popMatrix();
    }

}
