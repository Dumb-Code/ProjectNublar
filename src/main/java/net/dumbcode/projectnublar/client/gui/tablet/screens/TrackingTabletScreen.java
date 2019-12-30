package net.dumbcode.projectnublar.client.gui.tablet.screens;

import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.TextureUtils;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.client.gui.tablet.TabletScreen;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C23ConfirmTrackingTablet;
import net.dumbcode.projectnublar.server.network.C25StopTrackingTablet;
import net.dumbcode.projectnublar.server.network.C30TrackingTabletEntryClicked;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Matrix4f;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrackingTabletScreen extends TabletScreen {

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
    private Matrix4f transformation = new Matrix4f();
    private FloatBuffer buffer = BufferUtils.createFloatBuffer(16);


    public TrackingTabletScreen(List<Pair<BlockPos, String>> entries) {
        this.scrollEntries = entries.stream().map(pair -> new ScrollEntry(pair.getKey(), pair.getRight())).collect(Collectors.toList());
        this.transformation.setIdentity();
    }

    @Override
    public void setData(int xSize, int ySize) {
        super.setData(xSize, ySize);
        this.scrollBox = new GuiScrollBox<>(this.xSize - 100, 20, 100, 15, (this.ySize - 30) / 15, () -> this.scrollEntries);
    }

    public void initilizeSize(int startX, int startZ, int textureWidth, int textureHeight) {
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
    public void onSetAsCurrentScreen() {
        super.onSetAsCurrentScreen();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {

        if(this.textureID != -1) {
            GlStateManager.pushMatrix();

            this.putInFloat();
            GlStateManager.translate(this.xSize / 2F, this.ySize / 2F, 0);
            GlStateManager.multMatrix(this.buffer);


            GlStateManager.bindTexture(this.textureID);
            GlStateManager.disableAlpha();
            GlStateManager.disableBlend();

            Gui.drawScaledCustomSizeModalRect(-this.ySize / 2, -this.ySize / 2, 0, 0, 1, 1, this.ySize, this.ySize, 1F, 1F);

            GlStateManager.popMatrix();
        }

        this.scrollBox.render(mouseX, mouseY);
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
    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(!this.scrollBox.isMouseOver(mouseX, mouseY, this.scrollBox.getTotalSize())) {
            if(clickedMouseButton == 0) {
                Matrix4f newTransformation = new Matrix4f();
                newTransformation.setIdentity();

                int deltaX = mouseX - this.prevClickedX;
                int deltaY = mouseY - this.prevClickedY;

                newTransformation.m03 = deltaX;
                newTransformation.m13 = deltaY;

                this.transformation.mul(newTransformation, this.transformation);
            }
        }


        this.prevClickedX = mouseX;
        this.prevClickedY = mouseY;
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
                Matrix4f newTransformation = new Matrix4f();
                newTransformation.setIdentity();
                newTransformation.setScale(mod);
                this.transformation.mul(newTransformation, this.transformation);
            }
        }
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
        public void onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            ProjectNublar.NETWORK.sendToServer(new C30TrackingTabletEntryClicked(this.pos));
        }
    }
}
