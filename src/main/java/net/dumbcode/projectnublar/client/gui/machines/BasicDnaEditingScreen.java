package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.SimpleSlider;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.server.dna.EntityGeneticRegistry;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C2SSequencingSynthesizerSelectChange;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BasicDnaEditingScreen extends DnaEditingScreen {

    private final List<DnaSelectModuleSlot> slotList = Lists.newArrayList();

    private final List<DriveEntry> dinosaurList = Lists.newArrayList();
    private final List<DriveEntry> entryList = Lists.newArrayList();
    private final List<DriveEntry> filteredListForSlot = Lists.newArrayList();


    private List<GuiScrollboxEntry> individualBreakdownList;

    private int slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
    private int selectedSlot = -1;


    private GuiScrollBox<DriveEntry> scrollBox;

    public BasicDnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(blockEntity, inventorySlotsIn, playerInventory, title, bar, "advanced", 3);
    }

    @Override
    public void init() {
        super.init();

        this.slotList.clear();
        for (int i = 0; i < SequencingSynthesizerBlockEntity.TOTAL_SLOTS; i++) {
            this.slotList.add(this.addButton(new DnaSelectModuleSlot(this.slotList.size(), this.leftPos + 10, 0)));
        }
        this.refreshHardDrive(blockEntity.getHandler().getStackInSlot(0));
        this.onSelectChange();

        this.updateAllSlots();

        this.scrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 232, this.topPos + 23, 109, 20, 5, () -> {
            if(this.selectedSlot == -1) {
                return Collections.emptyList();
            } else if(this.selectedSlot == 0) {
                return this.dinosaurList;
            } else {
                return this.filteredListForSlot;
            }
        }).addFromPrevious(this.scrollBox).setBorderColor(0xFF577694).setCellHighlightColor(0xCF193B59).setCellSelectedColor(0xFF06559C).setInsideColor(0xFF193B59).setEmptyColor(0xCF0F2234).setRenderFullSize(true));
    }

    @Override
    protected GuiScrollBox<GuiScrollboxEntry> createOverviewScrollBox() {
        return new GuiScrollBox<>(this.leftPos + 104, this.topPos + 134, 237, 14, 3, this::createOverviewScrollBoxList);
    }

    @Override
    protected List<GuiScrollboxEntry> createOverviewScrollBoxList() {
        if(this.individualBreakdownList != null) {
            return this.individualBreakdownList;
        }
        return super.createOverviewScrollBoxList();
    }

    @Override
    public void tick() {
        super.tick();

        int slots = this.slots;
        this.slots = this.blockEntity.getSlots();
        if(slots != this.slots) {
            this.updateAllSlots();
        }
    }

    @Override
    public void onSlotChanged(int slot, ItemStack stack) {
        super.onSlotChanged(slot, stack);
        if(slot == 0) {
            this.refreshHardDrive(stack);
        }
    }

    @Override
    public void onSelectChange() {
        super.onGeneticsChanged();
        this.refreshSlots();
    }

    private void refreshHardDrive(ItemStack stack) {
        List<DriveEntry> safeEntries = Lists.newArrayList();

        for (DriveUtils.DriveEntry entry : DriveUtils.getAll(stack)) {
            DriveEntry match = null;
            for (DriveEntry driveEntry : this.entryList) {
                if(driveEntry.combined.equals(SequencingSynthesizerBlockEntity.combine(entry.getKey(), entry.getVariant()))) {
                    match = driveEntry;
                    break;
                }
            }
            if (match == null) {
                DriveEntry e = new DriveEntry(entry);
                if (entry.getDriveType() == DriveUtils.DriveType.DINOSAUR) {
                    this.dinosaurList.add(e);
                }
                this.entryList.add(e);
                safeEntries.add(e);
            } else {
                safeEntries.add(match);
            }
        }
        this.updateDriveEntires(safeEntries);
        this.refreshFilteredList();
    }

    private void refreshSlots() {
        for (DnaSelectModuleSlot moduleSlot : this.slotList) {
            String key = this.blockEntity.getSelectKey(moduleSlot.id);
            double amount = this.blockEntity.getSelectAmount(moduleSlot.id);

            DriveEntry foundEntry = null;
            for (DriveEntry entry : this.entryList) {
                if(entry.combined.equals(key)) {
                    foundEntry = entry;
                    break;
                }
            }
            moduleSlot.setDrive(foundEntry);
            moduleSlot.slider.setValueDirectly(amount);
        }
    }

    @Override
    protected int getEntityLeft() {
        return 105;
    }

    @Override
    protected int getEntityRight() {
        return 223;
    }

    private void updateDriveEntires(List<DriveEntry> safeEntries) {
        for (DriveEntry e : this.entryList) {
            if(safeEntries.contains(e)) {
                continue;
            }

            for (DnaSelectModuleSlot module : this.slotList) {
                if(module.drive == e) {
                    module.setDrive(null);
                }
            }

            this.dinosaurList.remove(e);
        }
        this.entryList.removeIf(d -> !safeEntries.contains(d));
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderScreen(stack, mouseX, mouseY, partialTicks);

//        int xStart = this.leftPos + 102;
//        int yStart = this.topPos + 166;
//
//        int w = 128;
//        int yEnd = yStart + 10;
//
//        fill(stack, xStart-1, yStart-1, xStart+w+1, yEnd+1, -1);
//
//        int start = 0;
//        for (DnaSelectModuleSlot module : this.slotList) {
//            if(module.drive == null || module.slider.getSliderValue() == 0) {
//                continue;
//            }
//            int value = (int) Math.round((w+1) * MathHelper.clamp(module.slider.getSliderValue(), 0D, 1D));
//            fill(stack, xStart + start, yStart, xStart + start + value-1, yEnd, 0xFF000000|this.colors[module.id]);
//            if(module.drive != null && mouseX >= xStart + start && mouseX < xStart + start + value && mouseY >= yStart && mouseY < yEnd) {
//                hoveringText = module.drive.entry;
//            }
//            start += value;
//
//        }
//        if(start < w) {
//            fill(stack, xStart + start, yStart, xStart + w, yEnd, 0xFF000000);
//        }
        renderTooltip(stack, mouseX, mouseY);


    }


    private void setSelectedSlot(int selectedSlot) {
        if(this.selectedSlot == selectedSlot) {
            selectedSlot = -1;
            this.scrollBox.setSelectedElement(null);
        }
        this.selectedSlot = selectedSlot;
        this.refreshFilteredList();

        if(selectedSlot != -1) {
            DriveEntry drive = this.slotList.get(selectedSlot).getDrive();
            if(drive != null) {
                String key = drive.combined;
                for (DriveEntry entry : this.filteredListForSlot) {
                    if(entry.combined.equals(key)) {
                        scrollBox.setSelectedElement(entry);
                        break;
                    }
                }
            }

        }
    }

    private void refreshFilteredList() {
        if(this.selectedSlot != -1) {
            this.filteredListForSlot.clear();
            this.filteredListForSlot.addAll(this.entryList);
            for (int i = 0; i < this.selectedSlot; i++) {
                DriveEntry drive = this.slotList.get(i).getDrive();
                if(drive != null) {
                    String key = drive.combined;
                    this.filteredListForSlot.removeIf(p -> p.combined.equals(key));
                }
            }

            this.individualBreakdownList = new ArrayList<>();
            DnaSelectModuleSlot slot = this.slotList.get(this.selectedSlot);
            DriveEntry entry = slot.drive;
            if(entry != null && entry.driveEntry.getDriveType() == DriveUtils.DriveType.OTHER) {
                entry.driveEntry.getEntity().ifPresent(value -> {
                    for (EntityGeneticRegistry.Entry<?, ?> e : EntityGeneticRegistry.INSTANCE.gatherEntry(value, entry.driveEntry.getVariant())) {
                        this.individualBreakdownList.add(new GeneEditEntry<>(e.create((float) slot.slider.getSliderValue())));
                    }

                    SequencingSynthesizerBlockEntity.DnaColourStorage storage = this.blockEntity.getStorage(this.selectedSlot);
                    if(storage != null) {
                        List<Integer> tints = EntityGeneticRegistry.INSTANCE.gatherTints(value, entry.driveEntry.getVariant());
                        for (int i = 0; i < tints.size(); i++) {
                            this.individualBreakdownList.add(new GeneEditTintEntry(i, tints.get(i), entry, storage));
                        }
                    }
                });
            }

        } else {
            this.individualBreakdownList = null;
        }
    }

    private void updateAllSlots() {
        int center = this.topPos + this.imageHeight / 2 + 1;
        int totalHeight = 18*this.slots - 2; //4 is the padding on the bottom element we take away.
        int yStart = center - totalHeight / 2;
        for (DnaSelectModuleSlot slot : this.slotList) {
            slot.visible = slot.id < this.slots;
            if(slot.visible) {
                slot.y = yStart + slot.id*18;
                slot.slider.y = slot.y;
            }
        }
    }

    private void changeDnaSelection(int id, String key, double amount) {
        if (this.blockEntity.setAndValidateSelect(id, key, amount)) {
            ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                id, key, amount,
                this.blockEntity.getStorage(id)
            ));
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        for (DnaSelectModuleSlot module : this.slotList) {
            module.slider.mouseReleased(mouseX, mouseY, state);
        }
        return super.mouseReleased(mouseX, mouseY, state);
    }


    private class DnaSelectModuleSlot extends Widget implements INestedGuiEventHandler {

        @Nullable
        private IGuiEventListener focused;
        private boolean isDragging;

        private final int id;
        private final ClampedGuiSlider slider;
        @Getter
        private DriveEntry drive;

        private DnaSelectModuleSlot(int id, int x, int y) {
            super(x, y, 109, 16, new StringTextComponent(":)"));
            this.id = id;
            this.slider = new ClampedGuiSlider(id, x + 16, y, this);
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
            if(!this.visible) {
                return;
            }
            if(this.isMouseOver(mouseX, mouseY) && this.drive != null) {
                hoveringText = this.drive.entry;
            }
            this.slider.active = this.id < slots && this.drive != null;
            if(this.drive != null && this.id >= slots) {
                this.setDrive(null);
            }

            int color = 0xCF193B59;
            if(selectedSlot == this.id) {
                color = 0xFF06559C;
            }
            if(this.id >= slots) {
                color = 0xFF6A0606;
            }
            fill(stack, x, y, x+16, y+16, color);
            RenderUtils.renderBorder(stack, x, y, x+16, y+16, 1, 0xFF577694);

            if(mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                fill(stack, x, y, x+16, y+16, 0x2299bbff);
            }

            this.slider.render(stack, mouseX, mouseY, ticks);


            if(this.id >= slots && mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                hoveringText = ProjectNublar.translate("gui.machine.sequencer.locked_dna_slot", String.valueOf(SequencingSynthesizerBlockEntity.getPercentageForSlot(this.id+1)), String.valueOf(blockEntity.getDinosaurAmount()));
            }

        }

        public void setDrive(DriveEntry drive) {
            if(this.drive == drive) {
                return;
            }
            this.drive = drive;
            if(this.drive == null) {
                changeDnaSelection(this.id, "", 0);
            }
            if(this.id == 0) {
                if(drive == null) {
                    slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
                } else {
                    slots = SequencingSynthesizerBlockEntity.getSlots(DriveUtils.getAmount(blockEntity.getHandler().getStackInSlot(0), drive.driveEntry.getKey(), drive.driveEntry.getVariant()) / 100F);
                }
                updateAllSlots();
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(this.active && this.visible && mouseX >= this.x && mouseX < this.x+16 && mouseY >= this.y && mouseY < this.y+16) {
                setSelectedSlot(this.id);
                return true;
            }
            return INestedGuiEventHandler.super.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public final boolean isDragging() {
            return this.isDragging;
        }

        @Override
        public final void setDragging(boolean p_231037_1_) {
            this.isDragging = p_231037_1_;
        }

        @Nullable
        @Override
        public IGuiEventListener getFocused() {
            return this.focused;
        }

        @Override
        public void setFocused(@Nullable IGuiEventListener p_231035_1_) {
            this.focused = p_231035_1_;
        }

        @Override
        public List<? extends IGuiEventListener> children() {
            return Collections.singletonList(this.slider);
        }

    }

    @RequiredArgsConstructor
    private class GeneEditTintEntry implements GuiScrollboxEntry {
        private final int tintIndex;
        private final int tint;
        private final DriveEntry entry;
        private final SequencingSynthesizerBlockEntity.DnaColourStorage storage;

        private int width;
        private int height;

        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            fill(stack, x, y, x+width, y+height, 0xFF000000 | this.tint);

            this.width = width;
            this.height = height;

            int squareSize = height - 2;
            int center = x + width/2;

            int overIndex = this.mouseOverSquareIndex(mouseX - x, mouseY - y);

            int primary = this.storage.getPrimary().contains(this.tintIndex) ? 0xBB518851 : 0xBB885151;
            int secondary = this.storage.getSecondary().contains(this.tintIndex) ? 0xBB518851 : 0xBB885151;

            fill(stack, center-squareSize-2, y+1, center-1, y+1+squareSize, overIndex == 0 ? 0xBB515188 : primary);
            fill(stack, center+squareSize+2, y+1, center+1, y+1+squareSize, overIndex == 1 ? 0xBB515188 : secondary);
        }

        private int mouseOverSquareIndex(double mx, double my) {
            double squareSize = this.height - 2;
            double center = this.width/2D;
            if(mx >= center-squareSize-2 && my >= 1 && mx < center-1 && my < 1+squareSize) {
                return 0;
            } else if(mx >= center+1 && my >= 1 && mx < center+squareSize+2 && my < 1+squareSize) {
                return 1;
            }
            return -1;
        }

        private void toggle(Set<Integer> set) {
            if(set.contains(this.tintIndex)) {
                set.remove(this.tintIndex);
            } else {
                set.add(this.tintIndex);
            }

            ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                selectedSlot, entry.driveEntry.getKey(), entry.driveEntry.getAmount(),
                this.storage
            ));
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            int overIndex = this.mouseOverSquareIndex(relMouseX, relMouseY);
            if(overIndex == 0) {
                this.toggle(this.storage.getPrimary());
                return true;
            } else if(overIndex == 1) {
                this.toggle(this.storage.getSecondary());
                return true;
            }
            return false;
        }
    }

    private class DriveEntry implements GuiScrollboxEntry {

        private final DriveUtils.DriveEntry driveEntry;
        private final TranslationTextComponent entry;
        private final String combined;

        private DriveEntry(DriveUtils.DriveEntry driveEntry) {
            this.driveEntry = driveEntry;
            this.entry = driveEntry.getTranslation();
            this.combined = SequencingSynthesizerBlockEntity.combine(driveEntry.getKey(), driveEntry.getVariant());
        }

        @Override
        public void draw(MatrixStack stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            drawString(stack, minecraft.font, this.entry, x + 2, y + 5, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            int id = selectedSlot;
            boolean ret = GuiScrollboxEntry.super.onClicked(relMouseX, relMouseY, mouseX, mouseY);
            if(id != -1) {
                String key = this.combined;
                if(scrollBox.getSelectedElement() == this) {
                    slotList.get(id).setDrive(null);
                    key = "";
                    scrollBox.setSelectedElement(null);
                    ret = false;
                }
                changeDnaSelection(id, key, 0);
                //We might only need to do `slotList[id].slider.setValueDirectly(0);`
                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
            }
            return ret;
        }
    }

    private class ClampedGuiSlider extends SimpleSlider {

        private final int id;
        private final DnaSelectModuleSlot module;

        ClampedGuiSlider(int id, int x, int y, DnaSelectModuleSlot module) {
            super(x, y, 70, 16, new StringTextComponent(""), new StringTextComponent("%"), 0D, id == 0 ? 100D : 50D, BasicDnaEditingScreen.this.blockEntity.getSelectAmount(id), false, true, p_onPress_1_ -> {});
            this.id = id;
            this.module = module;
        }

        @Override
        public void updateSlider() {
            if(module.drive != null) {
                changeDnaSelection(this.id, module.drive.combined, this.getSliderValue());

                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
                //Don't need to call super as it's done in `setValueDirectly`
            }
        }

        public double getSliderValue() {
            return this.sliderValue * (this.id == 0 ? 1 : 0.5D);
        }

        public void setValueDirectly(double d) {
            this.sliderValue = d / (this.id == 0 ? 1 : 0.5D);
            super.updateSlider(); //Must be super to prevent loops
        }
    }
}
