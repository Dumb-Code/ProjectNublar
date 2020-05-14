package net.dumbcode.projectnublar.client.render;

import lombok.Getter;
import net.dumbcode.dumblibrary.server.taxidermy.TaxidermyHistory;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import javax.vecmath.Vector3f;
import java.util.Map;

public class SkeletonBuilderScene {
    private final SkeletalBuilderBlockEntity te;
    @Getter
    private final Framebuffer framebuffer;

    private int lastMode;

    public SkeletonBuilderScene(SkeletalBuilderBlockEntity te) {
        this.te = te;
        this.framebuffer = new Framebuffer(1000, 500, true);
    }

    public void update(long time, float partialTicks) {

        this.framebuffer.bindFramebuffer(true);

        this.setup();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, -15, 0);

        GlStateManager.rotate(15F, 1, 0, 0);
        GlStateManager.rotate(45, 0, 1, 0);
        GlStateManager.translate(35, 0, -35);
        GlStateManager.rotate((time + partialTicks) / 2F, 0, 1, 0);

        GlStateManager.disableTexture2D();

        this.renderGrid(20, 40);

        GlStateManager.enableTexture2D();


        GlStateManager.scale(5, 5, 5);
        GlStateManager.translate(0f, 1.5f, 0f);
        GlStateManager.rotate(180f, 0f, 0f, 1f);

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);

        if(te.getDinosaurEntity().isPresent()) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(te.getTexture());
            Map<String, TaxidermyHistory.CubeProps> poseData = te.getPoseData();
            if(te.getModel() != null) {
                for (ModelRenderer box : te.getModel().boxList) {
                    TaxidermyHistory.CubeProps cube = poseData.get(box.boxName);
                    if (cube != null) {
                        cube.applyTo(box);
                    }
                }
                te.getDinosaurEntity().ifPresent(e -> te.getModel().render(e, 0f, 0f, 100f, 0f, 0f, 1f / 16f));
            }
        }


        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
        this.setdown();

        Minecraft.getMinecraft().getFramebuffer().bindFramebuffer(true);
        
    }
    
    private void renderGrid(int lineAmount, int size) {

        double diff  = (double)size / (lineAmount - 1);
        double off = size / 2D;

        BufferBuilder buff = Tessellator.getInstance().getBuffer();
        buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int column = 0; column < lineAmount; column++) {
            buff.pos(-off, 0, column*diff-off).color(1f, 1f, 1f, 1f).endVertex();
            buff.pos(+off, 0, column*diff-off).color(1f, 1f, 1f, 1f).endVertex();
        }
        for (int row = 0; row < lineAmount; row++) {
            buff.pos(row*diff-off, 0, -off).color(1f, 1f, 1f, 1f).endVertex();
            buff.pos(row*diff-off, 0, +off).color(1f, 1f, 1f, 1f).endVertex();
        }
        Tessellator.getInstance().draw();

        buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        buff.pos(-off, -0.001, -off).color(0, 41, 102, 255).endVertex();
        buff.pos(-off, -0.001, off).color(0, 41, 102, 255).endVertex();
        buff.pos(off, -0.001, off).color(0, 41, 102, 255).endVertex();
        buff.pos(off, -0.001, -off).color(0, 41, 102, 255).endVertex();

        Tessellator.getInstance().draw();
    }

    private void setup() {
        GlStateManager.clearColor(0F, 71/255F, 179/255f, 1f);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.color(1f, 1f, 1f, 1f);

        GlStateManager.glLineWidth(2f);

        RenderHelper.disableStandardItemLighting();

        this.lastMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(30, 2, 0.05F, 100 * MathHelper.SQRT_2);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
    }

    private void setdown() {
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();

        GlStateManager.matrixMode(this.lastMode);
        GlStateManager.popMatrix();
    }

}
