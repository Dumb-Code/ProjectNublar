package net.dumbcode.projectnublar.client.gui.tab;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.io.IOException;

public abstract class TabbedGui extends GuiContainer {

    private final TabListInformation info;

    public TabbedGui(Container inventorySlotsIn, TabListInformation list) {
        super(inventorySlotsIn);
        this.info = list;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.info.configurePageSelect();
        this.guiTop += this.getOffset();
    }

    protected int getOffset() {
        return 10;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.info.update();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.info.render(mouseX, mouseY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.info.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public abstract static class Tab {
        public boolean isDirty() {
            return false;
        }
        public abstract void onClicked();//Mouse X/Y ?
    }

    public TabListInformation getInfo() {
        return info;
    }
}
