package net.dumbcode.projectnublar.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.GuiGraphics;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.gui.GuiModelPoseEdit;
import net.dumbcode.dumblibrary.client.gui.GuiScrollBox;
import net.dumbcode.dumblibrary.client.gui.GuiScrollboxEntry;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
import net.dumbcode.projectnublar.server.network.BChangeGlobalRotation;
import net.dumbcode.projectnublar.server.network.BUpdatePoleList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.Mth;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;
import java.util.stream.Collectors;

public class GuiSkeletalProperties extends Screen {

    private final GuiModelPoseEdit parent;
    @Getter private final SkeletalBuilderBlockEntity builder;
    @Getter private final SkeletalProperties properties;
    public final List<PoleEntry> entries = Lists.newLinkedList();

    private float previousRot;

    private GuiScrollBox<PoleEntry> scrollBox = new GuiScrollBox<>(this.width / 2 - 100, 100, 200, 25, (this.height - 150) / 25, () -> this.entries);

    private Slider globalRotation;
    private Button editDirection;

    @Getter @Setter
    private PoleEntry editingPole;

    private TextFieldWidget editText;

    public GuiSkeletalProperties(GuiModelPoseEdit parent, SkeletalBuilderBlockEntity builder) {
        super(Component.literal("bruh"));
        this.parent = parent;
        this.builder = builder;
        this.properties = builder.getSkeletalProperties();
    }

    @Override
    public void init() {
        this.updateList();

        this.scrollBox = this.addButton(new GuiScrollBox<>(this.width / 2 - 100, 100, 200, 25, (this.height - 150) / 25, () -> this.entries));

        int diff = 0;

        int padding = 25;
        int top = 100;
        int total = this.height - top - 50;
        int totalAmount = Mth.floor(total / (float)padding);
        int bottom = totalAmount < 5 ? this.height - 80 : top + padding * 5;

        if(this.height > 275) {
            bottom += diff = this.height/2 - (bottom + top)/2;
        }

        this.globalRotation = this.addButton(new Slider( (this.width-200)/2, 30 + diff, 200, 20, Component.literal("rotaion"), Component.literal(""),0, 360, 0.0, true, true, p -> {}, s -> {
            float val = (float) s.getValue();
            if(this.previousRot != val) {
                this.previousRot = val;
                ProjectNublar.NETWORK.sendToServer(new BChangeGlobalRotation(this.builder.getBlockPos(), (float)s.getValue()));
            }
        }));
        this.globalRotation.setValue(this.properties.getRotation());

        this.addButton(new ExtendedButton(this.width / 2 + 70, 60 + diff, 20, 20, Component.literal("+"), p -> {
            this.builder.getSkeletalProperties().getPoles().add(new SkeletalProperties.Pole("", PoleFacing.NONE));
            this.sync();
            this.editingPole = this.entries.get(this.entries.size()-1);
            this.editText.active = true;
            this.editDirection.active = true;
        }));

        this.editText = this.addWidget(new TextFieldWidget(Minecraft.getInstance().font, this.width/2 - 100, bottom + 40, 75, 20, Component.literal("")));
        this.editText.setResponder(value -> {
            this.editingPole.pole.setCubeName(value);
            this.sync();
        });
        this.editDirection = this.addWidget(new ExtendedButton(this.width/2 + 25, bottom + 40, 75, 20, Component.literal(""), p -> {
            if(this.editingPole != null) {
                this.editingPole.pole.setFacing(this.editingPole.pole.getFacing().cycle());
                this.sync();
            }
        }));

        this.editText.active = this.editingPole != null;
        this.editDirection.active = this.editingPole != null;

    }


    @Override
    public void render(GuiGraphics stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);

        FontRenderer fr = minecraft.font;
        String poleliststr = "Pole List";  //todo: localize
        fr.draw(stack, poleliststr, (this.width-fr.width(poleliststr))/2F, 65, 0xAAAAAA);

        if(this.editingPole != null) {
            this.editText.render(stack, mouseX, mouseY, partialTicks);
            this.editDirection.render(stack, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void tick() {
        this.globalRotation.updateSlider();
        if (Minecraft.getInstance().isPaused()) { //Update cause the tickable wont
            this.builder.getSkeletalProperties().setPrevRotation(this.builder.getSkeletalProperties().getRotation());
        }
        if (this.editingPole != null) {
            this.editDirection.setMessage(Component.literal(this.editingPole.pole.getFacing().name())); //todo localize
        }
    }

    private void sync() {
        this.updateList();
        ProjectNublar.NETWORK.sendToServer(new BUpdatePoleList(this.builder.getBlockPos(), this.entries.stream().map(p -> p.pole).collect(Collectors.toList())));
    }

    public void updateList() {
        this.entries.clear();
        this.builder.getSkeletalProperties().getPoles().stream().map(PoleEntry::new).forEach(this.entries::add);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.children.add(this.editDirection);
        boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);
        this.children.remove(this.editDirection);

        if(ret) {
            return true;
        }

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
        return false;
    }

    public void setRotation(float rot) {
        this.previousRot = rot;
        this.globalRotation.setValue(rot);
    }

    public class PoleEntry implements GuiScrollboxEntry {
        @Getter private final SkeletalProperties.Pole pole;

        private final Button edit;
        private final Button delete;

        private boolean markedRemoved;

        private PoleEntry(SkeletalProperties.Pole pole) {
            this.pole = pole;

            this.edit = new ExtendedButton(0, 0, 20, 20, Component.literal(""), p -> {
                editingPole = this;
                editText.setValue(this.pole.getCubeName());
            }); //todo localize

            this.delete = new ExtendedButton(0, 0, 20, 20, Component.literal(""), p -> {
                this.markedRemoved = true;
            });
        }

        @Override
        public void draw(GuiGraphics stack, int x, int y, int width, int height, int mouseX, int mouseY, boolean mouseOver) {
            y += 12;
            minecraft.stack.drawString(font, pole.getCubeName(), x + 5, y - 3, 0xDDDDDD);

            this.edit.x = width/2+40;
            this.edit.y = y - 10;
            this.edit.render(stack, mouseX, mouseY, 1f);

            this.delete.x = width/2+70;
            this.delete.y = y - 10;
            this.delete.render(stack, mouseX, mouseY, 1f);
        }

        @Override
        public boolean onClicked(double relMouseX, double relMouseY, double mouseX, double mouseY) {
            return this.edit.mouseClicked(mouseX, mouseY, 0) || this.delete.mouseClicked(mouseX, mouseY, 0);
        }
    }
}
