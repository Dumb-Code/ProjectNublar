package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.client.gui.GuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class SetupPage<T extends TabletBackground> implements INestedGuiEventHandler {
    private final int width;
    private final int height;

    protected int x;
    protected int y;

    private final List<GuiEventListener> children = new ArrayList<>();
    private GuiEventListener focused;
    private boolean dragging;

    public abstract T create();

    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {

    }

    public void onFilesDrop(List<Path> files) {

    }

    public void onClose() {

    }

    public void setupFromPage(T page) {
    }

    public void initPage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void updatePage() {
    }

    public <E extends GuiEventListener> E add(E element) {
        this.children.add(element);
        return element;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        this.focused = listener;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public boolean isDragging() {
        return this.dragging;
    }
}
