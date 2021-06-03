package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.minecraft.util.Hand;

public class OpenedTabletScreen extends BaseTabletScreen {

    @Getter
    private TabletScreen screen;

    public OpenedTabletScreen(Hand hand) {
        super(hand);
    }

    public void setScreen(TabletScreen screen) {
        this.screen = screen;
        this.screen.setData(this.leftStart, this.topStart, this.tabletWidth, this.tabletHeight);
    }

    @Override
    public void init() {
        super.init();
        if(this.screen != null) {
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
            stack.pushPose();
            stack.translate(this.leftStart, this.topStart, 0);
            this.screen.render(stack, mouseX - this.leftStart, mouseY - this.topStart, partialTicks);
            stack.popPose();
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
