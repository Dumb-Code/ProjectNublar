package net.dumbcode.projectnublar.client.gui.tablet;

import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.util.EnumHand;

public abstract class BaseBackgroundTabletScreen extends BaseTabletScreen implements BackgroundableScreen {

    private TabletBackground background;

    protected BaseBackgroundTabletScreen(EnumHand hand) {
        super(hand);
    }

    @Override
    public void setBackground(TabletBackground background) {
        this.background = background;
    }

    @Override
    public TabletBackground getBackground() {
        return this.background;
    }

    @Override
    protected void drawTabletScreen(int mouseX, int mouseY, float partialTicks) {
        this.background.render(this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight, mouseX, mouseY);
    }
}
