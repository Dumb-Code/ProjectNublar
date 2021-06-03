package net.dumbcode.projectnublar.server.tablet.backgrounds.setuppages;

import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;

public interface SetupPage<T extends TabletBackground> {
    int getWidth();
    int getHeight();

    void render(int x, int y, int mouseX, int mouseY);

    default void setupFromPage(T page) {
    }

    default void initPage(int x, int y) {
    }

    default void updatePage(int x, int y) {
    }

    default void mouseClicked(int x, int y, double mouseX, double mouseY, int mouseButton) {
    }

    default void mouseDragged(int x, int y, double mouseX, double mouseY, int mouseButton, double changeX, double changeY) {
    }

    default void mouseReleased(int x, int y, double mouseX, double mouseY, int mouseButton) {
    }

    default void keyPressed(int keyCode, int scanCode, int modifiers) {
    }

    default void mouseScrolled(int x, int y, double mouseX, double mouseY, double scroll) {
    }

    T create();
}
