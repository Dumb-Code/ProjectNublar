package net.dumbcode.projectnublar.client.gui;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.block.entity.SkeletalBuilderBlockEntity;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.PoleFacing;
import net.dumbcode.projectnublar.server.block.entity.skeletalbuilder.SkeletalProperties;
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

public class GuiSkeletalProperties extends GuiScreen implements GuiSlider.ISlider {

    private final GuiSkeletalBuilder parent;
    @Getter private final SkeletalBuilderBlockEntity builder;
    @Getter private final SkeletalProperties properties;
    public final List<PoleEntry> entries = Lists.newArrayList();

    private GuiSlider globalRotation = new GuiSlider(0, 0, 0, 200, 20, "rotaion", "",0, 360, 0.0, true, true, this);
    private float previousRot;
    public float scroll;
    private PoleEntry editingPole;

    private GuiTextField editText = new GuiTextField(5, Minecraft.getMinecraft().fontRenderer, 0, 0, 75, 20);
    private GuiButton editDirection = new GuiButtonExt(1, 0, 0, 75, 20, "");

    public GuiSkeletalProperties(GuiSkeletalBuilder parent, SkeletalBuilderBlockEntity builder) {
        this.parent = parent;
        this.builder = builder;
        this.properties = builder.getSkeletalProperties();
        this.globalRotation.setValue(this.properties.getRotation());
    }

    @Override
    public void initGui() {

        this.updateList();

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


        this.editText.x = this.width/2 - 100;
        this.editText.y = bottom + 40;

        this.editDirection.x = this.width/2 + 25;
        this.editDirection.y = bottom + 40;

        this.addButton(this.globalRotation);

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if(!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled()) {
            Minecraft.getMinecraft().getFramebuffer().enableStencil();
        }

        int diff = 0;

        int padding = 25;
        int top = 100;
        int total = this.height - top - 50;
        int left = this.width / 2 - 100;
        int totalAmount = MathHelper.floor(total / (float)padding);
        int bottom = totalAmount < 5 ? this.height - 80 : top + padding * 5;

        if(this.height > 275) {
            diff = this.height/2 - (bottom + top)/2;
            bottom += diff;
            top += diff;
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        String poleliststr = "Pole List";  //todo: localize
        fr.drawString(poleliststr, (this.width-fr.getStringWidth(poleliststr))/2, 80 + diff, 0xAAAAAA);


        GL11.glEnable(GL11.GL_STENCIL_TEST);
        GL11.glColorMask(false, false, false, false);
        GL11.glDepthMask(false);
        GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

        GL11.glStencilMask(0xFF);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        Gui.drawRect(left, top-10, this.width / 2 + 100, bottom+10, -1);

        GL11.glColorMask(true, true, true, true);
        GL11.glDepthMask(true);
        GL11.glStencilMask(0x00);

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);


        int scrolledTop = (int) (top - this.scroll * 5F);

        for (int i = 0; i < this.entries.size(); i++) {
            this.entries.get(i).render(mouseX, mouseY, scrolledTop + padding*i, partialTicks);
        }

        GL11.glDisable(GL11.GL_STENCIL_TEST);

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
        if(button == this.editDirection && this.editingPole != null) {
            this.editingPole.pole.setFacing(this.editingPole.pole.getFacing().cycle());
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

    public void updateList() {
        this.entries.clear();
        Lists.newArrayList(
                new SkeletalProperties.Pole("Test1", PoleFacing.EAST),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2", PoleFacing.NONE),
                new SkeletalProperties.Pole("Test2D", PoleFacing.DOWN)

        ).stream().map(PoleEntry::new).forEach(this.entries::add);

    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int mouseInput = Mouse.getEventDWheel();
        if(mouseInput != 0) {
            this.scroll = MathHelper.clamp(this.scroll + (mouseInput > 0 ? 1 : -1), 0, 100); //todo upper limit
        }
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
        for (PoleEntry entry : this.entries) {
            entry.mouseClicked(mouseX, mouseY, mouseButton);
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

    private class PoleEntry {
        private final SkeletalProperties.Pole pole;

        private final GuiButton edit = new GuiButton(-1, 0, 0, 20, 20, ""); //todo localize
        private final GuiButton delete = new GuiButton(-1, 0, 0, 20, 20, ""); //todo localize

        private PoleEntry(SkeletalProperties.Pole pole) {
            this.pole = pole;
        }

        public void render(int mouseX, int mouseY, int y, float partialTicks) {
            mc.fontRenderer.drawString(pole.getCubeName(), width / 2 - 100, y, 0xDDDDDD);

            this.edit.x = width/2+40;
            this.edit.y = y - 10;
            this.edit.drawButton(mc, mouseX, mouseY, partialTicks);

            this.delete.x = width/2+70;
            this.delete.y = y - 10;
            this.delete.drawButton(mc, mouseX, mouseY, partialTicks);
        }

        protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
            if(this.edit.mousePressed(mc, mouseX, mouseY)) {
                editingPole = this;
                editText.setText(this.pole.getCubeName());
            } else if(this.delete.mousePressed(mc, mouseX, mouseY)) {
                System.out.println("delete");
            }
        }
    }
}
