package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.StencilStack;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.dumblibrary.client.gui.SelectListEntry;
import net.dumbcode.dumblibrary.server.utils.MathUtils;
import net.dumbcode.projectnublar.client.gui.tab.TabInformationBar;
import net.dumbcode.projectnublar.client.gui.tab.TabbedGuiContainer;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.dumbcode.projectnublar.server.network.C14SequencingSynthesizerSelectChange;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleFunction;

public class DnaEditingGui extends TabbedGuiContainer {

    private final SequencingSynthesizerBlockEntity blockEntity;

    private final int[] colors ={ 0x2acc9b, 0xa0dd66, 0x23e0e3, 0xe30394, 0x1d52f5, 0x484179, 0x4f985a, 0x680fb7 };

    private GuiScrollBox<DnaSelectModule> scrollBox;

    private final List<DnaSelectModule> slotList = Lists.newArrayList();

    private final List<DriveEntry> dinosaurList = Lists.newArrayList();
    private final List<DriveEntry> entryList = Lists.newArrayList();
    private final List<DriveEntry> filteredEntryList = Lists.newArrayList();

    private int slots = SequencingSynthesizerBlockEntity.SLOTS_AT_50;

    private GuiScrollBox<DriveEntry> popupSelection;
    private int dropdownSelectionId = -1;

    private boolean dirty;

    private String hoveringText;

    public DnaEditingGui(EntityPlayer player, SequencingSynthesizerBlockEntity blockEntity, TabInformationBar info, int tab) {
        super(blockEntity.createContainer(player, tab), info);
        this.blockEntity = blockEntity;
        this.xSize = 208;
        this.ySize =  192;
        this.updateList();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.scrollBox = new GuiScrollBox<>(
            this.guiLeft + 23, this.guiTop + 10, 162, 30, 5, () -> this.slotList
        );
        this.scrollBox.setHighlightColor(0);

        this.slotList.clear();
        for (int i = 0; i < SequencingSynthesizerBlockEntity.TOTAL_SLOTS; i++) {
            this.slotList.add(new DnaSelectModule(this.slotList.size()));
        }

        this.popupSelection = new GuiScrollBox<>(this.guiLeft + 54, this.guiTop + 30, 100, 20, 5, () -> {
            if(this.dropdownSelectionId == -1) {
                return Collections.emptyList();
            } else if(this.dropdownSelectionId == 0) {
                return this.dinosaurList;
            } else {
                return this.filteredEntryList;
            }
        });

        for (DriveEntry driveEntry : this.dinosaurList) {
            if(driveEntry.getKey().equals(this.blockEntity.getSelectKey(0))) {
                this.slotList.get(0).setDrive(driveEntry);
                this.slotList.get(0).slider.sliderValue = this.blockEntity.getSelectAmount(0);
            }
        }

        for (DriveEntry driveEntry : this.entryList) {
            for (int i = 1; i < this.slotList.size(); i++) {
                if(driveEntry.getKey().equals(this.blockEntity.getSelectKey(i))) {
                    this.slotList.get(i).setDrive(driveEntry);
                    this.slotList.get(i).slider.sliderValue = this.blockEntity.getSelectAmount(i);
                }
            }
        }
    }

    @Override
    protected int getOffset() {
        return 15;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.updateList();

        for (DnaSelectModule module : this.slotList) {
            module.slider.updateSlider();
        }

        if(this.dirty) {
            this.dirty = false;
            this.sync();
        }
    }

    private void refreshFilteredList() {
        this.filteredEntryList.clear();
        this.filteredEntryList.addAll(this.entryList);

        for (DnaSelectModule module : this.slotList) {
            if(module.drive != null) {
                this.filteredEntryList.remove(module.drive);
            }
        }
    }

    private void updateList() {
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
    }

