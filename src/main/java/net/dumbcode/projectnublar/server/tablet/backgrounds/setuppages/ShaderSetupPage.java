package net.dumbcode.projectnublar.server.tablet.backgrounds.setuppages;

import net.dumbcode.projectnublar.server.tablet.backgrounds.ShaderBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

public class ShaderSetupPage implements SetupPage<ShaderBackground> {

    private final GuiTextField textField = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 200, 20);

    @Override
    public int getWidth() {
        return 220;
    }

    @Override
    public int getHeight() {
        return 40;
    }

    @Override
    public void initPage(int x, int y) {
        this.textField.x = x + 10;
        this.textField.y = y + 10;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.textField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.textField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updatePage(int x, int y) {
        this.textField.updateCursorCounter();
    }

    @Override
    public void render(int x, int y, int mouseX, int mouseY) {
        this.textField.drawTextBox();
    }

    @Override
    public ShaderBackground create() {
        ShaderBackground background = new ShaderBackground();
        background.setUrl(this.textField.getText());
        return background;
    }
}
