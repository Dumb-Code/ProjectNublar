package net.dumbcode.projectnublar.client.gui.tab;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.io.IOException;

public abstract class TabbedGuiContainer extends GuiContainer {

    private final TabInformationBar info;

    public TabbedGuiContainer(Container inventorySlotsIn, TabInformationBar list) {
        super(inventorySlotsIn);
        this.info = list;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.info.configurePageSelect(this.xSize);
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
        this.info.render(this.guiLeft, this.xSize, this.guiTop);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.info.mouseClicked(this.guiLeft, this.xSize, this.guiTop, mouseX, mouseY, mouseButton);
    }

    public TabInformationBar getInfo() {
        return info;
    }
}
