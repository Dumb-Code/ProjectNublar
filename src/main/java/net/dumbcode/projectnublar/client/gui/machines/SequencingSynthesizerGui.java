package net.dumbcode.projectnublar.client.gui.machines;

import com.google.common.collect.Lists;
import net.dumbcode.dumblibrary.client.gui.GuiDropdownBox;
import net.dumbcode.projectnublar.server.block.entity.SequencingSynthesizerBlockEntity;
import net.dumbcode.projectnublar.server.item.data.DriveUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.List;
import java.util.function.DoubleFunction;

public class SequencingSynthesizerGui extends GuiContainer {

    private final SequencingSynthesizerBlockEntity blockEntity;

    private GuiDropdownBox initialBox;
    private GuiDropdownBox secondaryBox;
    private GuiDropdownBox thirdBox;

    private ClampedGuiSlider initialSlider;
    private ClampedGuiSlider secondarySlider;
    private ClampedGuiSlider thirdSlider;

    private List<StringEntry> dinosaurList = Lists.newArrayList();
    private List<StringEntry> entryList = Lists.newArrayList();

    public SequencingSynthesizerGui(EntityPlayer player, SequencingSynthesizerBlockEntity blockEntity) {
        super(blockEntity.createContainer(player));
        this.blockEntity = blockEntity;
        this.xSize = 256;
        this.ySize = 240;
        this.updateList();

    }

    @Override
    public void initGui() {
        super.initGui();

        this.initialBox = new GuiDropdownBox(this.guiLeft, this.guiTop + 10, 90, 20, 5, () -> this.dinosaurList);
        this.secondaryBox = new GuiDropdownBox(this.guiLeft, this.guiTop + 40, 90, 20, 5, () -> this.entryList);
        this.thirdBox = new GuiDropdownBox(this.guiLeft, this.guiTop + 70, 90, 20, 5, () -> this.entryList);

        this.initialSlider = new ClampedGuiSlider(0, this.guiLeft + 100, this.guiTop + 10, 90, 20, "", "%", 0D, 100D, 50D, false, true, d -> Math.max(d, 0.5D), this.initialBox);
        this.secondarySlider = new ClampedGuiSlider(1, this.guiLeft + 100, this.guiTop + 40, 90, 20, "", "%", 0D, 100D, 50D, false, true, d -> Math.min(d, 1D - this.initialSlider.sliderValue), this.secondaryBox);
        this.thirdSlider = new ClampedGuiSlider(2, this.guiLeft + 100, this.guiTop + 70, 90, 20, "", "%", 0D, 100D, 0D, false, true, d -> Math.min(d, 1D - this.initialSlider.sliderValue - this.secondarySlider.sliderValue), this.thirdBox);

    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.updateList();

        this.initialSlider.updateSlider();
        this.secondarySlider.updateSlider();
        this.thirdSlider.updateSlider();

    }

