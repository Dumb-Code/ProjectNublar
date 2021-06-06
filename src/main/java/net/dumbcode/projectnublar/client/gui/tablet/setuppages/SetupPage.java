package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class SetupPage<T extends TabletBackground> implements INestedGuiEventHandler {
    private final int width;
    private final int height;

    protected int x;
    protected int y;

    private final List<IGuiEventListener> children = new ArrayList<>();
    private IGuiEventListener focused;
    private boolean dragging;

    public abstract T create();

    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {

    }

    public void setupFromPage(T page) {
    }

    public void initPage(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void updatePage() {
    }

    public <E extends IGuiEventListener> E add(E element) {
        this.children.add(element);
        return element;
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        return this.children;
    }

    @Override
    public void setFocused(@Nullable IGuiEventListener listener) {
        this.focused = listener;
    }

    @Nullable
    @Override
    public IGuiEventListener getFocused() {
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
