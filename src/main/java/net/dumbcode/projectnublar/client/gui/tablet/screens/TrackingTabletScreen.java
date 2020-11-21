package net.dumbcode.projectnublar.client.gui.tablet.screens;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.dumbcode.projectnublar.server.network.C23ConfirmTrackingTablet;
import net.dumbcode.projectnublar.server.network.C25StopTrackingTablet;
import net.dumbcode.projectnublar.server.network.C30TrackingTabletEntryClicked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector4f;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TrackingTabletScreen extends TabletPage {

    private GuiScrollBox<ScrollEntry> scrollBox;
    private final List<ScrollEntry> scrollEntries;

    private int startX;
    private int startZ;

    private int textureWidth;
    private int textureHeight;

    //The reason I don't use DynamicTexture is because I don't want to have to reupload ALL the pixels every time i make a change.
    //Doing direct texture IDs, I can use glTexSubImage2d (uploadTextureMipmap), and just change the affected pixels
    private int textureID = -1;

    private int prevClickedX;
    private int prevClickedY;

    private final Matrix4f transformation = new Matrix4f();
    private final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);

    private List<TrackingSavedData.DataEntry> trackingData = new ArrayList<>();
    private TrackingSavedData.DataEntry selected;

    public TrackingTabletScreen(List<Pair<BlockPos, String>> entries) {
        this.scrollEntries = entries.stream().map(pair -> new ScrollEntry(pair.getKey(), pair.getRight())).collect(Collectors.toList());
        this.transformation.setIdentity();
    }

    @Override
    public void setData(int xSize, int ySize, String route) {
        super.setData(xSize, ySize, route);
        this.scrollBox = new GuiScrollBox<>(this.xSize - 100, 20, 100, 15, (this.ySize - 30) / 15, () -> this.scrollEntries);
    }

    public void initializeSize(int startX, int startZ, int textureWidth, int textureHeight) {
        this.startX = startX;
        this.startZ = startZ;

        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        if(this.textureID != -1) {
            TextureUtil.deleteTexture(this.textureID);
        }

        this.textureID = TextureUtil.glGenTextures();
        TextureUtil.allocateTextureImpl(this.textureID, 0, this.textureWidth, this.textureHeight);

        ProjectNublar.NETWORK.sendToServer(new C23ConfirmTrackingTablet());

    }


    @Override
    public void render(int mouseX, int mouseY, float partialTicks, String route) {
        if(this.textureID != -1) {

            GlStateManager.pushMatrix();

            this.putInFloat();
            GlStateManager.translate(this.xSize / 2F, this.ySize / 2F, 0);
            GlStateManager.multMatrix(this.buffer);

            GlStateManager.bindTexture(this.textureID);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            Gui.drawScaledCustomSizeModalRect(-this.xSize / 2, -this.xSize / 2, 0, 0, 1, 1, this.xSize, this.xSize, 1F, 1F);

            GlStateManager.popMatrix();

            for (TrackingSavedData.DataEntry datum : this.trackingData) {
                Vec2f point = this.getPoint(datum);
                //Debug
                RenderUtils.renderBorderExclusive((int) point.x - 1, (int) point.y - 1, (int) point.x + 1, (int) point.y + 1, 2, 0xFFFF0000);
                datum.getInformation().forEach(i -> i.renderMap((int) point.x, (int) point.y));
            }
            if(this.selected != null) {
                this.renderTooltip(mouseX, mouseY);
            }
        }

        this.scrollBox.render(mouseX, mouseY);
    }

    private void renderTooltip(int mouseX, int mouseY) {
        int padding = 10;
        int borderSize = 2;

        List<Dimension> dimensions = this.selected.getInformation().stream().map(TrackingDataInformation::getInfoDimensions).collect(Collectors.toList());
        double width = 2*padding + dimensions.stream().mapToDouble(Dimension::getWidth).reduce(Math::max).orElseThrow(NoSuchElementException::new);
        double height = padding + dimensions.stream().mapToDouble(Dimension::getHeight).map(d -> d != 0 ? d + padding : 0).sum();
        Gui.drawRect(0, 15, (int) width, (int) height + 15, 0xFF7A7A7A);
        RenderUtils.renderBorderExclusive(borderSize, 15 + borderSize, (int) width - borderSize, (int) height + 15 - borderSize, borderSize, 0xFF333333);
        double yCoord = 15D + padding;
        for (TrackingDataInformation info : this.selected.getInformation()) {
            Dimension d = info.getInfoDimensions();
            int relx = mouseX - padding;
            int rely = mouseY - (int) yCoord;
            boolean over = MathUtils.inBetween(relx, 0, d.width) && MathUtils.inBetween(rely, 0, d.height);
            if(d.height != 0) {
                info.renderInfo(padding, (int) yCoord, over ? relx : -1, over ? rely : -1);
                yCoord += d.height + padding;

            }
        }
    }

    private Optional<TrackingSavedData.DataEntry> getEntryUnder(int x, int y, int maxDistFromPoint) {
        TrackingSavedData.DataEntry nearest = null;
        float nearestDist = Integer.MAX_VALUE;

        for (TrackingSavedData.DataEntry datum : this.trackingData) {
            Vec2f point = this.getPoint(datum);
            float dist = (point.x - x)*(point.x - x) + (point.y - y)*(point.y - y);
            if(dist < nearestDist) {
                nearestDist = dist;
                nearest = datum;
            }
        }

        return nearestDist <= maxDistFromPoint*maxDistFromPoint ? Optional.ofNullable(nearest) : Optional.empty();
    }

    private Vec2f getPoint(TrackingSavedData.DataEntry entry) {
        float x = (float) ((entry.getPosition().x - this.startX) / this.textureWidth * this.xSize);
        float y = (float) ((entry.getPosition().z - this.startZ) / this.textureHeight * this.xSize);

        return this.getTransformedPoint(x, y, this.transformation);
    }

    private void putInFloat() {
        this.buffer.rewind();
        this.buffer.put(this.transformation.m00);
        this.buffer.put(this.transformation.m10);
        this.buffer.put(this.transformation.m20);
        this.buffer.put(this.transformation.m30);

        this.buffer.put(this.transformation.m01);
        this.buffer.put(this.transformation.m11);
        this.buffer.put(this.transformation.m21);
        this.buffer.put(this.transformation.m31);

        this.buffer.put(this.transformation.m02);
        this.buffer.put(this.transformation.m12);
        this.buffer.put(this.transformation.m22);
        this.buffer.put(this.transformation.m32);

        this.buffer.put(this.transformation.m03);
        this.buffer.put(this.transformation.m13);
        this.buffer.put(this.transformation.m23);
        this.buffer.put(this.transformation.m33);

        this.buffer.rewind();
    }

    @Override
    public void onClosed() {
        TextureUtil.deleteTexture(this.textureID);
        ProjectNublar.NETWORK.sendToServer(new C25StopTrackingTablet());
    }

    public void setTrackingData(List<TrackingSavedData.DataEntry> trackingData) {
        this.trackingData = trackingData;
        this.selected = trackingData.stream().filter(d -> this.selected != null && d.getUuid().equals(this.selected.getUuid())).findAny().orElse(null);
        if(this.selected != null) {
            Vec2f point = this.getPoint(this.selected);
            this.translateWithZoom(matrix4f -> matrix4f.m03 = -point.x + this.xSize / 2);
            this.translateWithZoom(matrix4f -> matrix4f.m13 = -point.y + this.ySize / 2);
        }
    }

    public void setRGB(int startX, int startZ, int width, int height, int[] setIntoArray) {
        GlStateManager.bindTexture(this.textureID);
        TextureUtil.uploadTextureMipmap(new int[][]{setIntoArray}, width, height, startX - this.startX, startZ - this.startZ, false, false);
    }

    @Override
    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.prevClickedX = mouseX;
        this.prevClickedY = mouseY;

        this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onMouseReleased(int mouseX, int mouseY, int state) {
        if(!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            this.selected = this.selected != null ? null : this.getEntryUnder(mouseX, mouseY, 10).orElse(null);
        }
        super.onMouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize()) && clickedMouseButton == 0 && this.selected == null) {
            this.tryMulMatrix(matrix4f -> matrix4f.m03 = mouseX - this.prevClickedX, fail -> {});
            this.tryMulMatrix(matrix4f -> matrix4f.m13 = mouseY - this.prevClickedY, fail -> {});
        }

        this.prevClickedX = mouseX;
        this.prevClickedY = mouseY;
    }

    private void translateWithZoom(Consumer<Matrix4f> mat) {
        float modifier = 0.75F;
        this.tryMulMatrix(mat, fail -> {
            this.tryMulMatrix(m -> m.setScale(1F/modifier), impossibleFails -> {});
            this.translateWithZoom(mat);
        });

    }

    @Override
    public void onMouseInput(int mouseX, int mouseY) {
        if (this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            this.scrollBox.handleMouseInput();
        } else {
            int wheel = Mouse.getDWheel();
            if(wheel != 0) {
                float modifier = 0.75F;
                float mod = wheel < 0 ? modifier : 1F/modifier;

                //Ignore disgusting code below. In the future maybe remove the matrix stuff, it don't really be needed
                this.tryMulMatrix(matrix4f -> matrix4f.setScale(mod), fail -> {
                    if(this.selected != null || wheel > 0) {
                        return;
                    }
                    boolean leftOut = fail.x > 0;
                    boolean rightOut = fail.z < this.xSize;

                    boolean topOut = fail.y > 0;
                    boolean bottomOut = fail.w < this.ySize;


                    float xOffset = 0;
                    if(leftOut != rightOut) {
                        if(leftOut) {
                            xOffset = -fail.x;
                        } else {
                            xOffset = this.xSize - fail.z;
                        }
                    }

                    float yOffset = 0;
                    if(topOut != bottomOut) {
                        if(topOut) {
                            yOffset = -fail.y;
                        } else {
                            yOffset = this.ySize - fail.z;
                        }
                    }

                    float finalXOff = xOffset;
                    float finalYOff = yOffset;

                    this.tryMulMatrix(matrix4f -> {
                        Matrix4f scale = new Matrix4f();
                        scale.setIdentity();
                        scale.setScale(mod);

                        Matrix4f translation = new Matrix4f();
                        translation.setIdentity();
                        translation.m03 = finalXOff;
                        translation.m13 = finalYOff;

                        matrix4f.mul(translation, scale);
                    }, failImpossible -> { /*Throw something?*/ });
                });
            }
        }
    }

    private void tryMulMatrix(Consumer<Matrix4f> mat, Consumer<Vector4f> onFail) {
        Matrix4f newTransformation = new Matrix4f();
        newTransformation.setIdentity();
        mat.accept(newTransformation);

        Matrix4f finishedTransformation = new Matrix4f();
        finishedTransformation.mul(newTransformation, this.transformation);

        // (x, y)
        //
        //          (z, w)
        Vector4f fail = new Vector4f();
        boolean legal = true;

        for (int i = 0; i < 4; i++) {
            Vec2f point = this.getTransformedPoint((i & 1) == 0 ? 0 : this.xSize, (i & 2) == 0 ? 0 : this.xSize, finishedTransformation);
            legal &= this.isInBound(point.x, point.y);

            if(i == 0) {
                fail.x = point.x;
                fail.y = point.y;
            }

            if(i == 3) {
                fail.z = point.x;
                fail.w = point.y;
            }
        }

        if(legal) {
            this.transformation.set(finishedTransformation);
        } else {
            onFail.accept(fail);
        }
    }

    private boolean isInBound(float x, float y) {
        return !( (x > 0 && x < this.xSize) || (y > 0 && y < this.ySize) );
    }

    private Vec2f getTransformedPoint(float x, float y, Matrix4f transformation) {
        Point3f point = new Point3f( x - this.xSize / 2, y - this.xSize / 2, 0);
        transformation.transform(point);
        return new Vec2f(point.x + this.xSize / 2, point.y + this.ySize / 2);
    }

    @RequiredArgsConstructor
    public class ScrollEntry implements GuiScrollboxEntry {

        private final BlockPos pos;
        private final String name;

        @Override
        public void draw(int x, int y, int mouseX, int mouseY) {
            Minecraft.getMinecraft().fontRenderer.drawString(this.name, x + 5, y + 4, -1);
        }

        @Override
        public void postDraw(int x, int y, int mouseX, int mouseY) {
            if(mouseX - x >= 0 && mouseX - x <= 100 && mouseY - y >= 0 && mouseY - y <= 15) {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
                GlStateManager.enableDepth();
                GuiUtils.drawHoveringText(ItemStack.EMPTY, Collections.singletonList(this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ()), mouseX, mouseY, xSize, ySize, -1, Minecraft.getMinecraft().fontRenderer);
                GlStateManager.enableDepth();
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1F, 1F, 1F,1F);
            }
        }

        @Override
        public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            transformation.setIdentity();
            selected = null;
            ProjectNublar.NETWORK.sendToServer(new C30TrackingTabletEntryClicked(this.pos));
            return true;
        }
    }
}
