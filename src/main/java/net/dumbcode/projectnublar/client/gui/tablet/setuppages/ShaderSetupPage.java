package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import net.dumbcode.projectnublar.mixin.ButtonAccessor;
import net.dumbcode.projectnublar.server.tablet.backgrounds.ShaderBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ShaderSetupPage extends SetupPage<ShaderBackground> {

    private EditBox textField;
    private Button button;

    private ShaderBackground.ScreenSize selectedSize = ShaderBackground.ScreenSize.FIT_TO_GUI;

    public ShaderSetupPage() {
        super(220, 70);
    }

    @Override
    public void setupFromPage(ShaderBackground page) {
        this.textField.setValue(page.getUrl());
        this.selectedSize = page.getSize();
        this.updateButtonText();
    }

    @Override
    public void initPage(int x, int y) {
        this.textField = this.add(new EditBox(Minecraft.getInstance().font, x + 10, y + 10, 200, 20, Component.literal("")));
        this.button = this.add(ButtonAccessor.construct(x + 10, y + 45, 200, 20, Component.literal(""), b -> {
            this.selectedSize = ShaderBackground.ScreenSize.values()[(this.selectedSize.ordinal() + 1) % ShaderBackground.ScreenSize.values().length];
            this.updateButtonText();
        }, Supplier::get));
        this.updateButtonText();
        super.initPage(x, y);
    }

    private void updateButtonText() {
        this.button.setMessage(Component.translatable("projectnublar.gui.shader.size", I18n.get(this.selectedSize.getTranslationKey())));
    }

    @Override
    public void updatePage() {
        this.textField.tick();
    }

    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
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
