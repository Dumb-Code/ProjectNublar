package net.dumbcode.projectnublar.client.gui.tab;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class TabListInformation {

    private static final int TAB_WIDTH = 30;
    private static final int TAB_PADDING = 5;
    private static final int TAB_HEIGHT = 20;

    private final Supplier<List<TabbedGui.Tab>> list;

    private List<TabbedGui.Tab> CACHE = Lists.newArrayList();

    private int pageOffset;

    public TabListInformation(Supplier<List<TabbedGui.Tab>> list) {
        this.list = list;
    }

    public void update() {
        if(CACHE.isEmpty()) {
            CACHE = list.get();
        } else {
            for (TabbedGui.Tab tab : CACHE) {
                if(tab.isDirty()) {
                    CACHE = this.list.get();
                    break;
                }
            }
        }
    }

    public void render(int mouseX, int mouseY) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if(gui instanceof GuiContainer) {
            GuiContainer cont = (GuiContainer) gui;
            List<TabbedGui.Tab> tabs = CACHE;

            int fitAmount = (cont.width - TAB_WIDTH) / (TAB_WIDTH + TAB_PADDING) + 1;
            int offset = cont.getGuiLeft() + (cont.width - TAB_WIDTH - fitAmount * (TAB_WIDTH + TAB_PADDING)) / 2;


            for (int i = 0; i < fitAmount && i + this.pageOffset < tabs.size(); i++) {
                int xStart = offset + (TAB_WIDTH + TAB_PADDING) * i;
                Gui.drawRect(xStart, cont.getGuiTop() - TAB_HEIGHT, xStart + TAB_WIDTH, cont.getGuiTop(), 0xFF000000 | new Random(i << 21).nextInt());
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton == 0) {
            TabbedGui.Tab tab = this.getMouseOver(CACHE, mouseX, mouseY);
            if(tab != null) {
                tab.onClicked();
            }
        }
    }

    @Nullable
    private TabbedGui.Tab getMouseOver(List<TabbedGui.Tab> list, int mouseX, int mouseY) {
        GuiScreen gui = Minecraft.getMinecraft().currentScreen;
        if(gui instanceof GuiContainer) {
            GuiContainer cont = (GuiContainer) gui;

            int fitAmount = (cont.width - TAB_WIDTH) / (TAB_WIDTH + TAB_PADDING) + 1;
            int offset = cont.getGuiLeft() + (cont.width - TAB_WIDTH - fitAmount * (TAB_WIDTH + TAB_PADDING)) / 2;


            for (int i = 0; i < fitAmount && i + this.pageOffset < list.size(); i++) {
                int xStart = offset + (TAB_WIDTH + TAB_PADDING) * i;
                if(mouseY > cont.getGuiTop() - TAB_HEIGHT && mouseY < cont.getGuiTop() && mouseX > xStart && mouseX < xStart + TAB_WIDTH) {
                    return list.get(i + this.pageOffset);
                }
            }
        }
        return null;
    }


}
