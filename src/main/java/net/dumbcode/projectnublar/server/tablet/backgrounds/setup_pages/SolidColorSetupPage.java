package net.dumbcode.projectnublar.server.tablet.backgrounds.setup_pages;

import com.google.common.base.Predicate;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.dumbcode.projectnublar.server.tablet.backgrounds.SolidColorBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Point2i;
import java.awt.*;
import java.io.IOException;

public class SolidColorSetupPage implements SetupPage<SolidColorBackground>, GuiPageButtonList.GuiResponder {

    private static final Minecraft MC = Minecraft.getMinecraft();

    private static ShaderManager shaderManager;

    private static final int WHEEL_DIAMETER = 100;

    private float lightness = 0F;
    private boolean wheelSelected;
    private boolean sliderSelected;

    private final Point2i selectedPoint = new Point2i(0, 0);

    private GuiTextField redField;
    private GuiTextField greenField;
    private GuiTextField blueField;

    public SolidColorSetupPage() {
        if(shaderManager == null) {
            try {
                shaderManager = new ShaderManager(Minecraft.getMinecraft().getResourceManager(), ProjectNublar.MODID + ":colorwheel");
            } catch (IOException e) {
                ProjectNublar.getLogger().error("Unable to load color wheel shader :/", e);
            }
        }
    }

    @Override
    public void initPage(int x, int y) {
        int startX = x + WHEEL_DIAMETER + 30;
        this.redField = new GuiTextField(0, MC.fontRenderer, startX + 7, y + 78, 20, 20);
        this.greenField = new GuiTextField(1, MC.fontRenderer, startX + 29, y + 78, 20, 20);
        this.blueField = new GuiTextField(2, MC.fontRenderer, startX + 51, y + 78, 20, 20);

        this.redField.setGuiResponder(this);
        this.greenField.setGuiResponder(this);
        this.blueField.setGuiResponder(this);

        this.redField.setTextColor(0xFFFF0000);
        this.greenField.setTextColor(0xFF00FF00);
        this.blueField.setTextColor(0xFF0000FF);

        this.redField.setText("ff");
        this.greenField.setText("ff");
        this.blueField.setText("ff");

        Predicate<String> predicate = s -> {
            if(s != null && s.isEmpty()) {
                return true;
            }
            try {
                return Integer.parseInt(s, 16) <= 0xFF;
            } catch (NumberFormatException e) {
                return false;
            }
        };

        this.redField.setValidator(predicate);
        this.greenField.setValidator(predicate);
        this.blueField.setValidator(predicate);
    }

    @Override
    public int getWidth() {
        return WHEEL_DIAMETER + 103;
    }

    @Override
    public int getHeight() {
        return WHEEL_DIAMETER;
    }

    @Override
    public void render(int x, int y, int mouseX, int mouseY) {
        if(shaderManager != null) {

            shaderManager.getShaderUniformOrDefault("lightness").set(1F - this.lightness);

            shaderManager.useShader();

            int centerX = x + WHEEL_DIAMETER/2;
            int centerY = y + WHEEL_DIAMETER/2;

            int radii = WHEEL_DIAMETER/2;

            BufferBuilder buff = Tessellator.getInstance().getBuffer();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.pos(centerX - radii, centerY - radii, 0).tex(0, 0).endVertex();
            buff.pos(centerX - radii, centerY + radii, 0).tex(0, 1).endVertex();
            buff.pos(centerX + radii, centerY + radii, 0).tex(1, 1).endVertex();
            buff.pos(centerX + radii, centerY - radii, 0).tex(1, 0).endVertex();

            Tessellator.getInstance().draw();
            shaderManager.endShader();
        }

        int halfSliderWidth = 10;

        Gui.drawRect(x + WHEEL_DIAMETER + halfSliderWidth + 3, y + 5, x + WHEEL_DIAMETER + halfSliderWidth + 7, y + this.getHeight() - 5, 0xFF000000);
        Gui.drawRect(x + WHEEL_DIAMETER + 5, (int) (y + 2 + (this.getHeight()-10)*this.lightness), x + WHEEL_DIAMETER + 2*halfSliderWidth + 5, (int) (y + 8 + (this.getHeight()-10)*this.lightness), 0xFF000000);

        Gui.drawRect(x + WHEEL_DIAMETER + 2*halfSliderWidth + 10, y + 5, x + this.getWidth() - 5, y + this.getHeight() - 5, 0xFF000000 | this.calculateColor());

        this.redField.drawTextBox();
        this.greenField.drawTextBox();
        this.blueField.drawTextBox();

        int centerX = x + WHEEL_DIAMETER/2;
        int centerY = y + WHEEL_DIAMETER/2;
        RenderUtils.renderBorderExclusive(centerX + this.selectedPoint.x - 2, centerY + this.selectedPoint.y - 2, centerX + this.selectedPoint.x + 2, centerY + this.selectedPoint.y + 2, 2, -1);

        GlStateManager.color(1F, 1F, 1F, 1F);
    }

