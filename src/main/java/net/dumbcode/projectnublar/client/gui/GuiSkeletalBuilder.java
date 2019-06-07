package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.dumblibrary.server.animation.TabulaUtils;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModel;
import net.dumbcode.dumblibrary.client.model.tabula.TabulaModelRenderer;
import net.dumbcode.projectnublar.client.files.SkeletalBuilderFileHandler;
import net.dumbcode.projectnublar.client.files.SkeletalBuilderFileInfomation;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalHistory;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.dinosaur.Dinosaur;
import net.dumbcode.projectnublar.server.dinosaur.DinosaurHandler;
import net.dumbcode.projectnublar.server.network.C0MoveSelectedSkeletalPart;
import net.dumbcode.projectnublar.server.network.C2SkeletalMovement;
import net.dumbcode.projectnublar.server.network.C4MoveInHistory;
import net.dumbcode.projectnublar.server.network.C8FullPoseChange;
import net.dumbcode.projectnublar.server.utils.DialogBox;
import net.dumbcode.projectnublar.server.utils.RotationAxis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Vector2f;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;

import static net.dumbcode.projectnublar.client.gui.GuiConstants.mouseOn;

public class GuiSkeletalBuilder extends GuiScreen {

    private final SkeletalBuilderBlockEntity builder;
    private final SkeletalProperties properties;
    private final TabulaModel model;
    private final TextComponentTranslation titleText;
    private boolean registeredLeftClick;
    private Vector2f lastClickPosition = new Vector2f();
    private IntBuffer colorBuffer = BufferUtils.createIntBuffer(1);
    private TabulaModelRenderer selectedPart;
    private boolean movedPart;
    private TextComponentTranslation undoText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.undo");
    private TextComponentTranslation redoText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.redo");
    private TextComponentTranslation resetText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.reset");
    private TextComponentTranslation propertiesGui = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.edit_properties");
    private GuiButton undoButton = new GuiButtonExt(0, 0, 0, undoText.getUnformattedText());
    private GuiButton redoButton = new GuiButtonExt(1, 0, 0, redoText.getUnformattedText());
    private GuiButton resetButton = new GuiButtonExt(2, 0, 0, resetText.getUnformattedText());
    private GuiButton exportButton = new GuiButtonExt(3, 0, 0, 20, 20, "E");
    private GuiButton importButton = new GuiButtonExt(4, 20, 0, 20, 20, "I");
    private float cameraPitch;
    private float cameraYaw = 90f;
    private double zoom = 1.0;
    private TabulaModel rotationRingModel;
    private RotationAxis currentSelectedRing = RotationAxis.NONE;
    private boolean draggingRing = false;
    private Vector2f dMouse = new Vector2f();

    private FloatBuffer modelMatrix = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projectionMatrix = BufferUtils.createFloatBuffer(16);
    private IntBuffer viewport = BufferUtils.createIntBuffer(4);

    /**
     * Base Y component for control text, selected part text & sliders
     */
    private int baseYOffset = 20;

    private TextComponentTranslation noPartSelectedText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.no_part_selected");
    private TextComponentTranslation zoomText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.controls.zoom");
    private TextComponentTranslation selectModelPartText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.controls.select_part");
    private TextComponentTranslation rotateCameraText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.controls.rotate_camera");

    private DialogBox dialogBox;

    private double prevXSlider;
    private double prevYSlider;
    private double prevZSlider;

