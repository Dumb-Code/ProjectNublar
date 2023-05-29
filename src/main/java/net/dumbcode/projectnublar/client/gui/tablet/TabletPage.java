package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabletPage implements INestedGuiEventHandler {
    protected static final Minecraft MC = Minecraft.getInstance();
    protected int left;
    protected int top;
    protected int xSize;
    protected int ySize;
    protected String route;
    private final List<IGuiEventListener> children = new ArrayList<>();

    @Nullable
    @Getter @Setter private IGuiEventListener focused;

    @Getter @Setter private boolean dragging;

    public void setData(int left, int top, int xSize, int ySize, String route) {
        this.left = left;
        this.top = top;
        this.xSize = xSize;
        this.ySize = ySize;
        this.route = route;
    }

    public void navigateRoute(String link) {
        String[] linkPieces = link.split("/");
        List<String> routePieces = new ArrayList<String>();
        if(link.startsWith("/")) {
            this.route = this.route.split("/")[0].concat(link);
            if(!link.endsWith("/")) this.route = this.route.concat("/");
        } else {
            Collections.addAll(routePieces, this.route.split("/"));
            for(int i = 0; i < linkPieces.length; i++) {
                if(linkPieces[i].equals("..")) {
                    if(routePieces.size() > 1)
                        routePieces.remove(routePieces.size() - 1);
                } else if(linkPieces[i].endsWith(":")) {
                    // We're swapping to a new screen

                } else {
                    routePieces.add(linkPieces[i]);
                }
            }
            // Add an extra empty string so that we get the final '/'
            routePieces.add("");
            this.route = String.join("/", routePieces);
        }
    }

    protected <T extends IGuiEventListener> T add(T t) {
        this.children.add(t);
        return t;
    }

    public void onSetAsCurrentScreen() { }

    public void onClosed() { }

    public void updateScreen() {  }

    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks, String route) {}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.left && mouseX <= this.left + this.xSize &&
            mouseY >= this.top && mouseY <= this.top + this.ySize;
    }

    @Override
    public List<? extends IGuiEventListener> children() {
        return this.children;
    }
}
