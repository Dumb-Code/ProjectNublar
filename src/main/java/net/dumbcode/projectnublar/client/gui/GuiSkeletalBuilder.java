package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.dumblibrary.server.entity.GrowthStage;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.nio.IntBuffer;

public class GuiSkeletalBuilder extends GuiScreen {

    private final BlockEntitySkeletalBuilder builder;
    private final TabulaModel model;
    private boolean refreshSelectedPart;
    private Vector2f lastClickPosition = new Vector2f();
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private ModelRenderer selectedPart;

    public GuiSkeletalBuilder(BlockEntitySkeletalBuilder builder) {
        this.builder = builder;
        this.model = builder.getDinosaur().getModelContainer().getModelMap().get(GrowthStage.ADULT); // TODO: child models? -> Selectable
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("Dinosaur ID: "+builder.getDinosaur().getRegName(), mouseX, mouseY, 0xFFFFFFFF);
        String selectionText;
        if(selectedPart != null)
            selectionText = "Selected part: "+selectedPart.boxName;
        else
            selectionText = "No part selected :c";
        fontRenderer.drawString(selectionText, 0,0, 0xFFFFFFFF);
        prepareModelRendering(150, 150, 50f);
        if(refreshSelectedPart) {
            selectPart();
            refreshSelectedPart = false;
        }
        actualModelRender(partialTicks);
        cleanupModelRendering();
    }

    private int getColorUnderMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();
        colorBuffer.rewind();
        GL11.glReadPixels(x, y, 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private void selectPart() {
        ModelRenderer newSelection = null;
        float modelScale = 1f/16f;
        for (ModelRenderer modelRenderer : model.boxList) {
            AdvancedModelRenderer box = (AdvancedModelRenderer) modelRenderer;
            box.scaleX = 0f;
            box.scaleY = 0f;
            box.scaleZ = 0f;
        }

        GlStateManager.pushMatrix();

        // TODO: find out why this is required
        GlStateManager.rotate(180f, 1f, 0f, 0f);
        GlStateManager.translate(0f, -1.5f, 0f);

        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();
        Object reflectOut = ReflectionHelper.getPrivateValue(TabulaModel.class, this.model, "tabulaAnimator");
        if(reflectOut instanceof DinosaurAnimator) {
            DinosaurAnimator animator = (DinosaurAnimator)reflectOut;
            animator.setScalingDisabled(true);
            for (int index = 0; index < model.boxList.size(); index++) {
                AdvancedModelRenderer box = (AdvancedModelRenderer) model.boxList.get(index);
                float color = index / 255f;
                GlStateManager.color(color, color, color);
                int prevColor = getColorUnderMouse();

                box.scaleX = 1f;
                box.scaleY = 1f;
                box.scaleZ = 1f;
                model.render(builder.getDinosaurEntity(), 0, 0, 0f, 0, 0f, modelScale);
                box.scaleX = 0f;
                box.scaleY = 0f;
                box.scaleZ = 0f;

                int newColor = getColorUnderMouse();

                if (newColor != prevColor) {
                    newSelection = box;
                }
            }
            animator.setScalingDisabled(false);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        this.selectedPart = newSelection;
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 0) {
            refreshSelectedPart = true;
            lastClickPosition.set(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    private void prepareModelRendering(int posX, int posY, float scale) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
//        GlStateManager.rotate(135.0F+((System.currentTimeMillis() % 2000) / 1000f) * 180f, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
    }

    private void cleanupModelRendering() {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setRenderShadow(true);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    private void actualModelRender(float partialTicks) {
        model.resetToDefaultPose();
        this.mc.getRenderManager().renderEntity(builder.getDinosaurEntity(), 0, 0, 0, 0, partialTicks, false);
    }
}