    private GuiSlider xRotationSlider = new GuiSlider(5, 0, 0, 200, 20,
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.prefix", "X").getUnformattedText(),
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.suffix", "X").getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);
    private GuiSlider yRotationSlider = new GuiSlider(6, 0, 0, 200, 20,
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.prefix", "Y").getUnformattedText(),
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.suffix", "Y").getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);
    private GuiSlider zRotationSlider = new GuiSlider(7, 0, 0, 200, 20,
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.prefix", "Z").getUnformattedText(),
            new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.rotation_slider.suffix", "Z").getUnformattedText(),
            -180.0, 180.0, 0.0, true, true);


    private GuiButton propertiesButton = new GuiButtonExt(8, 0, 0, propertiesGui.getUnformattedText());

    public GuiSkeletalBuilder(SkeletalBuilderBlockEntity builder) {
        this.builder = builder;
        this.properties = builder.getSkeletalProperties();
        this.model = builder.getModel(); // TODO: child models? -> Selectable
        TextComponentTranslation dinosaurNameComponent = this.getDinosaur().createNameComponent();
        this.titleText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.title", dinosaurNameComponent.getUnformattedText());
        this.rotationRingModel = TabulaUtils.getModel(GuiConstants.ROTATION_RING_LOCATION);
        this.cameraPitch = builder.getCameraPitch();
        this.cameraYaw = builder.getCameraYaw();
        this.zoom = builder.getCameraZoom();
    }

    private Dinosaur getDinosaur() {
        return builder.getDinosaur().orElse(DinosaurHandler.TYRANNOSAURUS);
    }

    @Override
    public void initGui() {
        super.initGui();
        int buttonWidth = width/3;
        undoButton.x = 0;
        redoButton.x = buttonWidth;
        resetButton.x = buttonWidth*2;

        undoButton.width = buttonWidth;
        redoButton.width = buttonWidth;
        resetButton.width = buttonWidth;

        undoButton.y = height-undoButton.height-1;
        redoButton.y = height-redoButton.height-1;
        resetButton.y = height-resetButton.height-1;

        xRotationSlider.x = width-xRotationSlider.width-1;
        yRotationSlider.x = width-yRotationSlider.width-1;
        zRotationSlider.x = width-zRotationSlider.width-1;

        propertiesButton.x = width-propertiesButton.width-1;

        // + height+2 to leave space for the text
        xRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2;
        yRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2+xRotationSlider.height+5;
        zRotationSlider.y = baseYOffset+fontRenderer.FONT_HEIGHT+2+xRotationSlider.height+yRotationSlider.height+5+5;

        propertiesButton.y = resetButton.y-2-propertiesButton.height;

        xRotationSlider.setValue(180.0);
        yRotationSlider.setValue(180.0);
        zRotationSlider.setValue(180.0);

        addButton(undoButton);
        addButton(redoButton);
        addButton(resetButton);
        addButton(xRotationSlider);
        addButton(yRotationSlider);
        addButton(zRotationSlider);
        addButton(propertiesButton);
        addButton(exportButton);
        addButton(importButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button == undoButton) {
            ProjectNublar.NETWORK.sendToServer(new C4MoveInHistory(builder, -1));
        } else if(button == redoButton) {
            ProjectNublar.NETWORK.sendToServer(new C4MoveInHistory(builder, +1));
        } else if(button == resetButton) {
            ProjectNublar.NETWORK.sendToServer(new C2SkeletalMovement(builder.getPos(), SkeletalHistory.RESET_NAME, new Vector3f()));

        } else if(button == exportButton) {
            (this.dialogBox = new DialogBox(file -> SkeletalBuilderFileHandler.serilize(new SkeletalBuilderFileInfomation(this.getDinosaur().getRegName(), this.builder.getPoseData()), file)))
                    .root(new File(mc.gameDir, "dinosaur poses"))
                    .title("Export pose")
                    .extension("ProjectNublar Dinosaur Pose (.dpose)", true, "*.dpose")

                    .showBox(DialogBox.Type.SAVE);
//            this.mc.displayGuiScreen(new GuiFileExplorer(this, "dinosaur poses", "Export", file -> SkeletalBuilderFileHandler.serilize(new SkeletalBuilderFileInfomation(this.getDinosaur().getRegName(), this.builder.getPoseData()), file))); //TODO: localize
        } else if(button == importButton) {
            this.mc.displayGuiScreen(new GuiFileExplorer(this, "dinosaur poses", "Import", file -> ProjectNublar.NETWORK.sendToServer(new C8FullPoseChange(this.builder, SkeletalBuilderFileHandler.deserilize(file).getPoseData())))); //TODO: localize
        } else if(button == propertiesButton) {
            this.mc.displayGuiScreen(new GuiSkeletalProperties(this, this.builder));
        }
    }

    public void sliderChanged(GuiSlider slider) {
        if(currentSelectedRing != RotationAxis.NONE)
            return;
        if(selectedPart == null)
            return;
        RotationAxis axis;
        if(slider == xRotationSlider) {
            axis = RotationAxis.X_AXIS;
        } else if(slider == yRotationSlider) {
            axis = RotationAxis.Y_AXIS;
        } else {
            axis = RotationAxis.Z_AXIS;
        }
        actualizeRotation(selectedPart, axis, (float)Math.toRadians(slider.getValue()));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        int scrollDirection = (int) Math.signum(Mouse.getDWheel());
        final double zoomSpeed = 0.1;
        zoom += scrollDirection * zoomSpeed;
        if(zoom < zoomSpeed)
            zoom = zoomSpeed;
        undoButton.enabled = builder.getHistory().getHistory().canUndo();
        redoButton.enabled = builder.getHistory().getHistory().canRedo();

        xRotationSlider.enabled = selectedPart != null;
        yRotationSlider.enabled = selectedPart != null;
        zRotationSlider.enabled = selectedPart != null;
        xRotationSlider.updateSlider();
        yRotationSlider.updateSlider();
        zRotationSlider.updateSlider();

        if(xRotationSlider.getValue() != prevXSlider) {
            sliderChanged(xRotationSlider);
            prevXSlider = xRotationSlider.getValue();
        }
        if(yRotationSlider.getValue() != prevYSlider) {
            sliderChanged(yRotationSlider);
            prevYSlider = yRotationSlider.getValue();
        }
        if(zRotationSlider.getValue() != prevZSlider) {
            sliderChanged(zRotationSlider);
            prevZSlider = zRotationSlider.getValue();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawBackground(int tint) {
        drawDefaultBackground();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        builder.setCameraAngles(cameraYaw, cameraPitch);
        builder.setCameraZoom(zoom);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackground(0);

        GlStateManager.pushMatrix();
        // ensures that the buttons show above the model
        GlStateManager.translate(0f, 0f, 1000f);
        super.drawScreen(mouseX, mouseY, partialTicks);
        SkeletalHistory history = builder.getHistory();
        drawCenteredString(fontRenderer, (history.getHistory().getIndex()+1)+"/"+history.getHistory().getUnindexedList().size(), width/2, height-redoButton.height-fontRenderer.FONT_HEIGHT, GuiConstants.NICE_WHITE);
        drawCenteredString(fontRenderer, titleText.getUnformattedText(), width/2, 1, GuiConstants.NICE_WHITE);

        int yOffset = baseYOffset;
        drawString(fontRenderer, TextFormatting.BOLD.toString()+TextFormatting.UNDERLINE.toString()+GuiConstants.CONTROLS_TEXT.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 15;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+selectModelPartText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.LEFT_CLICK_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+rotateCameraText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.MIDDLE_CLICK_DRAG_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.MOVEMENT_KEYS_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.ARROW_KEYS_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, TextFormatting.UNDERLINE.toString()+zoomText.getUnformattedText(), 5, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 12;
        drawString(fontRenderer, GuiConstants.MOUSE_WHEEL_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);
        yOffset += 10;
        drawString(fontRenderer, GuiConstants.TRACKPAD_ZOOM_TEXT.getUnformattedText(), 10, yOffset, GuiConstants.NICE_WHITE);

        String selectionText;
        if(selectedPart == null)
            selectionText = noPartSelectedText.getUnformattedText();
        else
            selectionText = new TextComponentTranslation(ProjectNublar.MODID+".gui.skeletal_builder.selected_part", selectedPart.boxName).getUnformattedText();
        drawCenteredString(fontRenderer, selectionText, xRotationSlider.x+xRotationSlider.width/2, baseYOffset, GuiConstants.NICE_WHITE);
        GlStateManager.popMatrix();

        setModelToPose();
        prepareModelRendering(width/8*3, height/2, 30f);
        RotationAxis ringBelowMouse = findRingBelowMouse();
        if(draggingRing) {
            if(ringBelowMouse != RotationAxis.NONE) {
                handleRingDrag(dMouse.x, dMouse.y);
            }
            dMouse.set(0f, 0f);
            draggingRing = false;
        }
        TabulaModelRenderer partBelowMouse = findPartBelowMouse();
        if(registeredLeftClick) {
            if(ringBelowMouse == RotationAxis.NONE) {
                this.selectedPart = partBelowMouse;
                this.currentSelectedRing = RotationAxis.NONE;
                if(selectedPart != null) {
                    xRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleX)));
                    yRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleY)));
                    zRotationSlider.setValue(MathHelper.wrapDegrees(Math.toDegrees(selectedPart.rotateAngleZ)));

                    prevXSlider = xRotationSlider.getValue();
                    prevYSlider = yRotationSlider.getValue();
                    prevZSlider = zRotationSlider.getValue();
                }
            } else {
                this.currentSelectedRing = ringBelowMouse;
            }
            registeredLeftClick = false;
        }
        actualModelRender(partialTicks, partBelowMouse);
        cleanupModelRendering();

