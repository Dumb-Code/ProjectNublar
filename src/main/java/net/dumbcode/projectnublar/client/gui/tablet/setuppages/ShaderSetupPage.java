package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.server.tablet.backgrounds.ShaderBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ShaderSetupPage extends SetupPage<ShaderBackground> {

    private TextFieldWidget textField;
    private Button button;

    private ShaderBackground.ScreenSize selectedSize = ShaderBackground.ScreenSize.FIT_TO_GUI;

    public ShaderSetupPage() {
        super(220, 70);
    }

    @Override
    public void setupFromPage(ShaderBackground page) {
        this.textField.setMaxLength(40);
        this.textField.setValue(page.getUrl());
        this.selectedSize = page.getSize();
        this.updateButtonText();
    }

    @Override
    public void initPage(int x, int y) {
        this.textField = this.add(new TextFieldWidget(Minecraft.getInstance().font, x + 10, y + 10, 200, 20, new StringTextComponent("")));
        this.button = this.add(new Button(x + 10, y + 45, 200, 20, new StringTextComponent(""), b -> {
            this.selectedSize = ShaderBackground.ScreenSize.values()[(this.selectedSize.ordinal() + 1) % ShaderBackground.ScreenSize.values().length];
            this.updateButtonText();
        }));
        this.updateButtonText();
        super.initPage(x, y);
    }

    private void updateButtonText() {
        this.button.setMessage(new TranslationTextComponent("projectnublar.gui.shader.size", I18n.get(this.selectedSize.getTranslationKey())));
    }

    @Override
    public void updatePage() {
        this.textField.tick();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.textField.render(stack, mouseX, mouseY, partialTicks);
        this.button.render(stack, mouseX, mouseY, partialTicks);
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public ShaderBackground create() {
        ShaderBackground background = new ShaderBackground();
        background.setUrl(this.textField.getValue());
        background.setSize(this.selectedSize);
        return background;
    }
}
