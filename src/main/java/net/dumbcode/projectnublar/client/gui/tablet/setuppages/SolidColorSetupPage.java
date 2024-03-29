package net.dumbcode.projectnublar.client.gui.tablet.setuppages;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.dumbcode.dumblibrary.DumbLibrary;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.dumblibrary.client.gui.ColourUtils;
import net.dumbcode.projectnublar.server.tablet.backgrounds.SolidColorBackground;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class SolidColorSetupPage extends SetupPage<SolidColorBackground> {

    private static final Minecraft MC = Minecraft.getInstance();

    private static ShaderInstance shaderManager;

    private static final int WHEEL_DIAMETER = 100;

    private float lightness = 0F;
    private boolean wheelSelected;
    private boolean sliderSelected;

    private Vector3i selectedPoint = new Vector3i(0, 0, 0);

    private TextFieldWidget redField;
    private TextFieldWidget greenField;
    private TextFieldWidget blueField;

    public SolidColorSetupPage() {
        super(WHEEL_DIAMETER + 103, WHEEL_DIAMETER);
        //TODO: move to ColourWheelSelector
        if(shaderManager == null) {
            try {
                shaderManager = new ShaderInstance(MC.getResourceManager(), DumbLibrary.MODID + ":colorwheel");
            } catch (IOException e) {
                DumbLibrary.getLogger().error("Unable to load color wheel shader :/", e);
            }
        }
    }

    @Override
    public void setupFromPage(SolidColorBackground page) {
        int color = page.getColor();
        this.redField.setValue(this.convertColorToString((color >> 16) & 255));
        this.greenField.setValue(this.convertColorToString((color >> 8) & 255));
        this.blueField.setValue(this.convertColorToString(color & 255));
    }

    @Override
    public void initPage(int x, int y) {
        int startX = x + WHEEL_DIAMETER + 30;

        this.redField = this.add(new TextFieldWidget(MC.font, startX + 7, y + 78, 20, 20, this.redField, new StringTextComponent("R")));
        this.redField.setResponder(s -> this.updateSelectors());
        this.greenField = this.add(new TextFieldWidget(MC.font, startX + 29, y + 78, 20, 20, this.greenField, new StringTextComponent("G")));
        this.greenField.setResponder(s -> this.updateSelectors());
        this.blueField = this.add(new TextFieldWidget(MC.font, startX + 51, y + 78, 20, 20, this.blueField, new StringTextComponent("B")));
        this.blueField.setResponder(s -> this.updateSelectors());

        this.redField.setTextColor(0xFFFF0000);
        this.greenField.setTextColor(0xFF00FF00);
        this.blueField.setTextColor(0xFF0000FF);


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

        this.redField.setFilter(predicate);
        this.greenField.setFilter(predicate);
        this.blueField.setFilter(predicate);
        super.initPage(x, y);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        if(shaderManager != null) {

            shaderManager.safeGetUniform("lightness").set(1F - this.lightness);

            shaderManager.apply();

            int centerX = x + WHEEL_DIAMETER/2;
            int centerY = y + WHEEL_DIAMETER/2;

            int radii = WHEEL_DIAMETER/2;

            BufferBuilder buff = Tessellator.getInstance().getBuilder();

            buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            buff.vertex(centerX - radii, centerY - radii, 0).uv(0, 0).endVertex();
            buff.vertex(centerX - radii, centerY + radii, 0).uv(0, 1).endVertex();
            buff.vertex(centerX + radii, centerY + radii, 0).uv(1, 1).endVertex();
            buff.vertex(centerX + radii, centerY - radii, 0).uv(1, 0).endVertex();

            Tessellator.getInstance().end();
            shaderManager.clear();
        }

        int halfSliderWidth = 10;

        AbstractGui.fill(stack, x + WHEEL_DIAMETER + halfSliderWidth + 3, y + 5, x + WHEEL_DIAMETER + halfSliderWidth + 7, y + this.getHeight() - 5, 0xFF000000);
        AbstractGui.fill(stack, x + WHEEL_DIAMETER + 5, (int) (y + 2 + (this.getHeight()-10)*this.lightness), x + WHEEL_DIAMETER + 2*halfSliderWidth + 5, (int) (y + 8 + (this.getHeight()-10)*this.lightness), 0xFF000000);

        AbstractGui.fill(stack, x + WHEEL_DIAMETER + 2*halfSliderWidth + 10, y + 5, x + this.getWidth() - 5, y + this.getHeight() - 5, 0xFF000000 | this.calculateColor());

        this.redField.render(stack, mouseX, mouseY, partialTicks);
        this.greenField.render(stack, mouseX, mouseY, partialTicks);
        this.blueField.render(stack, mouseX, mouseY, partialTicks);

        int centerX = x + WHEEL_DIAMETER/2;
        int centerY = y + WHEEL_DIAMETER/2;
        RenderUtils.renderBorderExclusive(stack, centerX + this.selectedPoint.getX() - 2, centerY + this.selectedPoint.getY() - 2, centerX + this.selectedPoint.getX() + 2, centerY + this.selectedPoint.getY() + 2, 2, -1);

        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public void updatePage() {
        super.updatePage();
        this.redField.tick();
        this.greenField.tick();
        this.blueField.tick();
    }

    private int calculateColor() {
        double theta = -Math.atan2(this.selectedPoint.getY(), this.selectedPoint.getX()) - Math.PI/2D;
        double brightness = Math.min(1F, Math.sqrt(this.selectedPoint.getX()*this.selectedPoint.getX() + this.selectedPoint.getY()*this.selectedPoint.getY()) / (WHEEL_DIAMETER/2D));
        return ColourUtils.HSBtoRGB((float) (theta / (2*Math.PI)), (float) brightness, 1F - this.lightness);
    }

    private void updateTextFields() {
        int color = this.calculateColor();
        this.redField.setValue(this.convertColorToString((color >> 16) & 255));
        this.greenField.setValue(this.convertColorToString((color >> 8) & 255));
        this.blueField.setValue(this.convertColorToString(color & 255));
    }

    private String convertColorToString(int color) {
        String s = Integer.toString(color & 255, 16);
        if(s.length() == 1) {
            return "0" + s;
        }
        return s;
    }

    private void updateSelectors() {
        float[] hsb = ColourUtils.RGBtoHSB(this.stringToNumber(this.redField.getValue()), this.stringToNumber(this.greenField.getValue()), this.stringToNumber(this.blueField.getValue()));
        this.lightness = 1F - hsb[2];

        double theta = -2*Math.PI*hsb[0] - Math.PI/2D;
        double length = hsb[1] * WHEEL_DIAMETER/2D;

        this.selectedPoint = new Vector3i((int)(length*Math.cos(theta)), (int)(length*Math.sin(theta)), 0);
    }

    private int stringToNumber(String text) {
        if(text.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(text, 16) & 0xFF;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(!super.mouseClicked(mouseX, mouseY, mouseButton)) {
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
                this.selectedPoint = new Vector3i(mouseX - centerX + 5, mouseY - centerY + 5, 0);
            } else {
                this.wheelSelected = false;
            }

            if(this.sliderSelected || this.wheelSelected) {
                this.updateTextFields();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double changeX, double changeY) {
        if(!super.mouseDragged(mouseX, mouseY, button, changeX, changeY)) {
            if(this.sliderSelected) {
                this.lightness = MathHelper.clamp((float) (mouseY - y) / (WHEEL_DIAMETER-10), 0F, 1F);
            }
            if(this.wheelSelected) {
                this.selectedPoint = new Vector3i(mouseX - x - WHEEL_DIAMETER/2F + 5, mouseY - y - WHEEL_DIAMETER/2F + 5, 0);
                double theta = Math.atan2(this.selectedPoint.getY(), this.selectedPoint.getX());
                double length = Math.min(Math.sqrt(this.selectedPoint.getX()*this.selectedPoint.getX() + this.selectedPoint.getY()*this.selectedPoint.getY()), WHEEL_DIAMETER/2D);

                this.selectedPoint = new Vector3i((int) (length*Math.cos(theta)), (int) (length*Math.sin(theta)), 0);
            }

            if(this.sliderSelected || this.wheelSelected) {
                this.updateTextFields();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.sliderSelected = this.wheelSelected = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public SolidColorBackground create() {
        SolidColorBackground background = new SolidColorBackground();
        background.setColor(this.calculateColor());
        return background;
    }
}