        if(partBelowMouse != null) {
            drawHoveringText(partBelowMouse.boxName, mouseX, mouseY);
        }
    }

    private RotationAxis findRingBelowMouse() {
        if(selectedPart == null)
            return RotationAxis.NONE;
        int color = getColorUnderMouse();
        renderRotationRing();
        int newColor = getColorUnderMouse();
        if(newColor != color) {
            int red = (newColor >> 16) & 0xFF;
            int green = (newColor >> 8) & 0xFF;
            int blue = newColor & 0xFF;
            if(red > 0xF0 && green < 0x0A && blue < 0x0A) {
                return RotationAxis.Y_AXIS;
            }

            if(green > 0xF0 && red < 0x0A && blue < 0x0A) {
                return RotationAxis.Z_AXIS;
            }

            if(blue > 0xF0 && red < 0x0A && green < 0x0A) {
                return RotationAxis.X_AXIS;
            }
        }
        return RotationAxis.NONE;
    }

    private int getColorUnderMouse() {
        int x = Mouse.getX();
        int y = Mouse.getY();
        colorBuffer.rewind();
        GL11.glReadPixels(x, y, 1, 1, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, colorBuffer);
        return colorBuffer.get(0);
    }

    private TabulaModelRenderer findPartBelowMouse() {
        TabulaModelRenderer newSelection = null;
        hideAllModelParts();

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableTexture2D();

        for (int index = 0; index < model.boxList.size(); index++) {
            // Render the model part with a specific color and check if the color below the mouse has changed.
            // If it did, the mouse is over this given box
            TabulaModelRenderer box = (TabulaModelRenderer) model.boxList.get(index);

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
        GlStateManager.color(1f, 1f, 1f);
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        return newSelection;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(onSliders(mouseX, mouseY) || onSliders(mouseX, mouseY))
            return;
        if(mouseButton == 0) {
            registeredLeftClick = true;
        }
        lastClickPosition.set(Mouse.getX(), Mouse.getY());
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if(onButtons(mouseX, mouseY))
            return;
        if(onSliders(mouseX, mouseY)) {
            if(clickedMouseButton == 0) {
                if(!movedPart) {
                    movedPart = true;
                }
            }
            return;
        }
        if(clickedMouseButton == 2) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            cameraPitch += dy*0.1f;
            cameraYaw += dx*0.1f;

            cameraPitch %= 360f;
            cameraYaw %= 360f;
        } else if(clickedMouseButton == 0 && currentSelectedRing != RotationAxis.NONE) {
            float dx = Mouse.getX() - lastClickPosition.x;
            float dy = Mouse.getY() - lastClickPosition.y;
            draggingRing = true;
            // add, don't set. This method can be called multiple types before rendering the screen, which causes the dMouse vector to be nil more often that it should
            dMouse.x += dx;
            dMouse.y += dy;

            if(!movedPart) {
                movedPart = true;
            }
        }
        lastClickPosition.set(Mouse.getX(), Mouse.getY());
    }

    private boolean onSliders(int mouseX, int mouseY) {
        if(mouseOn(xRotationSlider, mouseX, mouseY))
            return true;
        if(mouseOn(yRotationSlider, mouseX, mouseY))
            return true;
        if(mouseOn(zRotationSlider, mouseX, mouseY))
            return true;
        return false;
    }

    private boolean onButtons(int mouseX, int mouseY) {
        for (GuiButton button : this.buttonList) {
            if(button != xRotationSlider && button != yRotationSlider && button != zRotationSlider && mouseOn(button, mouseX, mouseY))
                return true;
        }
        return false;
    }

    /**
     * Rotate the selected model part according the mouse movement
     * Basically <a href=https://en.wikipedia.org/wiki/Angular_momentum>Angular momentum on Wikipédia</a>
     */
    private void handleRingDrag(float dx, float dy) {
        if(selectedPart == null)
            return;
        if(currentSelectedRing == RotationAxis.NONE)
            return;
        Matrix3f rotationMatrix = computeRotationMatrix(selectedPart);
        Vector3f force = new Vector3f(-dx, -dy, 0f);
        rotationMatrix.transform(force);

        // === START OF CODE FOR MOUSE WORLD POS ===
        modelMatrix.rewind();
        projectionMatrix.rewind();
        viewport.rewind();
        modelMatrix.rewind();
        viewport.put(0).put(0).put(Display.getWidth()).put(Display.getHeight());
        viewport.flip();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        modelMatrix.rewind();
        projectionMatrix.rewind();
        FloatBuffer out = BufferUtils.createFloatBuffer(3);
        Project.gluUnProject(Mouse.getX(), Mouse.getY(), 0f, modelMatrix, projectionMatrix, viewport, out);

        // === END OF CODE FOR MOUSE WORLD POS ===

        float mouseZ = 400f;

        Vector3f offset = new Vector3f(out.get(0), out.get(1), mouseZ);
        Matrix4f model = new Matrix4f();
        float[] modelCopy = new float[16];
        for (int j = 0;j<4;j++) {
            for(int i = 0;i<4;i++) {
                modelCopy[i*4+j] = modelMatrix.get(i*4+j);
            }
        }
        model.set(modelCopy);
        Vector3f partOrigin = computeTranslationVector(selectedPart);

        // Make sure our vectors are in the correct space
        model.transform(partOrigin);
        model.transform(force);

        offset.sub(partOrigin);
        Vector3f moment = new Vector3f();
        moment.cross(offset, force);
        float rotAmount = Math.signum(moment.dot(currentSelectedRing.getAxis()))*0.1f; // only get the sign to have total control on the speed (and avoid unit conversion)

        float previousAngle;
        switch (currentSelectedRing) {
            case X_AXIS:
                previousAngle = selectedPart.rotateAngleX;
                break;
            case Y_AXIS:
                previousAngle = selectedPart.rotateAngleY;
                break;
            case Z_AXIS:
                previousAngle = selectedPart.rotateAngleZ;
                break;
            default:
                return;
        }
        actualizeRotation(selectedPart, currentSelectedRing, previousAngle+rotAmount);
    }

    private void actualizeRotation(TabulaModelRenderer part, RotationAxis axis, float amount) {
        ProjectNublar.NETWORK.sendToServer(new C0MoveSelectedSkeletalPart(builder, part.boxName, axis, amount));
    }

    private Vector3f computeTranslationVector(TabulaModelRenderer part) {
        Matrix4f transform = computeTransformMatrix(part);
        Vector3f result = new Vector3f(0f, 0f, 0f);
        transform.transform(result);
        return result;
    }

    private Matrix4f computeTransformMatrix(TabulaModelRenderer part) {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        applyTransformations(part, result);
        return result;
    }

    private void applyTransformations(TabulaModelRenderer part, Matrix4f out) {
        TabulaModelRenderer parent = part.getParent();
        if(parent != null) {
            applyTransformations(parent, out);
        }
        Matrix4f translation = new Matrix4f();
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.offsetX, part.offsetY, part.offsetZ));
        out.mul(translation);

        float scale = 1f/16f;
        translation.setIdentity();
        translation.setTranslation(new Vector3f(part.rotationPointX*scale, part.rotationPointY*scale, part.rotationPointZ*scale));
        out.mul(translation);

        out.rotZ(part.rotateAngleZ);
        out.rotY(part.rotateAngleY);
        out.rotX(part.rotateAngleX);

        if(part.scaleChildren) {
            Matrix4f scaling = new Matrix4f();
            scaling.setIdentity();
            scaling.m00 = part.scaleX;
            scaling.m11 = part.scaleY;
            scaling.m22 = part.scaleZ;
            out.mul(scaling);
        }
    }

    private Matrix3f computeRotationMatrix(TabulaModelRenderer part) {
        Matrix3f result = new Matrix3f();
        result.setIdentity();
        TabulaModelRenderer parent = part.getParent();
        if(parent != null) {
            result.mul(computeRotationMatrix(parent));
        }
        result.rotZ(part.rotateAngleZ);
        result.rotY(part.rotateAngleY);
        result.rotX(part.rotateAngleX);
        return result;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int button) {
        super.mouseReleased(mouseX, mouseY, button);
        if(button == 0 && movedPart) {
            movedPart = false;
            if(selectedPart != null)
                ProjectNublar.NETWORK.sendToServer(new C2SkeletalMovement(builder.getPos(), selectedPart.boxName, new Vector3f(selectedPart.rotateAngleX, selectedPart.rotateAngleY, selectedPart.rotateAngleZ)));
        } else if(button == 0) {
            currentSelectedRing = RotationAxis.NONE;
        }
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        super.handleKeyboardInput();
        GameSettings settings = mc.gameSettings;
        final float cameraSpeed = 10f;
        if(Keyboard.isKeyDown(settings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
            cameraYaw -= cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindRight.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
            cameraYaw += cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
            cameraPitch += cameraSpeed;
        }
        if(Keyboard.isKeyDown(settings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(Keyboard.KEY_UP)) {
            cameraPitch -= cameraSpeed;
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

    private void actualModelRender(float partialTicks, TabulaModelRenderer partBelowMouse) {
        highlight(selectedPart, 0f, 0f, 1f);
        highlight(partBelowMouse, 1f, 1f, 0f);
        resetScalings();
        hidePart(selectedPart);
        hidePart(partBelowMouse);
        GlStateManager.color(1f, 1f, 1f);
        renderModel();
        resetScalings();

        // render rotation ring
        if(selectedPart != null) {
//            renderRotationRing();
        }
    }

    private void renderRotationRing() {
        final float ringScale = 1.5f;
        GlStateManager.disableLighting();
        GlStateManager.disableTexture2D();
        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.color(1f, 0f, 0f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.rotate(90f, 1f, 0f, 0f);
        GlStateManager.color(0f, 1f, 0f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        selectedPart.setParentedAngles(1f/16f);
        GlStateManager.rotate(90f, 0f, 0f, 1f);
        GlStateManager.color(0f, 0f, 1f);
        GlStateManager.scale(ringScale, ringScale, ringScale);
        rotationRingModel.render(null, 0f, 0f, 0f, 0f, 0f, 1f/16f);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
    }

    private void hidePart(TabulaModelRenderer part) {
        if(part == null)
            return;
        part.scaleX = 0f;
        part.scaleY = 0f;
        part.scaleZ = 0f;
    }

    /**
     * Renders a single model part with the given color
     */
    private void highlight(TabulaModelRenderer part, float red, float green, float blue) {
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
            if(renderer instanceof TabulaModelRenderer) {
                TabulaModelRenderer part = (TabulaModelRenderer)renderer;
                part.scaleX = 1f;
                part.scaleY = 1f;
                part.scaleZ = 1f;
            }
        }
    }

    private void renderModel() {
        setModelToPose();

        mc.getTextureManager().bindTexture(builder.getTexture());
        model.renderBoxes(1f/16f);
    }

    private void setModelToPose() {
        Map<String, Vector3f> poseData = builder.getPoseData();
        for(TabulaModelRenderer box : model.getAllCubes()) {
            Vector3f rotations = poseData.get(box.boxName);
            if(rotations != null) {
                box.rotateAngleX = rotations.x;
                box.rotateAngleY = rotations.y;
                box.rotateAngleZ = rotations.z;
            } else {
                float[] rotation = box.getDefaultRotation();
                box.rotateAngleX = rotation[0];
                box.rotateAngleY = rotation[1];
                box.rotateAngleZ = rotation[2];
            }
        }
    }

    private void hideAllModelParts() {
        for(ModelRenderer box : model.boxList) {
            TabulaModelRenderer renderer = (TabulaModelRenderer)box;
            renderer.scaleX = 0f;
            renderer.scaleY = 0f;
            renderer.scaleZ = 0f;
        }
    }

    public BlockPos getBuilderPos() {
        return this.builder.getPos();
    }

}
