package net.dumbcode.projectnublar.client.gui;

import lombok.Getter;
import lombok.Setter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.BlockEntitySkeletalBuilder;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.network.C11ChangePoleFacing;
import net.dumbcode.projectnublar.server.network.C9ChangeGlobalRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.io.IOException;

public class GuiSkeletalProperties extends GuiScreen implements GuiSlider.ISlider {

    private final GuiSkeletalBuilder parent;
    @Getter private final BlockEntitySkeletalBuilder builder;
    @Getter private final SkeletalProperties properties;

    private GuiSlider globalRotation = new GuiSlider(0, 0, 0, 200, 20, "rotaion", "",0, 360, 0.0, true, true, this);
    private float previousRot;

    private GuiButton dirButton = new GuiButtonExt(1, 0, 0, "");

    private GuiButton poleListGui = new GuiButtonExt(1, 0, 0, "Pole List");
    @Getter @Setter private PoleFacing poleFacing;

    public GuiSkeletalProperties(GuiSkeletalBuilder parent, BlockEntitySkeletalBuilder builder) {
        this.parent = parent;
        this.builder = builder;
        this.properties = builder.getSkeletalProperties();
        this.poleFacing = this.properties.getPoleFacing();
        this.globalRotation.setValue(this.properties.getRotation());
    }

    @Override
    public void initGui() {
        int paddingX = 10;
        int paddingY = 10;

        this.globalRotation.x = paddingX;
        this.globalRotation.y = paddingY;

        this.dirButton.x = this.globalRotation.x;
        this.dirButton.y = this.globalRotation.y+this.globalRotation.height+paddingX;

        this.poleListGui.x = this.dirButton.x;
        this.poleListGui.y = this.dirButton.y+this.dirButton.height+paddingX;

        this.addButton(this.globalRotation);
        this.addButton(this.dirButton);
        this.addButton(this.poleListGui);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button == this.dirButton) {
            this.poleFacing = this.poleFacing.cycle();
            ProjectNublar.NETWORK.sendToServer(new C11ChangePoleFacing(this.builder, this.poleFacing));
        }
    }

    @Override
    public void updateScreen() {
        this.globalRotation.updateSlider();
        this.dirButton.displayString = "Facing: " + this.poleFacing.name().toLowerCase();
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
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if(mouseButton == 1) {
            this.globalRotation.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY);
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
}
