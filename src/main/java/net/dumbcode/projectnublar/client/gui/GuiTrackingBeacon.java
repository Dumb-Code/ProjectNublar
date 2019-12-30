package net.dumbcode.projectnublar.client.gui;

import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.network.C31TrackingBeaconNameChanged;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;

public class GuiTrackingBeacon extends GuiScreen implements GuiPageButtonList.GuiResponder {

    private String name;
    private final BlockPos pos;

    private GuiTextField textField;

    public GuiTrackingBeacon(TrackingBeaconBlockEntity te) {
        this.name = te.getName();
        this.pos = te.getPos();
    }

    @Override
    public void initGui() {
        this.textField = new GuiTextField(0, mc.fontRenderer, this.width / 2 - 50, this.height / 2 - 10, 100, 20);
        this.textField.setText(this.name);
        this.textField.setCanLoseFocus(false);
        this.textField.setFocused(true);
        this.textField.setGuiResponder(this);
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.textField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {
        this.textField.updateCursorCounter();
        super.updateScreen();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.textField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void setEntryValue(int id, String value) {
        this.name = value;
        ProjectNublar.NETWORK.sendToServer(new C31TrackingBeaconNameChanged(this.pos, this.name));
    }

    @Override
    public void setEntryValue(int id, boolean value) {
    }

    @Override
    public void setEntryValue(int id, float value) {
    }
}
