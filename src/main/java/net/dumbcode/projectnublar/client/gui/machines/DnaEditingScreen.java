package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.containers.machines.MachineModuleContainer;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C2SSequencingSynthesizerSelectChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class DnaEditingScreen extends SequencerSynthesizerBaseScreen {

    private final SequencingSynthesizerBlockEntity blockEntity;

    //TODO: make colors entity related.
    private final int[] colors ={ 0x2acc9b, 0xa0dd66, 0x23e0e3, 0xe30394, 0x1d52f5, 0x484179, 0x4f985a, 0x680fb7 };

    private final List<DnaSelectModuleSlot> slotList = Lists.newArrayList();

    private final List<DriveEntry> dinosaurList = Lists.newArrayList();
    private final List<DriveEntry> entryList = Lists.newArrayList();
    private final List<DriveEntry> filteredListForSlot = Lists.newArrayList();

    private int slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
    private int selectedSlot = -1;

    private GuiScrollBox<DriveEntry> scrollBox;
    private TranslationTextComponent hoveringText;

    public DnaEditingScreen(SequencingSynthesizerBlockEntity blockEntity, MachineModuleContainer inventorySlotsIn, PlayerInventory playerInventory, ITextComponent title, TabInformationBar bar) {
        super(inventorySlotsIn, playerInventory, title, bar);
        this.blockEntity = blockEntity;
    }


    @Override
    public void init() {
        super.init();

        this.updateLists();

        this.scrollBox = this.addButton(new GuiScrollBox<>(this.leftPos + 232, this.topPos + 24, 100, 20, 5, () -> {
            if(this.selectedSlot == -1) {
                return Collections.emptyList();
            } else if(this.selectedSlot == 0) {
                return this.dinosaurList;
            } else {
                return this.filteredListForSlot;
            }
        }).addFromPrevious(this.scrollBox).setBorderColor(0xFF577694).setCellHighlightColor(0xCF193B59).setCellSelectedColor(0xFF06559C).setInsideColor(0xFF193B59).setEmptyColor(0xCF0F2234).setRenderFullSize(true));

        this.slotList.clear();
        for (int i = 0; i < SequencingSynthesizerBlockEntity.TOTAL_SLOTS; i++) {
            this.slotList.add(this.addButton(new DnaSelectModuleSlot(this.slotList.size(), this.leftPos + 10, 0)));
        }
        this.updateAllSlots();
    }

    @Override
    public void tick() {
        super.tick();
        this.updateLists();

        int slots = this.slots;
        this.slots = this.blockEntity.getSlots();
        if(slots != this.slots) {
            this.updateAllSlots();
        }
    }

    private void updateLists() {
        List<DriveEntry> safeEntries = Lists.newArrayList();

        for (DriveUtils.DriveEntry entry : DriveUtils.getAll(this.blockEntity.getHandler().getStackInSlot(0))) {
            DriveEntry match = null;
            for (DriveEntry driveEntry : this.entryList) {
                if(driveEntry.key.equals(entry.getKey())) {
                    match = driveEntry;
                    break;
                }
            }
            if (match == null) {
                DriveEntry e = new DriveEntry(entry.getKey(), entry.getName());
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

        for (DnaSelectModuleSlot moduleSlot : this.slotList) {
            String key = this.blockEntity.getSelectKey(moduleSlot.id);
            double amount = this.blockEntity.getSelectAmount(moduleSlot.id);

            DriveEntry foundEntry = null;
            for (DriveEntry entry : this.entryList) {
                if(entry.key.equals(key)) {
                    foundEntry = entry;
                    break;
                }
            }
            moduleSlot.setDrive(foundEntry);
            moduleSlot.slider.setValueDirectly(amount);
        }
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
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        hoveringText = null;
        super.render(stack, mouseX, mouseY, ticks);
    }

    @Override
    public void renderScreen(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderScreen(stack, mouseX, mouseY, partialTicks);

        int xStart = this.leftPos + 102;
        int yStart = this.topPos + 166;

        int w = 128;
        int yEnd = yStart + 10;

        fill(stack, xStart-1, yStart-1, xStart+w+1, yEnd+1, -1);

        int start = 0;
        for (DnaSelectModuleSlot module : this.slotList) {
            if(module.drive == null || module.slider.sliderValue == 0) {
                continue;
            }
            int value = (int) Math.round(w * Math.round(module.slider.sliderValue * 100D) / 100D);
            fill(stack, xStart + start, yStart, xStart + start + value-1, yEnd, 0xFF000000|this.colors[module.id]);
            if(module.drive != null && mouseX >= xStart + start && mouseX < xStart + start + value && mouseY >= yStart && mouseY < yEnd) {
                hoveringText = module.drive.entry;
            }
            start += value;

        }
        fill(stack, xStart + start, yStart, xStart + w, yEnd, 0xFF000000);


        if(hoveringText != null) {
            int width = minecraft.font.width(hoveringText);
            fill(stack, mouseX+5, mouseY-1, mouseX + width + 6, mouseY + minecraft.font.lineHeight+1, 0xFF23374A);
            RenderUtils.renderBorderExclusive(stack, mouseX+5, mouseY-1, mouseX + width +6, mouseY + minecraft.font.lineHeight+1, 1, 0xFF577694);
            drawString(stack, minecraft.font, hoveringText, mouseX + 6, mouseY, -1);
        }
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
                String key = drive.getKey();
                for (DriveEntry entry : this.filteredListForSlot) {
                    if(entry.key.equals(key)) {
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
                    String key = drive.getKey();
                    this.filteredListForSlot.removeIf(p -> p.key.equals(key));
                }
            }
        }
    }

    private void updateAllSlots() {
        int center = this.topPos + this.imageHeight / 2;
        int totalWidth = 20*this.slots - 4; //4 is the padding on the bottom element we take away.
        int yStart = center - totalWidth / 2;
        for (DnaSelectModuleSlot slot : this.slotList) {
            slot.visible = slot.id < this.slots;
            if(slot.visible) {
                slot.y = yStart + slot.id*20;
                slot.slider.y = slot.y;
            }
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
            super(x, y, 100, 16, new StringTextComponent(":)"));
            this.id = id;
            this.slider = new ClampedGuiSlider(id, x + 16, y, this);
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
            if(!this.visible) {
                return;
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

//        public void draw(MatrixStack stack) {
//            StencilStack.pushSquareStencil(stack, x, y + 5, x+75, y+20);
//            fill(stack, x, y, x+75, y+25, enabled ? 0xFF000000 : 0xFF4A0000);
//            if(this.drive != null) {
//                this.drive.draw(stack, x, y+5, mouseX, mouseY, mouseOver);
//            }
//            StencilStack.popStencil();
//            this.slider.render(stack, mouseX, mouseY, 1F);
//
//            if(!enabled && mouseOver) {
//                hoveringText = "Need " + SequencingSynthesizerBlockEntity.getPercentageForSlot(this.id+1) + " % of dino to use";//todo: localize
//            }
//        }

        public void setDrive(DriveEntry drive) {
            this.drive = drive;
            if(this.drive == null) {
                if (blockEntity.setAndValidateSelect(id, "", 0)) {
                    ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                        id, "", 0)
                    );
                }
            }
            if(this.id == 0) {
                if(drive == null) {
                    slots = SequencingSynthesizerBlockEntity.MINIMUM_SLOTS;
                } else {
                    slots = SequencingSynthesizerBlockEntity.getSlots(DriveUtils.getAmount(blockEntity.getHandler().getStackInSlot(0), drive.getKey()) / 100F);
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

//        @Override
//        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
//            if(relMouseX > 0 && relMouseX < 75 && relMouseY > 0 && relMouseY < 20) {
//                dropdownSelectionId = this.id;
//                popupSelection.setScroll(0);
//            }
//            return false;
//        }
//
//        @Override
//        public boolean globalClicked(double mouseX, double mouseY, int mouseButton) {
//            this.slider.mouseClicked(mouseX, mouseY, mouseButton);
//            return false;
//        }
    }

    private class DriveEntry implements GuiScrollboxEntry {

        private final String key;
        private final TranslationTextComponent entry;

        private DriveEntry(String key, String entry) {
            this.key = key;
            this.entry = new TranslationTextComponent(entry);
        }

        @Override
        public void draw(MatrixStack stack, int x, int y, int mouseX, int mouseY, boolean mouseOver) {
            drawString(stack, minecraft.font, this.entry, x + 2, y + 5, -1);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            int id = selectedSlot;
            boolean ret = GuiScrollboxEntry.super.onClicked(relMouseX, relMouseY, mouseX, mouseY);
            if(id != -1) {
                String key = this.key;
                if(scrollBox.getSelectedElement() == this) {
                    slotList.get(id).setDrive(null);
                    key = "";
                    scrollBox.setSelectedElement(null);
                    ret = false;
                }
                if (blockEntity.setAndValidateSelect(id, key, 0)) {
                    ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                        id, key, 0)
                    );
                }
                //We might only need to do `slotList[id].slider.setValueDirectly(0);`
                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
            }
            return ret;
        }

        public String getKey() {
            return this.key;
        }


    }

    private class ClampedGuiSlider extends Slider {

        private final int id;
        private final DnaSelectModuleSlot module;

        ClampedGuiSlider(int id, int x, int y, DnaSelectModuleSlot module) {
            super(x, y, 70, 16, new StringTextComponent(""), new StringTextComponent("%"), 0D, 100D, DnaEditingScreen.this.blockEntity.getSelectAmount(id), false, true, p_onPress_1_ -> {});
            this.id = id;
            this.module = module;
        }

        @Override
        public void updateSlider() {
            if(module.drive != null) {
                blockEntity.setAndValidateSelect(this.id, module.drive.getKey(), this.sliderValue);
                ProjectNublar.NETWORK.sendToServer(new C2SSequencingSynthesizerSelectChange(
                    this.id, module.drive.getKey(), this.sliderValue)
                );

                for (DnaSelectModuleSlot slot : slotList) {
                    slot.slider.setValueDirectly(blockEntity.getSelectAmount(slot.id));
                }
                //Don't need to call super as it's done in `setValueDirectly`
            }
        }

        public void setValueDirectly(double d) {
            this.sliderValue = d;
            super.updateSlider(); //Must be super to prevent loops
        }

        @Override
        public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partial) {
            if(!this.visible) {
                return;
            }
            if (this.dragging) {
                this.sliderValue = (mouseX - (this.x + 4)) / (float)(this.width - 8);
                updateSlider();
            }
            Minecraft mc = Minecraft.getInstance();
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            int thumbLeft = this.x + (int)(this.sliderValue * (float)(this.width - 8));
            int thumbTop = this.y + 1;
            int thumbRight = thumbLeft + 8;
            int thumbBottom = thumbTop + this.height - 2;

            StencilStack.pushSquareStencil(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, StencilStack.Type.NOT);
            fill(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 0xCF193B59);
            if(this.isHovered && this.active) {
                fill(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 0x2299bbff);
            }
            RenderUtils.renderBorderExclusive(stack, this.x, this.y + 4, this.x + this.width, this.y + 12, 1, 0xFF577694);
            StencilStack.popStencil();

            fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0xCF193B59);
            if(this.isHovered && this.active) {
                fill(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 0x2299bbff);
            }
            RenderUtils.renderBorderExclusive(stack, thumbLeft, thumbTop, thumbRight, thumbBottom, 1, 0xFF577694);

            ITextComponent buttonText = this.getMessage();
            int strWidth = mc.font.width(buttonText);
            int ellipsisWidth = mc.font.width("...");

            if (strWidth > width - 6 && strWidth > ellipsisWidth)
                //TODO, srg names make it hard to figure out how to append to an ITextProperties from this trim operation, wraping this in StringTextComponent is kinda dirty.
                buttonText = new StringTextComponent(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");

            drawCenteredString(stack, mc.font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
        }
    }
}
