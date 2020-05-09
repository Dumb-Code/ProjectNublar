package net.dumbcode.projectnublar.client.gui;

import com.google.common.primitives.Ints;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.network.C31TrackingBeaconDataChanged;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiTrackingBeacon extends GuiScreen implements GuiPageButtonList.GuiResponder {

    private final BlockPos pos;
    private String name;
    private int radius;

    private GuiTextField nameField;
    private GuiTextField radiusField;

    public GuiTrackingBeacon(TrackingBeaconBlockEntity te) {
        this.pos = te.getPos();
        this.name = te.getName();
        this.radius = te.getRadius();
    }

    @Override
    public void initGui() {
        this.nameField = new GuiTextField(0, mc.fontRenderer, this.width / 4, this.height / 2 - 50, this.width / 2, 20);
        this.nameField.setText(this.name);
        this.nameField.setFocused(true);
        this.nameField.setGuiResponder(this);

        this.radiusField = new GuiTextField(1, mc.fontRenderer, this.width / 2 - 25, this.height / 2, 50, 20);
        this.radiusField.setText(String.valueOf(this.radius));
        this.radiusField.setGuiResponder(this);
        this.radiusField.setValidator(input -> input != null && (input.isEmpty() || Ints.tryParse(input) != null));
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.nameField.drawTextBox();
        this.radiusField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        this.nameField.updateCursorCounter();
        this.radiusField.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.nameField.textboxKeyTyped(typedChar, keyCode);
        this.radiusField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
        this.radiusField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void setEntryValue(int id, String value) {
        if(id == 0) {
            this.name = value;
        }
        if(id == 1) {
            this.radius = value.isEmpty() ? 0 : Integer.parseInt(value);
        }
        ProjectNublar.NETWORK.sendToServer(new C31TrackingBeaconDataChanged(this.pos, this.name, this.radius));
    }

    @Override
    public void setEntryValue(int id, boolean value) {
    }

    @Override
    public void setEntryValue(int id, float value) {
    }
}
