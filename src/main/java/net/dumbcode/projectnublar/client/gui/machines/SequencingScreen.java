package net.dumbcode.projectnublar.client.gui.machines;

import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.projectnublar.client.gui.tab.MachineContainerScreen;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.MachineModuleBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SequencingScreen extends SequencerSynthesizerBaseScreen {

    private static final ResourceLocation BASE_LOCATION = get("sequencer_page");

    private static final int OVERLAY_WIDTH = 472;
    private static final int OVERLAY_HEIGHT = 199;

    private static final float FIRST_SECTION_SIZE = 119F / 239F;

    private final Supplier<List<DriveUtils.DriveEntry>> driveEntriesGetter;
    private final List<DriveDisplayEntry> cachedEntries = new ArrayList<>();

    private final MachineModuleBlockEntity.MachineProcess<SequencingSynthesizerBlockEntity> process;

    private GuiScrollBox<DriveDisplayEntry> displayEntryScrollBox;

    public SequencingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.process = blockEntity.getProcess(0);
        this.driveEntriesGetter = () -> DriveUtils.getAll(blockEntity.getHandler().getStackInSlot(0));
    }

    @Override
    public void init() {
        super.init();
        this.cacheEntries();

        this.displayEntryScrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 66, this.topPos + 29, 150, 13, 9, () -> this.cachedEntries));

        this.displayEntryScrollBox.setBorderColor(0xFF577694);
        this.displayEntryScrollBox.setInsideColor(0x9F193B59);
    }

    @Override
    public void tick() {
        this.cacheEntries();
        super.tick();
    }

    private void cacheEntries() {
        DriveDisplayEntry selectedElement = this.displayEntryScrollBox != null ? this.displayEntryScrollBox.getSelectedElement() : null;
        this.cachedEntries.clear();
        this.cachedEntries.addAll(this.driveEntriesGetter.get().stream()
                .map(DriveDisplayEntry::new)
                .sorted(Comparator.comparing(DriveDisplayEntry::getSearch))
                .collect(Collectors.toList())
        );
        if(selectedElement != null) {
            for (DriveDisplayEntry entry : this.cachedEntries) {
                if(entry.driveEntry.getKey().equals(selectedElement.driveEntry.getKey())) {
                    this.displayEntryScrollBox.setSelectedElement(entry);
                    break;
                }
            }
        }
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        super.renderScreen(stack, mouseX, mouseY, ticks);
        drawProcessIcon(this.process, stack, 167.5F, 153);
        drawProcessTooltip(this.process, stack, 56, 151, 239, 19, mouseX, mouseY);
    }

    @Override
    protected void renderBg(MatrixStack stack, float ticks, int mouseX, int mouseY) {
        super.renderBg(stack, ticks, mouseX, mouseY);

        minecraft.textureManager.bind(BASE_LOCATION);
        blit(stack, this.leftPos, this.topPos, this.getBlitOffset(), 0, 0, this.imageWidth, this.imageHeight, OVERLAY_HEIGHT, OVERLAY_WIDTH); //There is a vanilla bug that mixes up width and height

        float timeDone = this.process.getTimeDone();
        float leftDone = MathHelper.clamp(timeDone / FIRST_SECTION_SIZE, 0F, 1F);
        float rightDone = MathHelper.clamp((timeDone - FIRST_SECTION_SIZE) / (1 - FIRST_SECTION_SIZE), 0F, 1F);

        if(leftDone != 0) {
            subPixelBlit(stack, this.leftPos + 56, this.topPos + 151, 351, 0, leftDone * 119F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
        if(rightDone != 0) {
            subPixelBlit(stack, this.leftPos + 175, this.topPos + 151, 351, 19, rightDone * 120F, 19, OVERLAY_WIDTH, OVERLAY_HEIGHT);
        }
//        blit(stack, this.leftPos, this.topPos);
    }

    @RequiredArgsConstructor
    private class DriveDisplayEntry implements SelectListEntry {

        private final DriveUtils.DriveEntry driveEntry;

        @Override
        public void draw(MatrixStack stack, int x, int y, int mouseX, int mouseY, boolean mouseOver) {
            String translated = I18n.get(this.driveEntry.getName()) + ": " + this.driveEntry.getAmount() + "%";
            FontRenderer font = Minecraft.getInstance().font;
            font.draw(stack, translated, x + (150 - font.width(translated)) / 2F, y + 3, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            if(displayEntryScrollBox.getSelectedElement() == this) {
                displayEntryScrollBox.setSelectedElement(null);
                return false;
            }
            //On click, do stuff
            return true;
        }

        @Override
        public String getSearch() {
            return I18n.get(this.driveEntry.getName());
        }
    }
}
