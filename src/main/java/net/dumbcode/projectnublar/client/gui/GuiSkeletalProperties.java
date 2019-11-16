package net.dumbcode.projectnublar.client.gui;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.network.C11UpdatePoleList;
import net.dumbcode.projectnublar.server.network.C9ChangeGlobalRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GuiSkeletalProperties extends GuiScreen implements GuiSlider.ISlider, GuiPageButtonList.GuiResponder {

    private final GuiSkeletalBuilder parent;
    @Getter private final SkeletalBuilderBlockEntity builder;
    @Getter private final SkeletalProperties properties;
    public final List<PoleEntry> entries = Lists.newLinkedList();

    private GuiSlider globalRotation = new GuiSlider(0, 0, 0, 200, 20, "rotaion", "",0, 360, 0.0, true, true, this);
    private GuiButton addButton = new GuiButton(121, 0, 0, 20, 20, "+");
    private float previousRot;

    private GuiScrollBox<PoleEntry> scrollBox = new GuiScrollBox<>(this.width / 2 - 100, 100, 200, 25, (this.height - 150) / 25, () -> this.entries);

    @Getter @Setter
    private PoleEntry editingPole;

    private GuiTextField editText = new GuiTextField(5, Minecraft.getMinecraft().fontRenderer, 0, 0, 75, 20);
    private GuiButton editDirection = new GuiButtonExt(1, 0, 0, 75, 20, "");

    public GuiSkeletalProperties(GuiSkeletalBuilder parent, SkeletalBuilderBlockEntity builder) {
        this.editText.setGuiResponder(this);
        this.parent = parent;
        this.builder = builder;
        this.properties = builder.getSkeletalProperties();
        this.globalRotation.setValue(this.properties.getRotation());
    }

    @Override
    public void initGui() {

        this.updateList();

        this.scrollBox = new GuiScrollBox<>(this.width / 2 - 100, 100, 200, 25, (this.height - 150) / 25, () -> this.entries);


        int diff = 0;

        int padding = 25;
        int top = 100;
        int total = this.height - top - 50;
        int totalAmount = MathHelper.floor(total / (float)padding);
        int bottom = totalAmount < 5 ? this.height - 80 : top + padding * 5;

        if(this.height > 275) {
            bottom += diff = this.height/2 - (bottom + top)/2;
        }

        this.globalRotation.x = (this.width-this.globalRotation.width)/2;
        this.globalRotation.y = 30 + diff;

        this.addButton.x = this.width / 2 + 70;
        this.addButton.y = 60 + diff;

        this.editText.x = this.width/2 - 100;
        this.editText.y = bottom + 40;

        this.editDirection.x = this.width/2 + 25;
        this.editDirection.y = bottom + 40;

        this.addButton(this.globalRotation);
        this.addButton(this.addButton);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);


        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        String poleliststr = "Pole List";  //todo: localize
        fr.drawString(poleliststr, (this.width-fr.getStringWidth(poleliststr))/2, 65, 0xAAAAAA);

        this.scrollBox.render(mouseX, mouseY);

        if(this.editingPole != null) {
            this.editText.drawTextBox();
            this.editDirection.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        if(this.editingPole != null) {
            this.editText.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button == this.addButton) {
            this.builder.getSkeletalProperties().getPoles().add(new SkeletalProperties.Pole("", PoleFacing.NONE));
            this.sync();
            this.editingPole = this.entries.get(this.entries.size()-1);
        }
        if(button == this.editDirection && this.editingPole != null) {
            this.editingPole.pole.setFacing(this.editingPole.pole.getFacing().cycle());
            this.sync();
        }
    }

    @Override
    public void updateScreen() {
        this.globalRotation.updateSlider();
        if(Minecraft.getMinecraft().isGamePaused()) { //Update cause the tickable wont
            this.builder.getSkeletalProperties().setPrevRotation(this.builder.getSkeletalProperties().getRotation());
        }
        if(this.editingPole != null) {
            this.editDirection.displayString = this.editingPole.pole.getFacing().name(); //todo localize
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return super.doesGuiPauseGame();
    }

    private void sync() {
        this.updateList();
        ProjectNublar.NETWORK.sendToServer(new C11UpdatePoleList(this.builder, this.entries.stream().map(p -> p.pole).collect(Collectors.toList())));
    }

    public void updateList() {
        this.entries.clear();
        this.builder.getSkeletalProperties().getPoles().stream().map(PoleEntry::new).forEach(this.entries::add);

    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.scrollBox.handleMouseInput();
    }

    @Override
    public void onChangeSliderValue(GuiSlider slider) {
        if(slider == this.globalRotation) {
            float val = (float)this.globalRotation.getValue();
            if(this.previousRot != val) {
                this.previousRot = val;
                ProjectNublar.NETWORK.sendToServer(new C9ChangeGlobalRotation(this.builder, (float)slider.getValue()));
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.buttonList.add(this.editDirection);
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.buttonList.remove(this.editDirection);

        if(mouseButton == 1) {
            this.globalRotation.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
        }
        this.scrollBox.mouseClicked(mouseX, mouseY, mouseButton);

        boolean hasRemoved = false;
        for (PoleEntry entry : this.entries) {
            if(entry.markedRemoved) {
                this.getBuilder().getSkeletalProperties().getPoles().remove(entry.pole);
                hasRemoved = true;
            }
        }

        if(hasRemoved) {
            this.sync();
        }
        if(this.editingPole != null) {
            this.editText.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.globalRotation.mouseReleased(mouseX, mouseY);
    }

    public void setRotation(float rot) {
        this.previousRot = rot;
        this.globalRotation.setValue(rot);
    }

    @Override
    public void setEntryValue(int id, boolean value) {
    }

    @Override
    public void setEntryValue(int id, float value) {
    }

    @Override
    public void setEntryValue(int id, String value) {
        this.editingPole.pole.setCubeName(value);
        this.sync();
    }

    public class PoleEntry implements GuiScrollboxEntry {
        @Getter private final SkeletalProperties.Pole pole;

        private final GuiButton edit = new GuiButton(-1, 0, 0, 20, 20, ""); //todo localize
        private final GuiButton delete = new GuiButton(-1, 0, 0, 20, 20, ""); //todo localize

        private boolean markedRemoved;

        private PoleEntry(SkeletalProperties.Pole pole) {
            this.pole = pole;
        }

        @Override
        public void draw(int x, int y, int mouseX, int mouseY) {
            y += 12;
            mc.fontRenderer.drawString(pole.getCubeName(), x + 5, y - 3, 0xDDDDDD);

            this.edit.x = width/2+40;
            this.edit.y = y - 10;
            this.edit.drawButton(mc, mouseX, mouseY, 1f);

            this.delete.x = width/2+70;
            this.delete.y = y - 10;
            this.delete.drawButton(mc, mouseX, mouseY, 1f);
        }

        @Override
        public void onClicked(int relMouseX, int relMouseY, int mouseX, int mouseY) {
            if(this.edit.mousePressed(mc, mouseX, mouseY)) {
                editingPole = this;
                editText.setText(this.pole.getCubeName());
            } else if(this.delete.mousePressed(mc, mouseX, mouseY)) {
                this.markedRemoved = true;
            }
        }
    }
}
