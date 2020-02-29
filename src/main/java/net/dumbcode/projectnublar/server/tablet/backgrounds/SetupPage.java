package net.dumbcode.projectnublar.server.tablet.backgrounds;

public interface SetupPage<T extends TabletBackground> {
    int getWidth();
    int getHeight();

    void render(int x, int y, int mouseX, int mouseY);

    default void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }

    default void mouseClickMove(int x, int y, int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
    }

    default void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
    }

    T create();
}
