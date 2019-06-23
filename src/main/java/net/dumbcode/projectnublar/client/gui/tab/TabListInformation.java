package net.dumbcode.projectnublar.client.gui.tab;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.function.Supplier;

public class TabListInformation {

    private static final int TAB_WIDTH = 28;
    private static final int TAB_PADDING = 5;
    private static final int TAB_HEIGHT = 27;

    private final Supplier<List<TabbedGui.Tab>> list;

    private List<TabbedGui.Tab> CACHE;

    private int selectedIndex;
    private int pageOffset;

    public TabListInformation(Supplier<List<TabbedGui.Tab>> list) {
        this.list = list;
        CACHE = this.list.get();
    }

    public void update() {
        for (TabbedGui.Tab tab : CACHE) {
            if (tab.isDirty()) {
                CACHE = this.list.get();
                break;
            }
        }
    }

    public void render(int mouseX, int mouseY) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if(gui instanceof GuiContainer) {

            int nextSize = 20;

            GuiContainer cont = (GuiContainer) gui;

            int fitAmount = (cont.getXSize() - nextSize - nextSize - TAB_PADDING) / (TAB_WIDTH + TAB_PADDING);
            int offset = cont.getGuiLeft() + (cont.getXSize() + TAB_PADDING - fitAmount * (TAB_WIDTH + TAB_PADDING)) / 2;

            for (int i = 0; i < fitAmount && i + this.pageOffset < CACHE.size(); i++) {
                int xStart = offset + (TAB_WIDTH + TAB_PADDING) * i;
                boolean selected = this.selectedIndex == i + this.pageOffset;
                int tabExtension = selected ? 5 : 1;
                GlStateManager.color(1f, 1f, 1f, 1f);
                GlStateManager.disableLighting();
                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/gui/container/creative_inventory/tabs.png"));
                Gui.drawModalRectWithCustomSizedTexture(xStart, cont.getGuiTop() - TAB_HEIGHT - 1, 28, selected ? 32 : 0, TAB_WIDTH, TAB_HEIGHT + tabExtension, 256, 256);
//                Gui.drawRect(xStart, cont.getGuiTop() - TAB_HEIGHT, xStart + TAB_WIDTH, cont.getGuiTop() + 5, 0xFF000000 | new Random((i + this.pageOffset) << 21).nextInt());
//                if(mouseY > cont.getGuiTop() - TAB_HEIGHT && mouseY < cont.getGuiTop() + tabExtension && mouseX > xStart && mouseX < xStart + TAB_WIDTH) {
//                    Gui.drawRect(xStart, cont.getGuiTop() - TAB_HEIGHT, xStart + TAB_WIDTH, cont.getGuiTop() + tabExtension, 0x6A0063FF); TODO: Remove this?
//                }
            }
            FontRenderer font = Minecraft.getMinecraft().fontRenderer;
            String text = String.format("Page %s/%s", this.pageOffset / fitAmount + 1, Math.round(CACHE.size() / (float)fitAmount)); // TODO: 11/11/2018 localize
            font.drawString(text, cont.getGuiLeft() + (cont.getXSize() - font.getStringWidth(text)) / 2, cont.getGuiTop() - TAB_HEIGHT - font.FONT_HEIGHT - 2, -1);

            int top = cont.getGuiTop() - TAB_HEIGHT + (TAB_HEIGHT - nextSize) / 2;
            if(this.pageOffset != 0) {
                Gui.drawRect(cont.getGuiLeft(), top, cont.getGuiLeft() + nextSize, top + nextSize, -1);
            }
            if(this.pageOffset + fitAmount < CACHE.size()) {
                Gui.drawRect(cont.getGuiLeft() + cont.getXSize() - nextSize, top, cont.getGuiLeft() + cont.getXSize(), top + nextSize, -1);
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {

            GuiScreen gui = Minecraft.getMinecraft().currentScreen;
            if(gui instanceof GuiContainer) {
                GuiContainer cont = (GuiContainer) gui;
                int nextSize = 20;

                int fitAmount = (cont.getXSize() - nextSize - nextSize - TAB_PADDING) / (TAB_WIDTH + TAB_PADDING);
                int offset = cont.getGuiLeft() + (cont.getXSize() + TAB_PADDING - fitAmount * (TAB_WIDTH + TAB_PADDING)) / 2;

                for (int i = 0; i < fitAmount && i + this.pageOffset < CACHE.size(); i++) {
                    int xStart = offset + (TAB_WIDTH + TAB_PADDING) * i;
                    int tabExtension = this.selectedIndex == i + this.pageOffset ? 5 : 1;
                    if(mouseY > cont.getGuiTop() - TAB_HEIGHT && mouseY < cont.getGuiTop() + tabExtension && mouseX > xStart && mouseX < xStart + TAB_WIDTH) {
                        this.selectedIndex = this.pageOffset + i;
                        CACHE.get(i + this.pageOffset).onClicked();
                        break;
                    }
                }

                int top = cont.getGuiTop() - TAB_HEIGHT + (TAB_HEIGHT - nextSize) / 2;

                int relX = mouseX - cont.getGuiLeft();
                int relY = mouseY - top;

                if(this.pageOffset != 0 && relX > 0 && relX < nextSize && relY > 0 && relY < nextSize) {
                    this.pageOffset -= fitAmount;
                    if(this.pageOffset < 0) {
                        this.pageOffset = 0;
                    }
                }
                if(this.pageOffset + fitAmount < CACHE.size() && relX > cont.getXSize() - nextSize && relX < cont.getXSize() && relY > 0 && relY < nextSize) {
                    this.pageOffset += fitAmount;
                }
            }
        }
    }

    public void configurePageSelect() {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if(gui instanceof GuiContainer) {
            GuiContainer cont = (GuiContainer) gui;
            int nextSize = 20;
            int fitAmount = (cont.getXSize() - nextSize - nextSize - TAB_PADDING) / (TAB_WIDTH + TAB_PADDING);
            this.pageOffset = this.selectedIndex / fitAmount * fitAmount;
        }
    }


}
