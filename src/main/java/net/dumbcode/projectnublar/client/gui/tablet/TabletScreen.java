package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TabletScreen implements ContainerEventHandler {
    protected static final Minecraft MC = Minecraft.getInstance();
    protected int left;
    protected int top;
    protected int xSize;
    protected int ySize;
    private final List<GuiEventListener> children = new ArrayList<>();

    @Nullable
    @Getter @Setter private GuiEventListener focused;

    @Getter @Setter private boolean dragging;

    public void setData(int left, int top, int xSize, int ySize) {
        this.left = left;
        this.top = top;
        this.xSize = xSize;
        this.ySize = ySize;
    }

    protected <T extends GuiEventListener> T add(T t) {
        this.children.add(t);
        return t;
    }

    public void onSetAsCurrentScreen() { }

    public void onClosed() { }

    public void updateScreen() {  }

    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.left && mouseX <= this.left + this.xSize &&
            mouseY >= this.top && mouseY <= this.top + this.ySize;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }
}
