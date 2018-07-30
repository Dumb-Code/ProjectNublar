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
    private final TextComponentTranslation titleText;
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
    private double zoom = 1.0;

    public GuiSkeletalBuilder(BlockEntitySkeletalBuilder builder) {
        this.builder = builder;
        this.model = builder.getModel(); // TODO: child models? -> Selectable
        this.animator = ReflectionHelper.getPrivateValue(TabulaModel.class, this.model, "tabulaAnimator");
        TextComponentTranslation dinosaurNameComponent = builder.getDinosaur().createNameComponent();
        this.titleText = new TextComponentTranslation("projectnublar.gui.skeletal_builder.title", dinosaurNameComponent.getUnformattedText());
    }

    @Override
    public void initGui() {
        super.initGui();
        int buttonWidth = width/3; // TODO: handle when > 200px (eg Gui Scale Small)
        undoButton.x = 0;
        redoButton.x = buttonWidth;
        resetButton.x = buttonWidth*2;

        undoButton.width = buttonWidth;
        redoButton.width = buttonWidth;
        resetButton.width = buttonWidth;

        undoButton.y = height-undoButton.height-1;
        redoButton.y = height-redoButton.height-1;
        resetButton.y = height-resetButton.height-1;

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
        int scrollDirection = (int) Math.signum(Mouse.getDWheel());
        final double zoomSpeed = 0.1;
        zoom += scrollDirection * zoomSpeed;
        if(zoom < zoomSpeed)
            zoom = zoomSpeed;
        undoButton.enabled = !builder.getHistory().atHistoryBeginning();
        redoButton.enabled = !builder.getHistory().atHistoryEnd();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawBackground(int tint) {
        super.drawBackground(tint);
        drawDefaultBackground();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawBackground(0);

        GlStateManager.pushMatrix();
        // ensures that the button show above the model
        GlStateManager.translate(0f, 0f, 1000f);
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();

        String selectionText;
        if(selectedPart != null)
            selectionText = "Selected part: "+selectedPart.boxName;
        else
            selectionText = "No part selected :c";
        fontRenderer.drawString(selectionText, 0,0, 0xFFFFFFFF);
        prepareModelRendering(width/2, height/2, 30f);
        AdvancedModelRenderer partBelowMouse = findPartBelowMouse();
        if(refreshSelectedPart) {
            this.selectedPart = partBelowMouse;
            refreshSelectedPart = false;
        }
        actualModelRender(partialTicks, partBelowMouse);
        cleanupModelRendering();

        SkeletalHistory history = builder.getHistory();
        drawCenteredString(fontRenderer, (history.getIndex()+1)+"/"+history.getHistoryLength(), width/2, height-redoButton.height-fontRenderer.FONT_HEIGHT, GuiConstants.NICE_WHITE);
        drawCenteredString(fontRenderer, titleText.getUnformattedText(), width/2, 1, GuiConstants.NICE_WHITE);

        if(partBelowMouse != null) {
            drawHoveringText(partBelowMouse.boxName, mouseX, mouseY);
        }
    }

    private int getColorUnderMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();
        colorBuffer.rewind();
        GL11.glReadPixels(x, y, 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private AdvancedModelRenderer findPartBelowMouse() {
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
        GlStateManager.popMatrix();
        return newSelection;
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
        scale *= zoom;
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 500.0F);
        GlStateManager.translate(0f, -20f, 0f);
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

    private void actualModelRender(float partialTicks, AdvancedModelRenderer partBelowMouse) {
        animator.setRescalingEnabled(false);
        highlight(selectedPart, 0f, 0f, 1f);
        highlight(partBelowMouse, 1f, 1f, 0f);
        resetScalings();
        hidePart(selectedPart);
        hidePart(partBelowMouse);
        GlStateManager.color(1f, 1f, 1f);
        renderModel();
        animator.setRescalingEnabled(true);
        resetScalings();
    }

    private void hidePart(AdvancedModelRenderer part) {
        if(part == null)
            return;
        part.scaleX = 0f;
        part.scaleY = 0f;
        part.scaleZ = 0f;
    }

    private void highlight(AdvancedModelRenderer part, float red, float green, float blue) {
        if(part != null) {
            hideAllModelParts();
            part.scaleX = 1f;
            part.scaleY = 1f;
            part.scaleZ = 1f;
            GlStateManager.disableTexture2D();
            GlStateManager.color(red, green, blue);
            renderModel();
            GlStateManager.enableTexture2D();

            resetScalings();
        }
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
