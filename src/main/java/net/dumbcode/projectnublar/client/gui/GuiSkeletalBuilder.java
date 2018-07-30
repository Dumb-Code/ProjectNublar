package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.client.render.MoreTabulaUtils;
import net.dumbcode.projectnublar.client.render.animator.DinosaurAnimator;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.SkeletalHistory;
import net.dumbcode.projectnublar.server.network.C0MoveSelectedSkeletalPart;
import net.dumbcode.projectnublar.server.network.C2SkeletalMovement;
import net.dumbcode.projectnublar.server.network.C4MoveInHistory;
import net.dumbcode.projectnublar.server.network.C6ResetPose;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModel;
import net.ilexiconn.llibrary.client.model.tools.AdvancedModelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.Map;

public class GuiSkeletalBuilder extends GuiScreen {

    private final BlockEntitySkeletalBuilder builder;
    private final TabulaModel model;
    private final DinosaurAnimator animator;
    private boolean refreshSelectedPart;
    private Vector2f lastClickPosition = new Vector2f();
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private AdvancedModelRenderer selectedPart;
    private boolean movedPart;
    private TextComponentTranslation undoText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.undo");
    private TextComponentTranslation redoText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.redo");
    private TextComponentTranslation resetText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.reset");
    private GuiButton undoButton = new GuiButton(0, 0, 0, undoText.getUnformattedText());
    private GuiButton redoButton = new GuiButton(1, 0, 0, redoText.getUnformattedText());
    private GuiButton resetButton = new GuiButton(2, 0, 0, resetText.getUnformattedText());
    private float cameraPitch;
    private float cameraYaw;

    public GuiSkeletalBuilder(BlockEntitySkeletalBuilder builder) {
        this.builder = builder;
        this.model = builder.getModel(); // TODO: child models? -> Selectable
        this.animator = ReflectionHelper.getPrivateValue(TabulaModel.class, this.model, "tabulaAnimator");
    }

    @Override
    public void initGui() {
        super.initGui();
        undoButton.x = 250;
        redoButton.x = 250;
        resetButton.x = 250;

        redoButton.y = 20;
        resetButton.y = 40;
        addButton(undoButton);
        addButton(redoButton);
        addButton(resetButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button == undoButton) {
            ProjectNublar.NETWORK.sendToServer(new C4MoveInHistory(builder, -1));
        } else if(button == redoButton) {
            ProjectNublar.NETWORK.sendToServer(new C4MoveInHistory(builder, +1));
        } else if(button == resetButton) {
            ProjectNublar.NETWORK.sendToServer(new C6ResetPose(builder));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        undoButton.enabled = !builder.getHistory().atHistoryBeginning();
        redoButton.enabled = !builder.getHistory().atHistoryEnd();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        fontRenderer.drawString("Dinosaur ID: "+builder.getDinosaur().getRegName(), mouseX, mouseY, 0xFFFFFFFF);
        fontRenderer.drawString("History length: "+builder.getHistory().getHistoryLength(), 0, 20, 0xFFFFFFFF);
        fontRenderer.drawString("History index: "+builder.getHistory().getIndex(), 0, 30, 0xFFFFFFFF);
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
        AdvancedModelRenderer newSelection = null;
        hideAllModelParts();

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();

        animator.setRescalingEnabled(false);
        for (int index = 0; index < model.boxList.size(); index++) {
            // Render the model part with a specific color and check if the color below the mouse has changed.
            // If it did, the mouse is over this given box
            AdvancedModelRenderer box = (AdvancedModelRenderer) model.boxList.get(index);

            // multiply by 2 because in some cases, the colors are not far enough to allow to pick the correct part
            // (a box behind another may be picked instead because the color are too close)
            float color = index*2 / 255f; // FIXME: 128 boxes MAX

            GlStateManager.color(color, color, color);
            int prevColor = getColorUnderMouse();

            box.scaleX = 1f;
            box.scaleY = 1f;
            box.scaleZ = 1f;
            renderModel();
            box.scaleX = 0f;
            box.scaleY = 0f;
            box.scaleZ = 0f;

            int newColor = getColorUnderMouse();

            if (newColor != prevColor) {
                newSelection = box;
            }
        }
        animator.setRescalingEnabled(true);
        GlStateManager.color(1f, 1f, 1f);
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
        }
        lastClickPosition.set(Mouse.getX(), Mouse.getY());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(clickedMouseButton == 1) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            lastClickPosition.set(Mouse.getX(), Mouse.getY());
            if(selectedPart != null) {
                if(!movedPart) {
                    ProjectNublar.NETWORK.sendToServer(new C2SkeletalMovement(builder, selectedPart.boxName, SkeletalHistory.MovementType.STARTING));
                    movedPart = true;
                }
                ProjectNublar.NETWORK.sendToServer(new C0MoveSelectedSkeletalPart(builder, selectedPart.boxName, dx*0.1f, dy*0.1f));
            }
        } else if(clickedMouseButton == 2) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            lastClickPosition.set(Mouse.getX(), Mouse.getY());
            cameraPitch += dy*0.1f;
            cameraYaw += dx*0.1f;

            cameraPitch %= 360f;
            cameraYaw %= 360f;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if(button == 1 && movedPart) {
            movedPart = false;
            ProjectNublar.NETWORK.sendToServer(new C2SkeletalMovement(builder, selectedPart.boxName, SkeletalHistory.MovementType.STOPPING));
        }
    }

    private void prepareModelRendering(int posX, int posY, float scale) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(cameraPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(cameraYaw, 0.0F, 1.0F, 0.0F);
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
        animator.setRescalingEnabled(false);
        if(selectedPart != null) {
            hideAllModelParts();
            selectedPart.scaleX = 1f;
            selectedPart.scaleY = 1f;
            selectedPart.scaleZ = 1f;
            GlStateManager.disableTexture2D();
            GlStateManager.color(0f, 0f, 1f);
            renderModel();
            GlStateManager.enableTexture2D();

            resetScalings();
            selectedPart.scaleX = 0f;
            selectedPart.scaleY = 0f;
            selectedPart.scaleZ = 0f;
        } else {
            resetScalings();
        }
        GlStateManager.color(1f, 1f, 1f);
        renderModel();
        animator.setRescalingEnabled(true);
        resetScalings();
    }

    private void resetScalings() {
        for(ModelRenderer renderer : model.boxList) {
            if(renderer instanceof AdvancedModelRenderer) {
                AdvancedModelRenderer part = (AdvancedModelRenderer)renderer;
                part.scaleX = 1f;
                part.scaleY = 1f;
                part.scaleZ = 1f;
            }
        }
    }

    private void renderModel() {
        mc.getTextureManager().bindTexture(builder.getDinosaur().getTextureLocation(builder.getDinosaurEntity()));
        Map<String, Vector3f> poseData = builder.getPoseData();
        for(ModelRenderer box : model.boxList) {
            Vector3f rotations = poseData.get(box.boxName);
            if(rotations != null) {
                box.rotateAngleX = rotations.x;
                box.rotateAngleY = rotations.y;
                box.rotateAngleZ = rotations.z;
            }
        }
        animator.setRotationAngles(model, builder.getDinosaurEntity(), 0f, 0f, 0f, 0f, 0f, 1f/16f);
        MoreTabulaUtils.renderModelWithoutChangingPose(model, 1f/16f);
    }

    private void hideAllModelParts() {
        for(ModelRenderer box : model.boxList) {
            AdvancedModelRenderer renderer = (AdvancedModelRenderer)box;
            renderer.scaleX = 0f;
            renderer.scaleY = 0f;
            renderer.scaleZ = 0f;
        }
    }
}
