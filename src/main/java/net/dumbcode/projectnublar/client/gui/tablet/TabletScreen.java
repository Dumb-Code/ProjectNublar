package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.Setter;
import net.minecraft.client.Minecraft;

public  class TabletScreen {
    protected static final Minecraft MC = Minecraft.getMinecraft();
    protected int xSize;
    protected int ySize;

    public void setData(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    public void onSetAsCurrentScreen() {
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
    }

    public void updateScreen() {
    }

    public void onMouseInput(int mouseX, int mouseY) {
    }

    public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
    }

    public void onMouseReleased(int mouseX, int mouseY, int state) {
    }

    public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
    }

    public void onKeyInput() {
    }

    public void onKeyTyped(char typedChar, int keyCode) {
    }

    public void onClosed() {
    }


}