    private void updateDriveEntires(List<DriveEntry> safeEntries) {
        for (DriveEntry e : this.entryList) {
            if(safeEntries.contains(e)) {
                continue;
            }

            for (DnaSelectModule module : this.slotList) {
                if(module.drive == e) {
                    module.setDrive(null);
                }
            }

            this.dinosaurList.remove(e);
        }
        this.entryList.removeIf(d -> !safeEntries.contains(d));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if(this.dropdownSelectionId == -1) {
            this.drawDefaultBackground();
        }
        this.hoveringText = null;
        super.drawScreen(mouseX, mouseY, partialTicks);

        int xStart = this.guiLeft + 23;
        int yStart = this.guiTop + 165;

        int w = 163;
        int yEnd = yStart + 16;

        Gui.drawRect(xStart-1, yStart-1, xStart+w, yEnd+1, -1);

        int start = 0;
        for (DnaSelectModule module : this.slotList) {
            if(module.drive == null || module.slider.sliderValue == 0) {
                continue;
            }
            int value = (int) Math.round(w * Math.round(module.slider.sliderValue * 100D) / 100D);
            Gui.drawRect(xStart + start, yStart, xStart + start + value-1, yEnd, 0xFF000000|this.colors[module.id]);
            if(module.drive != null && mouseX >= xStart + start && mouseX < xStart + start + value && mouseY >= yStart && mouseY < yEnd) {
                this.hoveringText = module.drive.getSearch();
            }
            start += value;

        }
        Gui.drawRect(xStart + start, yStart, xStart + w, yEnd, 0xFF000000);

        this.scrollBox.render(mouseX, mouseY);

        if(this.dropdownSelectionId != -1) {
            this.drawDefaultBackground();
            this.popupSelection.render(mouseX, mouseY);
        } else {
            if(this.hoveringText != null) {
                this.drawHoveringText(this.hoveringText, mouseX, mouseY);
            }
            this.renderHoveredToolTip(mouseX, mouseY);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.dropdownSelectionId != -1) {
            this.popupSelection.handleMouseInput();
        } else {
            this.scrollBox.handleMouseInput();

        }
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(this.dropdownSelectionId != -1) {
            if(this.popupSelection.isMouseOver(mouseX, mouseY, this.popupSelection.getTotalSize())) {
                this.popupSelection.mouseClicked(mouseX, mouseY, mouseButton);
            } else {
                this.dropdownSelectionId = -1;
            }
        } else {
            this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        for (DnaSelectModule module : this.slotList) {
            module.slider.mouseReleased(mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(new ResourceLocation(ProjectNublar.MODID, "textures/gui/dna_editing.png"));
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    private void sync() {
        //TODO: only sync necessary stuff
        for (int i = 0; i < this.slotList.size(); i++) {
            DnaSelectModule module = this.slotList.get(i);
            if(module.drive != null) {
                ProjectNublar.NETWORK.sendToServer(new C14SequencingSynthesizerSelectChange(
                    this.blockEntity.getPos(), i, module.drive.getKey(), module.slider.sliderValue)
                );
            }
        }
    }

    private class DnaSelectModule implements GuiScrollboxEntry {

        private final int id;
        private DriveEntry drive;
        private final ClampedGuiSlider slider;

        private DnaSelectModule(int id) {
            this.id = id;
            this.slider = new ClampedGuiSlider(id, this);
        }

        @Override
        public void draw(int x, int y, int mouseX, int mouseY, boolean mouseOver) {
            this.slider.x = x + 80;
            this.slider.y = y + 5;

            boolean enabled = this.id < slots;
            this.slider.enabled = enabled;
            if(this.drive != null && !enabled) {
                this.setDrive(null);
            }

            StencilStack.pushSquareStencil(x, y + 5, x+75, y+20);
            Gui.drawRect(x, y, x+75, y+25, enabled ? 0xFF000000 : 0xFF4A0000);
            if(this.drive != null) {
                this.drive.draw(x, y+5, mouseX, mouseY, mouseOver);
            }
            StencilStack.popStencil();
            this.slider.drawButton(mc, mouseX, mouseY, 1F);

            if(!enabled && mouseOver) {
                hoveringText = "Need " + SequencingSynthesizerBlockEntity.getPercentageForSlot(this.id+1) + " % of dino to use";//todo: localize
            }
        }

        public void setDrive(DriveEntry drive) {
            this.drive = drive;
            if(this.id == 0) {
                if(drive == null) {
                    slots = SequencingSynthesizerBlockEntity.SLOTS_AT_50;
                } else {
                    slots = SequencingSynthesizerBlockEntity.getSlots(DriveUtils.getAmount(blockEntity.getHandler().getStackInSlot(0), drive.getKey()) / 100F);
                }
            }
            refreshFilteredList();
        }

        @Override
        public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            if(relMouseX > 0 && relMouseX < 75 && relMouseY > 0 && relMouseY < 20) {
                dropdownSelectionId = this.id;
                popupSelection.setScroll(0);
            }
            return false;
        }

        @Override
        public boolean globalClicked(int mouseX, int mouseY, int mouseButton) {
            this.slider.mousePressed(mc, mouseX, mouseY);
            return false;
        }
    }

    private class DriveEntry implements SelectListEntry {

        private final String key;
        private final String entry;

        private DriveEntry(String key, String entry) {
            this.key = key;
            this.entry = I18n.format(entry);
        }

        @Override
        public void draw(int x, int y, int mouseX, int mouseY) {
            Minecraft.getMinecraft().fontRenderer.drawString(this.entry, x + 2, y + 5, -1);
        }

        public String getKey() {
            return this.key;
        }


        @Override
        public String getSearch() {
            return this.entry;
        }

        @Override
        public boolean onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            DnaEditingGui.this.dirty = true;
            if(dropdownSelectionId != -1) {
                slotList.get(dropdownSelectionId).setDrive(this);
                dropdownSelectionId = -1;
            }
            return false;
        }
    }

    private class ClampedGuiSlider extends GuiSlider {

        private final int id;
        private final DnaSelectModule module;

        ClampedGuiSlider(int id, DnaSelectModule module) {
            super(0, 0, 0, 79, 20, "", "%", 0D, 100D, DnaEditingGui.this.blockEntity.getSelectAmount(id), false, true);
            this.id = id;
            this.module = module;
        }

        @Override
        public void updateSlider() {
            for (DnaSelectModule selectModule : slotList) {
                selectModule.slider.directUpdateSlider();
            }
            super.updateSlider();
        }

        private void directUpdateSlider() {
            double prevValue = this.sliderValue;
            if(this.module.drive == null) {
                this.sliderValue = 0;
            } else {
                //Make sure that it doesn't add up to more than 100%:
                double amountLeftTo100 = 1D;
                for (int i = 0; i < this.id; i++) {
                    if(i == 0) {
                        amountLeftTo100 -= Math.max(0.5, slotList.get(0).slider.sliderValue);
                    } else {
                        amountLeftTo100 -= slotList.get(i).slider.sliderValue;
                    }
                }
                this.sliderValue = Math.min(this.sliderValue, amountLeftTo100);

                //Make sure we aren't using more genetic % than there actually is
                int maxAmount = DriveUtils.getAmount(blockEntity.getHandler().getStackInSlot(0), this.module.drive.getKey());
                this.sliderValue = MathHelper.clamp(this.sliderValue, 0, Math.max(maxAmount / 100D, 0));
            }
            if(prevValue != this.sliderValue) {
                DnaEditingGui.this.dirty = true;
            }
            super.updateSlider();
        }
    }
}
