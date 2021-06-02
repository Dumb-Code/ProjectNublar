package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TabletScreen implements INestedGuiEventHandler {
    protected static final Minecraft MC = Minecraft.getInstance();
    protected int xSize;
    protected int ySize;
    private final List<IGuiEventListener> children = new ArrayList<>();

    @Nullable
    @Getter @Setter private IGuiEventListener focused;

    @Getter @Setter private boolean dragging;

    public void setData(int xSize, int ySize) {
        this.xSize = xSize;
        this.ySize = ySize;
    }

    protected <T extends IGuiEventListener> T add(T t) {
        this.children.add(t);
        return t;
    }

    public void onSetAsCurrentScreen() { }

    public void onClosed() { }

    public void updateScreen() {  }

    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {}


    @Override
    public List<? extends IGuiEventListener> children() {
        return this.children;
    }
}
