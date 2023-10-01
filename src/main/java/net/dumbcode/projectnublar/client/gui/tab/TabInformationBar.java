package net.dumbcode.projectnublar.client.gui.tab;

import com.mojang.blaze3d.matrix.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class TabInformationBar {

    private static final int DEFAULT_TAB_WIDTH = 28;
    private static final int DEFAULT_TAB_PADDING = 5;
    private static final int DEFAULT_TAB_HEIGHT = 27;

    private final Supplier<List<Tab>> list;

    private final int tabWidth;
    private final int tabPadding;
    private final int tabHeight;

    private List<Tab> cache;

    private int selectedIndex;
    private int pageOffset;

    public TabInformationBar(Supplier<List<Tab>> list, int tabWidth, int tabPadding, int tabHeight) {
        this.list = list;
        this.cache = this.list.get();

        this.tabWidth = tabWidth;
        this.tabPadding = tabPadding;
        this.tabHeight = tabHeight;
    }

    public TabInformationBar(Supplier<List<Tab>> list) {
        this(list, DEFAULT_TAB_WIDTH, DEFAULT_TAB_PADDING, DEFAULT_TAB_HEIGHT);
    }

    public void update() {
        for (Tab tab : this.cache) {
            if (tab.isDirty()) {
                this.cache = this.list.get();
                break;
            }
        }
    }

    public void render(GuiGraphics stack, int guiLeft, int guiWidth, int guiTop) {
        int nextSize = 20;

        int fitAmount = (guiWidth - nextSize - nextSize - this.tabPadding) / (this.tabWidth + this.tabPadding);
        int offset = guiLeft + (guiWidth + this.tabPadding - fitAmount * (this.tabWidth + this.tabPadding)) / 2;

        for (int i = 0; i < fitAmount && i + this.pageOffset < this.cache.size(); i++) {
            int xStart = offset + (this.tabWidth + this.tabPadding) * i;
            this.drawTab(stack, xStart, guiTop, this.selectedIndex == i + this.pageOffset);
        }
        FontRenderer font = Minecraft.getInstance().font;
        String text = String.format("Page %s/%s", this.pageOffset / fitAmount + 1, Math.round(this.cache.size() / (float)fitAmount)); // TODO: 11/11/2018 localize
        stack.drawString(font, text, guiLeft + (guiWidth - font.width(text)) / 2F, guiTop - this.tabHeight - font.lineHeight - 2, -1);

        int top = guiTop - this.tabHeight + (this.tabHeight - nextSize) / 2;
        if(this.pageOffset != 0) {
            AbstractGui.stack.fill(guiLeft, top, guiLeft + nextSize, top + nextSize, -1);
        }
        if(this.pageOffset + fitAmount < this.cache.size()) {
            AbstractGui.stack.fill(guiLeft + guiWidth - nextSize, top, guiLeft + guiWidth, top + nextSize, -1);
        }
    }

    protected void drawTab(GuiGraphics stack, int xStart, int yStart, boolean selected) {
        int tabExtension = selected ? 5 : 1;
        Minecraft.getInstance().textureManager.bind(new ResourceLocation("textures/gui/container/creative_inventory/tabs.png"));
        AbstractGui.stack.blit(xStart, yStart - this.tabHeight - 1, 28, selected ? 32 : 0, this.tabWidth, this.tabHeight + tabExtension, 256, 256);
//      Gui.drawRect(xStart, guiTop - this.tabHeight, xStart + this.tabWidth, guiTop + 5, 0xFF000000 | new Random((i + this.pageOffset) << 21).nextInt());
//      if(mouseY > guiTop - this.tabHeight && mouseY < guiTop + tabExtension && mouseX > xStart && mouseX < xStart + this.tabWidth) {
//          Gui.drawRect(xStart, guiTop - this.tabHeight, xStart + this.tabWidth, guiTop + tabExtension, 0x6A0063FF); TODO: Remove this?
//      }
    }

    public void mouseClicked(int guiLeft, int guiWidth, int guiTop, double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0) {
            int nextSize = 20;

            int fitAmount = (guiWidth - nextSize - nextSize - this.tabPadding) / (this.tabWidth + this.tabPadding);
            int offset = guiLeft + (guiWidth + this.tabPadding - fitAmount * (this.tabWidth + this.tabPadding)) / 2;

            for (int i = 0; i < fitAmount && i + this.pageOffset < this.cache.size(); i++) {
                int xStart = offset + (this.tabWidth + this.tabPadding) * i;
                int tabExtension = this.selectedIndex == i + this.pageOffset ? 5 : 1;
                if(mouseY > guiTop - this.tabHeight && mouseY < guiTop + tabExtension && mouseX > xStart && mouseX < xStart + this.tabWidth) {
                    this.selectedIndex = this.pageOffset + i;
                    this.cache.get(i + this.pageOffset).onClicked();
                    break;
                }
            }

            int top = guiTop - this.tabHeight + (this.tabHeight - nextSize) / 2;

            double relX = mouseX - guiLeft;
            double relY = mouseY - top;

            if(this.pageOffset != 0 && relX > 0 && relX < nextSize && relY > 0 && relY < nextSize) {
                this.pageOffset -= fitAmount;
                if(this.pageOffset < 0) {
                    this.pageOffset = 0;
                }
            }
            if(this.pageOffset + fitAmount < this.cache.size() && relX > guiWidth - nextSize && relX < guiWidth && relY > 0 && relY < nextSize) {
                this.pageOffset += fitAmount;
            }
        }
    }

    public void configurePageSelect(int guiWidth) {
        int nextSize = 20;
        int fitAmount = (guiWidth - nextSize - nextSize - this.tabPadding) / (this.tabWidth + this.tabPadding);
        this.pageOffset = this.selectedIndex / fitAmount * fitAmount;
    }

    public interface Tab {
        default boolean isDirty() {
            return false;
        }
        void onClicked();//Mouse X/Y ?
    }

}
