package net.dumbcode.projectnublar.client.gui.tablet.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.gui.tablet.TabletPage;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingDataInformation;
import net.dumbcode.projectnublar.server.entity.tracking.TrackingSavedData;
import net.dumbcode.projectnublar.server.network.C2SConfirmTrackingTablet;
import net.dumbcode.projectnublar.server.network.C25StopTrackingTablet;
import net.dumbcode.projectnublar.server.network.C2STrackingTabletEntryClicked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
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

    private DynamicTexture texture;

    private final Matrix4f transformation = new Matrix4f();

    private List<TrackingSavedData.DataEntry> trackingData = new ArrayList<>();
    private TrackingSavedData.DataEntry selected;

    public TrackingTabletScreen(List<Pair<BlockPos, String>> entries) {
        this.scrollEntries = entries.stream().map(pair -> new ScrollEntry(pair.getKey(), pair.getRight())).collect(Collectors.toList());
        this.transformation.setIdentity();
    }

    @Override
    public void setData(int left, int top, int xSize, int ySize, String route) {
        super.setData(left, top, xSize, ySize, route);
        this.scrollBox = this.add(new GuiScrollBox<>(this.left + this.xSize - 100, this.top + 20, 100, 15, (this.ySize - 30) / 15, () -> this.scrollEntries));
    }

    public void initializeSize(int startX, int startZ, int textureWidth, int textureHeight) {
        this.startX = startX;
        this.startZ = startZ;

        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;

        if(this.texture != null) {
            this.texture.releaseId();
        }

        this.texture = new DynamicTexture(textureWidth, textureHeight, true);

        ProjectNublar.NETWORK.sendToServer(new C2SConfirmTrackingTablet());
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {
        if(this.texture != null) {

            stack.pushPose();

            stack.translate(this.left + this.xSize / 2F, this.top + this.ySize / 2F, 50);
            Matrix4f pose = stack.last().pose();
            pose.multiply(this.transformation);


            this.texture.bind();
            RenderSystem.disableBlend();

            int rad = Math.max(this.xSize, this.ySize);
            RenderUtils.drawScaledCustomSizeModalRect(stack,-rad / 2, -rad / 2, 0, 0, 1, 1, this.xSize, this.xSize, 1F, 1F);
            stack.popPose();

            for (TrackingSavedData.DataEntry datum : this.trackingData) {
                Vector2f point = this.getPoint(datum);
                //Debug
                RenderUtils.renderBorderExclusive(stack, (int) point.x - 1, (int) point.y - 1, (int) point.x + 1, (int) point.y + 1, 2, 0xFFFF0000);
                datum.getInformation().forEach(i -> i.renderMap(stack, (int) point.x, (int) point.y));
            }
            if(this.selected != null) {
                this.renderTooltip(stack, mouseX - left, mouseY - top);
            }
        }

        this.scrollBox.render(stack, mouseX, mouseY, partialTicks);
    }

    private void renderTooltip(MatrixStack stack, int mouseX, int mouseY) {
        int padding = 10;
        int borderSize = 2;

        List<Dimension> dimensions = this.selected.getInformation().stream().map(TrackingDataInformation::getInfoDimensions).collect(Collectors.toList());
        double width = 2*padding + dimensions.stream().mapToDouble(Dimension::getWidth).reduce(Math::max).orElseThrow(NoSuchElementException::new);
        double height = padding + dimensions.stream().mapToDouble(Dimension::getHeight).map(d -> d != 0 ? d + padding : 0).sum();
        AbstractGui.fill(stack, 0, 15, (int) width, (int) height + 15, 0xFF7A7A7A);
        RenderUtils.renderBorderExclusive(stack, borderSize, 15 + borderSize, (int) width - borderSize, (int) height + 15 - borderSize, borderSize, 0xFF333333);
        double yCoord = 15D + padding;
        for (TrackingDataInformation info : this.selected.getInformation()) {
            Dimension d = info.getInfoDimensions();
            int relx = mouseX - padding - left;
            int rely = mouseY - (int) yCoord - top;
            boolean over = MathUtils.inBetween(relx, 0, d.width) && MathUtils.inBetween(rely, 0, d.height);
            if(d.height != 0) {
                info.renderInfo(stack, padding, (int) yCoord, over ? relx : -1, over ? rely : -1);
                yCoord += d.height + padding;

            }
        }
    }

    private Optional<TrackingSavedData.DataEntry> getEntryUnder(double x, double y, int maxDistFromPoint) {
        TrackingSavedData.DataEntry nearest = null;
        double nearestDist = Integer.MAX_VALUE;

        x -= left;
        y -= top;

        for (TrackingSavedData.DataEntry datum : this.trackingData) {
            Vector2f point = this.getPoint(datum);
            double dist = (point.x - x)*(point.x - x) + (point.y - y)*(point.y - y);
            if(dist < nearestDist) {
                nearestDist = dist;
                nearest = datum;
            }
        }

        return nearestDist <= maxDistFromPoint*maxDistFromPoint ? Optional.ofNullable(nearest) : Optional.empty();
    }

    private Vector2f getPoint(TrackingSavedData.DataEntry entry) {
        float x = (float) ((entry.getPosition().x - this.startX) / this.textureWidth * this.xSize);
        float y = (float) ((entry.getPosition().z - this.startZ) / this.textureHeight * this.xSize);

        return this.getTransformedPoint(x, y, this.transformation);
    }

    @Override
    public void onClosed() {
        if(this.texture != null) {
            this.texture.close();
        }
        ProjectNublar.NETWORK.sendToServer(new C25StopTrackingTablet());
    }

    public void setTrackingData(List<TrackingSavedData.DataEntry> trackingData) {
        this.trackingData = trackingData;
        this.selected = trackingData.stream().filter(d -> this.selected != null && d.getUuid().equals(this.selected.getUuid())).findAny().orElse(null);
        if(this.selected != null) {
            Vector2f point = this.getPoint(this.selected);
            this.translateWithZoom(matrix4f -> matrix4f.setTranslation(-point.x + this.xSize / 2F, 0, 0));
            this.translateWithZoom(matrix4f -> matrix4f.setTranslation(0, -point.y + this.ySize / 2F, 0));
        }
    }

    public void setRGB(int startX, int startZ, int width, int height, int[] setIntoArray) {
        NativeImage pixels = this.texture.getPixels();
        if(pixels != null) {
            for (int x = 0; x < width; x++) {
                int posX = startX - this.startX + x;
                for (int z = 0; z < height; z++) {
                    int posZ = startZ - this.startZ + z;
                    int color = setIntoArray[z*width + x];
                    int r = (color >> 16 & 255);
                    int g = (color >> 8 & 255);
                    int b = (color & 255);
                    pixels.setPixelRGBA(posX, posZ, 0xFF000000 | b << 16 | g << 8 | r);
                }
            }
            TextureUtils.uploadSubArea(this.texture, startX - this.startX, startZ - this.startZ, width, height);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        if(!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            this.selected = this.selected != null ? null : this.getEntryUnder(mouseX, mouseY, 10).orElse(null);
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int clickedMouseButton, double changeX, double changeY) {
        if(!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize()) && clickedMouseButton == 0 && this.selected == null) {
            this.tryMulMatrix(matrix4f -> matrix4f.setTranslation((float) changeX, 0, 0), fail -> {});
            this.tryMulMatrix(matrix4f -> matrix4f.setTranslation(0, (float) changeY, 0), fail -> {});
        }

        return super.mouseDragged(mouseX, mouseY, clickedMouseButton, changeX, changeY);
    }
    private void translateWithZoom(Consumer<Matrix4f> mat) {
        float modifier = 0.75F;
        this.tryMulMatrix(mat, fail -> {
            this.tryMulMatrix(m -> m.multiply(Matrix4f.createScaleMatrix(1F/modifier, 1F/modifier, 1F/modifier)), impossibleFails -> {});
            this.translateWithZoom(mat);
        });
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollIn) {
        if (!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            float scroll = (float) (scrollIn * 0.75F);
            float mod = scroll < 0 ? -scroll : 1 / scroll;

            //Ignore disgusting code below. In the future maybe remove the matrix stuff, it don't really be needed
            //Diff between the mouse and the center of the tablet.
            float x = (float)mouseX - (this.left+this.xSize/2F);
            float y = (float)mouseY - (this.top+this.ySize/2F);
            this.tryMulMatrix(matrix4f -> {
                matrix4f.multiply(Matrix4f.createTranslateMatrix(x, y, 0));
                matrix4f.multiply(Matrix4f.createScaleMatrix(mod, mod, mod));
                matrix4f.multiply(Matrix4f.createTranslateMatrix(-x, -y, 0));
            }, fail -> {
                if(this.selected != null || scroll > 0) {
                    return;
                }

                boolean leftOut = fail.x() > 0;
                boolean rightOut = fail.z() < this.xSize;

                boolean topOut = fail.y() > 0;
                boolean bottomOut = fail.w() < this.ySize;


                float xOffset = 0;
                if(leftOut != rightOut) {
                    if(leftOut) {
                        xOffset = -fail.x();
                    } else {
                        xOffset = this.xSize - fail.z();
                    }
                }

                float yOffset = 0;
                if(topOut != bottomOut) {
                    if(topOut) {
                        yOffset = -fail.y();
                    } else {
                        yOffset = this.ySize - fail.w();
                    }
                }

                float finalXOff = xOffset;
                float finalYOff = yOffset;

                this.tryMulMatrix(matrix4f -> {
                    matrix4f.set(Matrix4f.createTranslateMatrix(finalXOff, finalYOff, 0));
                    matrix4f.multiply(Matrix4f.createTranslateMatrix(x, y, 0));
                    matrix4f.multiply(Matrix4f.createScaleMatrix(mod, mod, mod));
                    matrix4f.multiply(Matrix4f.createTranslateMatrix(-x, -y, 0));
                }, failImpossible -> { /*Throw something?*/ });
            });
            return true;
        }
        return super.mouseScrolled(mouseX, mouseX, scrollIn);
    }


    private void tryMulMatrix(Consumer<Matrix4f> mat, Consumer<Vector4f> onFail) {
        Matrix4f newTransformation = new Matrix4f();
        newTransformation.setIdentity();
        mat.accept(newTransformation);

        Matrix4f finishedTransformation = new Matrix4f();
        finishedTransformation.set(newTransformation);
        finishedTransformation.multiply(this.transformation);

        // (x, y)
        //
        //          (z, w)
        Vector4f fail = new Vector4f();
        boolean legal = true;

        for (int i = 0; i < 4; i++) {
            Vector2f point = this.getTransformedPoint((i & 1) == 0 ? 0 : this.xSize, (i & 2) == 0 ? 0 : this.xSize, finishedTransformation);
            legal &= this.isInBound(point.x, point.y);

            if(i == 0) {
                fail.setX(point.x);
                fail.setY(point.y);
            }

            if(i == 3) {
                fail.setZ(point.x);
                fail.setW(point.y);
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

    private Vector2f getTransformedPoint(float x, float y, Matrix4f transformation) {
        Vector4f point = new Vector4f( x - this.xSize / 2F, y - this.xSize / 2F, 0, 1);
        point.transform(transformation);
        return new Vector2f(point.x() + this.xSize / 2F, point.y() + this.ySize / 2F);
    }

    @RequiredArgsConstructor
    public class ScrollEntry implements GuiScrollboxEntry {

        private final BlockPos pos;
        private final String name;

        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            Minecraft.getInstance().font.draw(stack, this.name, x + 5, y + 4, -1);
        }

        @Override
        public void postDraw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY) {
            if(mouseX - x >= 0 && mouseX - x <= 100 && mouseY - y >= 0 && mouseY - y <= 15) {
                GL11.glDisable(GL11.GL_STENCIL_TEST);
                RenderSystem.enableDepthTest();
                GuiUtils.drawHoveringText(stack, Collections.singletonList(new StringTextComponent(this.pos.getX() + ", " + this.pos.getY() + ", " + this.pos.getZ())), mouseX, mouseY, xSize, ySize, -1, Minecraft.getInstance().font);
                RenderSystem.enableDepthTest();
                GL11.glEnable(GL11.GL_STENCIL_TEST);
                RenderHelper.setupForFlatItems();
                RenderSystem.color4f(1F, 1F, 1F,1F);
            }
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            transformation.setIdentity();
            selected = null;
            ProjectNublar.NETWORK.sendToServer(new C2STrackingTabletEntryClicked(this.pos));
            return true;
        }

    }
}
