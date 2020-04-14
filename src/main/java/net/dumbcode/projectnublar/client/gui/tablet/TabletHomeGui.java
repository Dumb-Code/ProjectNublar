package net.dumbcode.projectnublar.client.gui.tablet;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C27InstallModule;
import net.dumbcode.projectnublar.server.network.C28ModuleClicked;
import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.dumbcode.projectnublar.server.tablet.backgrounds.TabletBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import javax.vecmath.Point2i;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class TabletHomeGui extends BaseBackgroundTabletScreen {

    private static final int GALLARY_ICON_SIZE = 16;

    private static final int ICON_ROWS = 2;
    private static final int ICON_COLUMNS = 4;

    private static final int ICONS_PER_PAGE = ICON_ROWS * ICON_COLUMNS;

    private static final int ICON_SIZE = 32; //Icons are square

    private ItemStack currentStack;

    private int page;

    private final List<Icon> icons = new ArrayList<>();

    private boolean openedInstallPopup;

    private final List<DropdownBoxEntry> entries = new ArrayList<>();
    private GuiDropdownBox<DropdownBoxEntry> dropdownBox;
    private GuiButton installButton;

    private int galleryIconTicks;
    private Point2i gallaryIcon;

    public TabletHomeGui(EnumHand hand) {
        super(hand);
        this.addIcons(Minecraft.getMinecraft().player.getHeldItem(hand));
        this.homeButton = false;
    }

    private void addIcons(ItemStack stack) {
        this.currentStack = stack;

        this.icons.clear();
        try(TabletItemStackHandler handler = new TabletItemStackHandler(stack)) {
            handler.getEntryList().stream().map(EntryIcon::new).forEach(this.icons::add);
            this.setBackground(handler.getBackground());
        }

        this.icons.add(new InstallIcon());
    }

    private void initIcons() {
        int widthSpace = this.tabletWidth / ICON_COLUMNS;
        int heightSpace = this.tabletHeight / ICON_ROWS;

        for (int i = 0; i < this.icons.size(); i++) {
            int xIndex = i % ICON_COLUMNS;
            int yIndex = (i / ICON_COLUMNS) % ICON_ROWS;

            Icon icon = this.icons.get(i);

            int middleX = (int) (widthSpace * (xIndex + 0.5F));
            icon.setLeft(this.leftStart + middleX - (ICON_SIZE) / 2);
            icon.setWidth(ICON_SIZE);

            int middleY = (int) (heightSpace * (yIndex + 0.5F));
            icon.setTop(this.topStart + middleY - (ICON_SIZE) / 2);
            icon.setHeight(ICON_SIZE);
        }
    }

    private void addAndInitIcons(ItemStack stack) {
        this.addIcons(stack);
        this.initIcons();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.dropdownBox = new GuiDropdownBox<>(this.width / 2 - 50, (int) (this.height * 0.25F - 10), 100, 20, 10, () -> this.entries);

        this.initIcons();

        this.gallaryIcon = new Point2i(this.leftStart + this.tabletWidth - GALLARY_ICON_SIZE - 3, this.topStart + 16);

        this.installButton = this.addButton(new GuiButton(509, this.width / 2 - 100, this.height - 40, "Add"));
        this.installButton.enabled = false;
        this.installButton.visible = false;

    }

    @Override
    public void updateScreen() {
        this.installButton.visible = this.openedInstallPopup;
        this.installButton.enabled = this.openedInstallPopup && this.dropdownBox.getActive() != null;

        if(this.mc.player.getHeldItem(this.hand) != this.currentStack) {
            this.addAndInitIcons(this.mc.player.getHeldItem(this.hand));
        }

        super.updateScreen();
    }

    @Override
    public void drawTabletScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawTabletScreen(mouseX, mouseY, partialTicks);
        this.forAllIcons(icon -> icon.render(icon.isMouseOver(mouseX, mouseY)));

        mc.renderEngine.bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/tablet_background_icon.png"));
        drawModalRectWithCustomSizedTexture(this.gallaryIcon.x, this.gallaryIcon.y, 0, 0, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE);
        if(mouseX > this.gallaryIcon.x && mouseX < this.gallaryIcon.x + GALLARY_ICON_SIZE && mouseY > this.gallaryIcon.y && mouseY < this.gallaryIcon.y + GALLARY_ICON_SIZE) {
            drawRect(this.gallaryIcon.x, this.gallaryIcon.y, this.gallaryIcon.x + GALLARY_ICON_SIZE, this.gallaryIcon.y + GALLARY_ICON_SIZE, 0xAA000000);
        }

        if(this.openedInstallPopup) {
            this.installButton.drawButton(this.mc, mouseX, mouseY, partialTicks);
            //Copied from #drawWorldBackground
            this.drawGradientRect(this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, -1072689136, -804253680);
            this.dropdownBox.render(mouseX, mouseY);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        if(this.openedInstallPopup) {
            this.dropdownBox.handleMouseInput();
        }
        super.handleMouseInput();
    }

    @Override
    public void handleKeyboardInput() throws IOException {
        if(this.openedInstallPopup) {
            this.dropdownBox.handleKeyboardInput();
        }
        super.handleKeyboardInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == 1) {
            if(this.openedInstallPopup) {
                this.openedInstallPopup = false;
            } else {
                this.mc.displayGuiScreen(null);

                if (this.mc.currentScreen == null) {
                    this.mc.setIngameFocus();
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            this.forAllIcons(icon -> {
                if(icon.isMouseOver(mouseX, mouseY)) {
                    icon.onClicked();
                }
            });
            if(mouseX > this.gallaryIcon.x && mouseX < this.gallaryIcon.x + GALLARY_ICON_SIZE && mouseY > this.gallaryIcon.y && mouseY < this.gallaryIcon.y + GALLARY_ICON_SIZE) {
                Minecraft.getMinecraft().displayGuiScreen(this.transferBackground(new BackgroundTabletScreen(this.hand)));
            }
        }
        if(this.openedInstallPopup) {
            this.dropdownBox.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if(this.openedInstallPopup && button.id == this.installButton.id && this.dropdownBox.getActive() != null) {
            this.openedInstallPopup = false;
            ProjectNublar.NETWORK.sendToServer(new C27InstallModule(this.dropdownBox.getActive().slot, this.hand));
        }
        super.actionPerformed(button);
    }

    private void forAllIcons(Consumer<Icon> consumer) {
        int iconStart = this.page * ICONS_PER_PAGE;
        for (int i = iconStart; i < iconStart + ICONS_PER_PAGE; i++) {
            if(i < this.icons.size()) {
                consumer.accept(this.icons.get(i));
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @RequiredArgsConstructor
    private class DropdownBoxEntry implements SelectListEntry {

        private final TabletModuleType<?> type;
        private final int slot;

        @Override
        public String getSearch() {
            return Objects.requireNonNull(this.type.getRegistryName()).toString();
        }

        @Override
        public void draw(int x, int y, int mouseX, int mouseY) {
            Minecraft.getMinecraft().fontRenderer.drawString(this.getSearch(), x, y, -1);
        }
    }

    @Setter
    @Accessors(chain = true)
    private abstract class Icon {
        protected int left;
        protected int top;
        protected int width;
        protected int height;

        abstract void render(boolean mouseOver);

        abstract void onClicked();

        private boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= this.left && mouseX - this.left <= this.width && mouseY >= this.top && mouseY - this.top <= this.height;
        }
    }

    @RequiredArgsConstructor
    private class EntryIcon extends Icon {

        private final TabletItemStackHandler.Entry entry;

        @Override
        public void render(boolean mouseOver) {
            ResourceLocation loc = Objects.requireNonNull(this.entry.getType().getRegistryName());
            if(mouseOver) {
                drawRect(this.left, this.top, this.left + this.height, this.top + this.height, 0x44000000);
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            mc.getRenderManager().renderEngine.bindTexture(new ResourceLocation(loc.getNamespace(), "textures/gui/module_icons/" + loc.getPath() + ".png"));
            drawModalRectWithCustomSizedTexture(this.left, this.top, 0, 0, this.width, this.height, this.width, this.height);
        }

        @Override
        public void onClicked() {
            System.out.println(entry.getType().getRegistryName() + ": clicked");
            mc.displayGuiScreen(new OpenedTabletScreen(hand));
            ProjectNublar.NETWORK.sendToServer(new C28ModuleClicked(this.entry.getType().getRegistryName(), hand));
        }
    }

    private class InstallIcon extends Icon {

        @Override
        public void render(boolean mouseOver) {
            drawRect(left, this.top, this.left + this.height, this.top + this.height, mouseOver ? 0xFF999999 : 0xFF222222);
        }

        @Override
        public void onClicked() {
            openedInstallPopup = true;
            scanInventory();
        }

        private void scanInventory() {
            //Hash map is used as a set to get unique elements.
            Map<TabletModuleType<?>, Integer> typeMaps = new HashMap<>();
            for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if(stack.getItem() instanceof ModuleItem) {
                    TabletModuleType<?> type = ((ModuleItem) stack.getItem()).getType();
                    if (icons.stream().noneMatch(icon -> icon instanceof EntryIcon && ((EntryIcon) icon).entry.getType() == type)) {
                        typeMaps.put(type, i);
                    }
                }
            }

            entries.clear();
            typeMaps.forEach((type, slot) -> entries.add(new DropdownBoxEntry(type, slot)));
        }
    }

}
