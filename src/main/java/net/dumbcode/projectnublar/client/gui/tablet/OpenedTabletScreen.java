package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.Hand;

public class OpenedTabletScreen extends BaseTabletScreen {

    @Getter
    private TabletPage screen;

    public OpenedTabletScreen(Hand hand) {
        super(hand);
    }

    public void setScreen(TabletPage screen, String route) {
        this.children.clear();
        this.screen = this.addWidget(screen);
        this.route = route;
        this.screen.setData(this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight, route);
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
    protected void drawTabletScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
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