    @Override
    public void updatePage(int x, int y) {
        this.redField.updateCursorCounter();
        this.greenField.updateCursorCounter();
        this.blueField.updateCursorCounter();
    }

    private int calculateColor() {
        double theta = -Math.atan2(this.selectedPoint.y, this.selectedPoint.x) - Math.PI/2D;
        double brightness = Math.min(1F, Math.sqrt(this.selectedPoint.x*this.selectedPoint.x + this.selectedPoint.y*this.selectedPoint.y) / (WHEEL_DIAMETER/2D));
        return Color.HSBtoRGB((float) (theta / (2*Math.PI)), (float) brightness, 1F - this.lightness);
    }

    private void updateTextFields() {
        int color = this.calculateColor();
        this.redField.setText(this.convertColorToString((color >> 16) & 255));
        this.greenField.setText(this.convertColorToString((color >> 8) & 255));
        this.blueField.setText(this.convertColorToString(color & 255));
    }

    private String convertColorToString(int color) {
        String s = Integer.toString(color & 255, 16);
        if(s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    private void updateSelectors() {
        float[] hsb = Color.RGBtoHSB(this.stringToNumber(this.redField.getText()), this.stringToNumber(this.greenField.getText()), this.stringToNumber(this.blueField.getText()), null);
        this.lightness = 1F - hsb[2];

        double theta = -2*Math.PI*hsb[0] - Math.PI/2D;
        double length = hsb[1] * WHEEL_DIAMETER/2D;

        this.selectedPoint.set((int)(length*Math.cos(theta)), (int)(length*Math.sin(theta)));
    }

    private int stringToNumber(String text) {
        if(text.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(text, 16) & 0xFF;
    }

    @Override
    public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.redField.mouseClicked(mouseX, mouseY, mouseButton);
        this.greenField.mouseClicked(mouseX, mouseY, mouseButton);
        this.blueField.mouseClicked(mouseX, mouseY, mouseButton);

        int startX = x + WHEEL_DIAMETER;
        int halfSliderWidth = 10;
        if(mouseButton == 0 && mouseX > startX - 2 && mouseX < startX + 2*halfSliderWidth + 2&& mouseY > y - 2 && mouseY < y + WHEEL_DIAMETER-10 + 2) {
            this.lightness = (float) (mouseY - y) / (WHEEL_DIAMETER-10);
            this.sliderSelected = true;
        } else {
            this.sliderSelected = false;
        }

        int centerX = x + WHEEL_DIAMETER/2;
        int centerY = y + WHEEL_DIAMETER/2;
        int radii = WHEEL_DIAMETER/2;

        if((mouseX - centerX)*(mouseX - centerX) + (mouseY - centerY)*(mouseY - centerY) <= radii*radii) {
            this.wheelSelected = true;
            this.selectedPoint.set(mouseX - centerX + 5, mouseY - centerY + 5);
        } else {
            this.wheelSelected = false;
        }

        if(this.sliderSelected || this.wheelSelected) {
            this.updateTextFields();
        }
    }

    @Override
    public void mouseClickMove(int x, int y, int mouseX, int mouseY, int mouseButton, long timeSinceLastClick) {
        if(this.sliderSelected) {
            this.lightness = MathHelper.clamp((float) (mouseY - y) / (WHEEL_DIAMETER-10), 0F, 1F);
        }
        if(this.wheelSelected) {
            this.selectedPoint.set(mouseX - x - WHEEL_DIAMETER/2 + 5, mouseY - y - WHEEL_DIAMETER/2 + 5);
            double theta = Math.atan2(this.selectedPoint.y, this.selectedPoint.x);
            double length = Math.min(Math.sqrt(this.selectedPoint.x*this.selectedPoint.x + this.selectedPoint.y*this.selectedPoint.y), WHEEL_DIAMETER/2D);

            this.selectedPoint.set((int) (length*Math.cos(theta)), (int) (length*Math.sin(theta)));
        }

        if(this.sliderSelected || this.wheelSelected) {
            this.updateTextFields();
        }
    }

    @Override
    public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
        this.sliderSelected = this.wheelSelected = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        this.redField.textboxKeyTyped(typedChar, keyCode);
        this.greenField.textboxKeyTyped(typedChar, keyCode);
        this.blueField.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    public SolidColorBackground create() {
        SolidColorBackground background = new SolidColorBackground();
        background.setColor(this.calculateColor());
        return background;
    }

    @Override
    public void setEntryValue(int id, String value) {
        this.updateSelectors();
    }

    @Override
    public void setEntryValue(int id, boolean value) {
    }

    @Override
    public void setEntryValue(int id, float value) {
    }
}
