package net.dumbcode.projectnublar.server.tablet.backgrounds.setuppages;

import net.dumbcode.projectnublar.server.tablet.backgrounds.ShaderBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;

public class ShaderSetupPage implements SetupPage<ShaderBackground> {

    private final GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 200, 20);

    private ShaderBackground.ScreenSize selectedSize = ShaderBackground.ScreenSize.FIT_TO_GUI;
    private final GuiButton button = new GuiButton(1, 0, 0, 200, 20, "");

    @Override
    public void setupFromPage(ShaderBackground page) {
    	this.textField.setMaxStringLength(40);
        this.textField.setText(page.getUrl());
        this.selectedSize = page.getSize();
        this.updateButtonText();
    }

    @Override
    public int getWidth() {
        return 220;
    }

    @Override
    public int getHeight() {
        return 70;
    }

    @Override
    public void initPage(int x, int y) {
        this.textField.x = x + 10;
        this.textField.y = y + 10;

        this.button.x = x + 10;
        this.button.y = y + 45;
        this.updateButtonText();
    }

    private void updateButtonText() {
        this.button.displayString = I18n.format("projectnublar.gui.shader.size", I18n.format(this.selectedSize.getTranslationKey()));
    }


    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
        if(this.button.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
            this.selectedSize = ShaderBackground.ScreenSize.values()[(this.selectedSize.ordinal() + 1) % ShaderBackground.ScreenSize.values().length];
            this.updateButtonText();
        }
    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.button.mouseReleased(mouseX, mouseY);
    }

    @Override
    public void updatePage(int x, int y) {
        this.textField.updateCursorCounter();
    }

    @Override
    public void render(int x, int y, int mouseX, int mouseY) {
        this.textField.drawTextBox();
        this.button.drawButton(Minecraft.getMinecraft(), mouseX, mouseY, 1F);
    }

    @Override
    public ShaderBackground create() {
        ShaderBackground background = new ShaderBackground();
        background.setUrl(this.textField.getText());
        background.setSize(this.selectedSize);
        return background;
    }
}
