package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C23ConfirmTrackingTablet;
import net.dumbcode.projectnublar.server.network.C25StopTrackingTablet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

public class GuiTrackingTablet extends GuiScreen {

    private final int startX;
    private final int startZ;

    private final int textureWidth;
    private final int textureHeight;

    private final DynamicTexture texture;
    private final ResourceLocation location;

    public GuiTrackingTablet(int startX, int startZ, int textureWidth, int textureHeight) {
        this.startX = startX;
        this.startZ = startZ;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.texture = new DynamicTexture(textureWidth, textureHeight);
        this.location = Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation("tracking_tablet_map", this.texture);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(this.location);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        drawScaledCustomSizeModalRect(this.width/2 - 100, this.height/2 - 100, 0, 0, 1, 1, 200, 200, 1F, 1F);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        this.mc.renderEngine.deleteTexture(this.location);
        this.texture.deleteGlTexture();

        ProjectNublar.NETWORK.sendToServer(new C25StopTrackingTablet());
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public void setRGB(int startX, int startZ, int width, int height, int[] setIntoArray) {

        int arrayStartX = startX - this.startX;
        int arrayStartZ = startZ - this.startZ;

        int off = 0;
        for (int z = arrayStartZ; z < arrayStartZ+height; z++) {
            for (int x = arrayStartX; x < arrayStartX+width; x++) {
                this.texture.getTextureData()[x + z*this.textureWidth] = setIntoArray[off++];
            }
        }

        this.texture.updateDynamicTexture();
    }

}
