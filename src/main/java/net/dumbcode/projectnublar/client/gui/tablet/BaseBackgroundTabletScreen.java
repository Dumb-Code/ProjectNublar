package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.util.Hand;

public abstract class BaseBackgroundTabletScreen extends BaseTabletScreen implements BackgroundableScreen {

    private TabletBackground background;

    protected BaseBackgroundTabletScreen(Hand hand) {
        super(hand);
    }

    @Override
    public void setBackground(TabletBackground background) {
        if(this.background != null) {
            this.background.dispose();
        }
        this.background = background;
    }

    @Override
    public TabletBackground getBackground() {
        return this.background;
    }

    protected  <T extends BaseBackgroundTabletScreen> T transferBackground(T screen) {
        screen.setBackground(this.background);
        this.background = null; //We don't want to dispose
        return screen;
    }

    @Override
    public void onClose() {
        if(this.background != null) {
            this.background.dispose();
        }
        super.onClose();
    }

    @Override
    protected void drawTabletScreen(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        if(this.background != null) {
            this.background.render(stack, this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight, mouseX, mouseY);
        }
    }
}
