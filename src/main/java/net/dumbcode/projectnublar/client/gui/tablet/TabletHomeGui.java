package net.dumbcode.projectnublar.client.gui.tablet;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.projectnublar.mixin.ButtonAccessor;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.network.C2SInstallModule;
import net.dumbcode.projectnublar.server.network.C2STabletModuleClicked;
import net.dumbcode.projectnublar.server.tablet.ModuleItem;
import net.dumbcode.projectnublar.server.tablet.TabletItemStackHandler;
import net.dumbcode.projectnublar.server.tablet.TabletModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    private Button installButton;

    private int galleryIconTicks;
    private Vector3i galleryIcon;

    public TabletHomeGui(InteractionHand hand) {
        super(hand);
        this.addIcons(Minecraft.getInstance().player.getItemInHand(hand));
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
    public void init() {
        super.init();

        this.dropdownBox = this.addWidget(new GuiDropdownBox<>(this.width / 2 - 50, (int) (this.height * 0.25F - 10), 100, 20, 10, () -> this.entries));

        this.initIcons();

        this.galleryIcon = new Vector3i(this.leftStart + this.tabletWidth - GALLARY_ICON_SIZE - 3, this.topStart + 16, 0);

        this.installButton = this.addWidget(construct(this.width / 2 - 100, this.height - 40, 200, 20, ProjectNublar.translate("gui.tablethome.add"), p -> {
            if(this.openedInstallPopup && this.dropdownBox.getActive() != null) {
                this.openedInstallPopup = false;
                ProjectNublar.NETWORK.sendToServer(new C2SInstallModule(this.dropdownBox.getActive().slot, this.hand));
            }
        }));
        this.installButton.active = false;
        this.installButton.visible = false;

    }

    public static Button construct(int x, int y, int width, int height, Component text, Button.OnPress onPress) {
        return ButtonAccessor.construct(x, y, width, height, text, onPress, Supplier::get);
    }

    @Override
    public void tick() {
        this.installButton.visible = this.openedInstallPopup;
        this.installButton.active = this.openedInstallPopup && this.dropdownBox.getActive() != null;

        if(minecraft.player.getItemInHand(this.hand) != this.currentStack) {
            this.addAndInitIcons(minecraft.player.getItemInHand(this.hand));
        }

        super.tick();
    }

    @Override
    public void drawTabletScreen(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        super.drawTabletScreen(stack, mouseX, mouseY, partialTicks);
        this.forAllIcons(icon -> icon.render(stack, icon.isMouseOver(mouseX, mouseY)));

        stack.blit(new ResourceLocation(ProjectNublar.MODID, "textures/gui/tablet_background_icon.png"), this.galleryIcon.x, this.galleryIcon.y, 0, 0, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE, GALLARY_ICON_SIZE);
        if(mouseX > this.galleryIcon.x && mouseX < this.galleryIcon.x + GALLARY_ICON_SIZE && mouseY > this.galleryIcon.y && mouseY < this.galleryIcon.y + GALLARY_ICON_SIZE) {
            stack.fill(this.galleryIcon.x, this.galleryIcon.y, this.galleryIcon.x + GALLARY_ICON_SIZE, this.galleryIcon.y + GALLARY_ICON_SIZE, 0xAA000000);
        }

        if(this.openedInstallPopup) {
            this.installButton.render(stack, mouseX, mouseY, partialTicks);
            //Copied from #drawWorldBackground
            stack.fillGradient(this.leftStart, this.topStart, this.leftStart + this.tabletWidth, this.topStart + this.tabletHeight, -1072689136, -804253680);
            this.dropdownBox.render(stack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if(this.openedInstallPopup) {
                this.openedInstallPopup = false;
            } else {
                minecraft.setScreen(null);

//                if (minecraft.screen == null) {
//                    minecraft.setIngameFocus();
//                }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            this.forAllIcons(icon -> {
                if(icon.isMouseOver(mouseX, mouseY)) {
                    icon.onClicked();
                }
            });
            if(mouseX > this.galleryIcon.x && mouseX < this.galleryIcon.x + GALLARY_ICON_SIZE && mouseY > this.galleryIcon.y && mouseY < this.galleryIcon.y + GALLARY_ICON_SIZE) {
                Minecraft.getInstance().setScreen(this.transferBackground(new BackgroundTabletScreen(this.hand)));
                return true;
            }
        }
        if(this.openedInstallPopup) {
            if(this.dropdownBox.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
            if(this.installButton.mouseClicked(mouseX, mouseY, mouseButton)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
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
    public boolean isPauseScreen() {
        return false;
    }

    @RequiredArgsConstructor
    private static class DropdownBoxEntry implements SelectListEntry {

        private final TabletModuleType<?> type;
        private final int slot;

        @Override
        public String getSearch() {
            return Objects.requireNonNull(this.type.getRegistryName()).toString();
        }

        @Override
        public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            stack.drawString(Minecraft.getInstance().font, this.getSearch(), x, y, -1);
        }
    }

    @Setter
    @Accessors(chain = true)
    private static abstract class Icon {
        protected int left;
        protected int top;
        protected int width;
        protected int height;

        abstract void render(GuiGraphics stack, boolean mouseOver);

        abstract void onClicked();

        private boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= this.left && mouseX - this.left <= this.width && mouseY >= this.top && mouseY - this.top <= this.height;
        }
    }

    @RequiredArgsConstructor
    private class EntryIcon extends Icon {

        private final TabletItemStackHandler.Entry<?> entry;

        @Override
        public void render(GuiGraphics stack, boolean mouseOver) {
            ResourceLocation loc = Objects.requireNonNull(this.entry.getType().getRegistryName());
            if(mouseOver) {
                stack.fill(this.left, this.top, this.left + this.height, this.top + this.height, 0x44000000);
            }
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            stack.blit(new ResourceLocation(loc.getNamespace(), "textures/gui/module_icons/" + loc.getPath() + ".png"), this.left, this.top, 0, 0, this.width, this.height, this.width, this.height);
        }

        @Override
        public void onClicked() {
            System.out.println(entry.getType().getRegistryName() + ": clicked");
            minecraft.forceSetScreen(new OpenedTabletScreen(hand));
            ProjectNublar.NETWORK.sendToServer(new C2STabletModuleClicked(this.entry.getType().getRegistryName(), hand));
        }
    }

    private class InstallIcon extends Icon {

        @Override
        public void render(GuiGraphics stack, boolean mouseOver) {
            stack.fill(left, this.top, this.left + this.height, this.top + this.height, mouseOver ? 0xFF999999 : 0xFF222222);
        }

        @Override
        public void onClicked() {
            openedInstallPopup = true;
            scanInventory();
        }

        private void scanInventory() {
            //Hash map is used as a set to get unique elements.
            Map<TabletModuleType<?>, Integer> typeMaps = new HashMap<>();
            for (int i = 0; i < Objects.requireNonNull(Objects.requireNonNull(minecraft).player).getInventory().items.size(); i++) {
                assert minecraft.player != null;
                ItemStack stack = minecraft.player.getInventory().getItem(i);
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
