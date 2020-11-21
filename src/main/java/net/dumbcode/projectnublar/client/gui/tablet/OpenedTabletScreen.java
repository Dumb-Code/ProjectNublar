package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.Getter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class OpenedTabletScreen extends BaseTabletScreen {

    @Getter
    private TabletPage screen;

    public OpenedTabletScreen(EnumHand hand) {
        super(hand);
    }

    public void setScreen(TabletPage screen, String route) {
        this.screen = screen;
        this.route = route;
        this.screen.setData(this.tabletWidth, this.tabletHeight, route);
    }

    @Override
    public void initGui() {
        super.initGui();
        if(this.screen != null) {
            this.screen.setData(this.tabletWidth, this.tabletHeight, this.route);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void drawTabletScreen(int mouseX, int mouseY, float partialTicks) {
        if(this.screen != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.leftStart, this.topStart, 0);
            this.screen.render(mouseX - this.leftStart, mouseY - this.topStart, partialTicks, "");
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void updateScreen() {
        if(this.screen != null) {
            this.screen.updateScreen();
        }
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(this.screen != null) {
            this.screen.onKeyTyped(typedChar, keyCode);
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if(this.screen != null) {
            this.screen.onKeyInput();
        }
        super.handleKeyboardInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.screen != null) {
            this.screen.onMouseClicked(mouseX - this.leftStart, mouseY - this.topStart, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if(this.screen != null) {
            this.screen.onMouseClickMove(mouseX - this.leftStart, mouseY - this.topStart, clickedMouseButton, timeSinceLastClick);
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(this.screen != null) {
            this.screen.onMouseReleased(mouseX - this.leftStart, mouseY - this.topStart, state);
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        if(this.screen != null) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            this.screen.onMouseInput(mouseX - this.leftStart, mouseY - this.topStart);
        }
        super.handleMouseInput();
    }

    @Override
    public void onGuiClosed() {
        if(this.screen != null) {
            this.screen.onClosed();
        }
        super.onGuiClosed();
    }
}
