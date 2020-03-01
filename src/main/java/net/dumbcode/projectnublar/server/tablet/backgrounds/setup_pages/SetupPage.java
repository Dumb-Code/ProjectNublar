package net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages;

import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.client.gui.GuiButton;

public interface SetupPage<T extends TabletBackground> {
    int getWidth();
    int getHeight();

    void render(int x, int y, int mouseX, int mouseY);

    default void initPage(int x, int y) {
    }

    default void update() {
    }

    default void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }

    default void mouseClickMove(int x, int y, int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
    }

    default void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }

    default void keyTyped(char typedChar, int keyCode) {
    }

    T create();
}