    private void updateList() {
        this.entryList.clear();
        this.dinosaurList.clear();

        for (DriveUtils.DriveEntry entry : DriveUtils.getAll(this.blockEntity.getHandler().getStackInSlot(0))) {
            StringEntry stringEntry = new StringEntry(entry.getName());
            if(entry.getDriveType() == DriveUtils.DriveType.DINOSAUR) {
                this.dinosaurList.add(stringEntry);
            }
            this.entryList.add(stringEntry);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        GuiDropdownBox mouseOver =
                this.initialBox.isMouseOver(mouseX, mouseY) || this.initialBox.open  ? this.initialBox :
                        this.secondaryBox.isMouseOver(mouseX, mouseY) || this.secondaryBox.open  ? this.secondaryBox :
                                this.thirdBox.isMouseOver(mouseX, mouseY) || this.thirdBox.open  ? this.thirdBox : null;

        if(mouseOver != this.initialBox) {
            this.initialBox.render(mouseX, mouseY);
        }
        if(mouseOver != this.secondaryBox) {
            this.secondaryBox.render(mouseX, mouseY);
        }
        if(mouseOver != this.thirdBox) {
            this.thirdBox.render(mouseX, mouseY);
        }

        if(mouseOver != null) {
            mouseOver.render(mouseX, mouseY);
        }

        this.initialSlider.drawButton(mc, mouseX, mouseY, partialTicks);
        this.secondarySlider.drawButton(mc, mouseX, mouseY, partialTicks);
        this.thirdSlider.drawButton(mc, mouseX, mouseY, partialTicks);

        int xStart = this.guiLeft;
        int yStart = this.guiTop + 100;

        int w = 190;
        int yEnd = yStart + 15;


        int initial = (int) (w * this.initialSlider.sliderValue);
        int second = initial + (int) (w * this.secondarySlider.sliderValue);
        int third = second + (int) (w * this.thirdSlider.sliderValue);

        Gui.drawRect(xStart, yStart, xStart + initial, yEnd, 0xFFFF0000); //Initial
        Gui.drawRect(xStart + initial, yStart, xStart + second, yEnd, 0xFF00FF00); //Second
        Gui.drawRect(xStart + second, yStart, xStart + third, yEnd, 0xFF0000FF); //Third
        Gui.drawRect(xStart + third, yStart, xStart + w, yEnd, 0xFF000000); //Leftover

        if(mouseX <= xStart + 190 && mouseX >= xStart && mouseY <= yEnd && mouseY >= yStart) {
            int x = mouseX - xStart;
            String text;
            if(x <= initial) {
                text = "initial";
            } else if(x <= second) {
                text = "second";
            } else if(x <= third) {
                text = "third";
            } else {
                text = "void";
            }
            this.drawHoveringText(text, mouseX, mouseY);
        }


        this.renderHoveredToolTip(mouseX, mouseY);


    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

        GuiDropdownBox mouseOver =
                this.initialBox.isMouseOver(mouseX, mouseY) || this.initialBox.open  ? this.initialBox :
                        this.secondaryBox.isMouseOver(mouseX, mouseY) || this.secondaryBox.open  ? this.secondaryBox :
                                this.thirdBox.isMouseOver(mouseX, mouseY) || this.thirdBox.open  ? this.thirdBox : null;

        if(mouseOver != null) {
            mouseOver.handleMouseInput();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        GuiDropdownBox mouseOver =
                this.initialBox.isMouseOver(mouseX, mouseY) || this.initialBox.open ? this.initialBox :
                        this.secondaryBox.isMouseOver(mouseX, mouseY) || this.secondaryBox.open ? this.secondaryBox :
                                this.thirdBox.isMouseOver(mouseX, mouseY) || this.thirdBox.open ? this.thirdBox : null;

        if(mouseOver != null) {
            mouseOver.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            this.initialBox.mouseClicked(mouseX, mouseY, mouseButton);
            this.secondaryBox.mouseClicked(mouseX, mouseY, mouseButton);
            this.thirdBox.mouseClicked(mouseX, mouseY, mouseButton);
        }

        this.initialSlider.mousePressed(mc, mouseX, mouseY);
        this.secondarySlider.mousePressed(mc, mouseX, mouseY);
        this.thirdSlider.mousePressed(mc, mouseX, mouseY);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.initialSlider.mouseReleased(mouseX, mouseY);
        this.secondarySlider.mouseReleased(mouseX, mouseY);
        this.thirdSlider.mouseReleased(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        ResourceLocation slotLocation = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        Minecraft.getMinecraft().renderEngine.bindTexture(slotLocation);
        for(Slot slot : this.inventorySlots.inventorySlots) {
            this.drawTexturedModalRect(this.guiLeft + slot.xPos - 1, this.guiTop + slot.yPos - 1, 7, 17, 18, 18);
        }
    }

    private class StringEntry implements GuiDropdownBox.SelectListEntry {

        private final String entry;

        private StringEntry(String entry) {
            this.entry = I18n.format(entry);
        }

        @Override
        public void draw(int x, int y) {
            Minecraft.getMinecraft().fontRenderer.drawString(this.entry, x + 2, y + 5, -1);

        }
        @Override
        public String getSearch() {
            return this.entry;
        }
    }

    private class ClampedGuiSlider extends GuiSlider {

        private final DoubleFunction<Double> clampFuncion;
        private final GuiDropdownBox dropdownBox;

        public ClampedGuiSlider(int id, int xPos, int yPos, int width, int height, String prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, DoubleFunction<Double> clampFuncion, GuiDropdownBox dropdownBox) {
            super(id, xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr);
            this.clampFuncion = clampFuncion;
            this.dropdownBox = dropdownBox;
        }

        @Override
        public void updateSlider() {
            initialSlider.directUpdateSlider();
            secondarySlider.directUpdateSlider();
            thirdSlider.directUpdateSlider();
            super.updateSlider();
        }

        private void directUpdateSlider() {
            if(this.dropdownBox.getActive() == null) {
                this.sliderValue = 0;
            } else {
                this.sliderValue = this.clampFuncion.apply(this.sliderValue);
            }
            super.updateSlider();
        }
    }
}
