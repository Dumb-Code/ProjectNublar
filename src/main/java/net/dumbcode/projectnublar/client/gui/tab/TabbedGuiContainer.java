package net.dumbcode.projectnublar.client.gui.tab;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Getter
public abstract class TabbedGuiContainer<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> implements MenuAccess<T> {

    private final TabInformationBar info;

    public TabbedGuiContainer(T inventorySlotsIn, Inventory playerInventory, Component title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title);
        this.info = bar;
    }

    @Override
    public void init() {
        super.init();
//        this.info.configurePageSelect(this.width);
//        this.topPos += this.getOffset();
    }

    protected int getOffset() {
        return 10;
    }

//    @Override
//    public void tick() {
//        super.tick();
//        this.info.update();
//    }

    @Override
    public void render(GuiGraphics p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
//        this.info.render(p_230430_1_, this.leftPos, this.width, this.topPos);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
//        this.info.mouseClicked(this.leftPos, this.width, this.topPos, mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }


}
