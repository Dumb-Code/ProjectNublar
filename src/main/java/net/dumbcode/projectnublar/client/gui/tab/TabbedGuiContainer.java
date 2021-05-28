package net.dumbcode.projectnublar.client.gui.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class TabbedGuiContainer<T extends Container> extends ContainerScreen<T> implements IHasContainer<T> {

    private final TabInformationBar info;

    public TabbedGuiContainer(T inventorySlotsIn, PlayerInventory playerInventory, String containerName) {
        super(inventorySlotsIn, playerInventory, new TranslationTextComponent(ProjectNublar.MODID + "." + containerName + ".name"));
        this.info = list;
    }

    @Override
    public void init() {
        super.init();
        this.info.configurePageSelect(this.width);
        this.topPos += this.getOffset();
    }

    protected int getOffset() {
        return 10;
    }

    @Override
    public void tick() {
        super.tick();
        this.info.update();
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
        this.info.render(p_230430_1_, this.leftPos, this.width, this.topPos);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.info.mouseClicked(this.leftPos, this.width, this.topPos, mouseX, mouseY, mouseButton);

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    public TabInformationBar getInfo() {
        return info;
    }
}
