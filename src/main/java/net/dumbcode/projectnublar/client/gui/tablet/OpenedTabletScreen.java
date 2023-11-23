package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;

@Getter
public class OpenedTabletScreen extends BaseTabletScreen {

    private TabletScreen screen;

    public OpenedTabletScreen(InteractionHand hand) {
        super(hand);
    }

    public void setScreen(TabletScreen screen) {
        this.children().clear();
        this.screen = this.addWidget(screen);
        this.screen.setData(this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight);
    }

    @Override
    public void init() {
        super.init();
        if(this.screen != null) {
            this.addWidget(this.screen);
            this.screen.setData(this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void drawTabletScreen(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        if(this.screen != null) {
            this.screen.render(stack, mouseX, mouseY, partialTicks);
        }
    }


    @Override
    public void tick() {
        if(this.screen != null) {
            this.screen.updateScreen();
        }
        super.tick();
    }

    @Override
    public void onClose() {
        if(this.screen != null) {
            this.screen.onClosed();
        }
        super.onClose();
    }
}
