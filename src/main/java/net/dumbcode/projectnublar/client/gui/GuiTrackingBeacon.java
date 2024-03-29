package net.dumbcode.projectnublar.client.gui;

import com.google.common.primitives.Ints;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.BlockTrackingBeacon;
import net.dumbcode.projectnublar.server.block.entity.TrackingBeaconBlockEntity;
import net.dumbcode.projectnublar.server.network.C2STrackingBeaconData;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class GuiTrackingBeacon extends Screen implements IHasContainer<BlockTrackingBeacon.TrackingContainer> {

    private final BlockTrackingBeacon.TrackingContainer container;

    private String name;
    private int radius;

    private TextFieldWidget nameField;
    private TextFieldWidget radiusField;

    public GuiTrackingBeacon(BlockTrackingBeacon.TrackingContainer container) {
        super(new TranslationTextComponent(ProjectNublar.MODID + ".gui.trackingbeacon.title"));
        this.container = container;
        TrackingBeaconBlockEntity te = container.getBeacon();
        this.name = te.getName();
        this.radius = te.getRadius();
    }

    @Override
    public BlockTrackingBeacon.TrackingContainer getMenu() {
        return this.container;
    }

    @Override
    public void init() {
        this.nameField = this.addButton(new TextFieldWidget(minecraft.font, this.width / 4, this.height / 2 - 50, this.width / 2, 20, new StringTextComponent("")));
        this.nameField.setValue(this.name);
        this.nameField.setFocus(true);
        this.nameField.setResponder(s -> {
            this.name = s;
            ProjectNublar.NETWORK.sendToServer(new C2STrackingBeaconData(this.name, this.radius));
        });

        this.radiusField = this.addButton(new TextFieldWidget(minecraft.font, this.width / 2 - 25, this.height / 2, 50, 20, new StringTextComponent("")));
        this.radiusField.setValue(String.valueOf(this.radius));
        this.radiusField.setResponder(s -> {
            this.radius = s.isEmpty() ? 0 : Integer.parseInt(s);
            ProjectNublar.NETWORK.sendToServer(new C2STrackingBeaconData(this.name, this.radius));

        });
        this.radiusField.setFilter(input -> input != null && (input.isEmpty() || Ints.tryParse(input) != null));
        super.init();
    }


    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, ticks);
    }

    @Override
    public void tick() {
        this.nameField.tick();
        this.radiusField.tick();
        super.tick();
    }
}
