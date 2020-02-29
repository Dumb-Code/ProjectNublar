package net.dumbcode.projectnublar.server.tablet.backgrounds;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.dumbcode.dumblibrary.client.RenderUtils;
import net.dumbcode.projectnublar.server.ProjectNublar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Point2i;
import java.awt.*;
import java.io.IOException;

@Setter
@Getter
public class SolidColorBackground implements TabletBackground {

    public static final String KEY = "solid_color";

    private int color = 0xFFFFFF;

    @Override
    public String identifier() {
        return KEY;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("color", this.color);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        this.color = nbt.getInteger("color");
    }

    @Override
    public void writeToBuf(ByteBuf buf) {
        buf.writeInt(this.color);
    }

    @Override
    public void readFromBuf(ByteBuf buf) {
        this.color = buf.readInt();
    }

    @Override
    public void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        Gui.drawRect(x, y, x + width, y + height, this.color | 0xFF000000);
    }

    public static class SolidColorSetupPage implements SetupPage<SolidColorBackground> {

        private static ShaderManager shaderManager;

        private static final int WHEEL_DIAMETER = 100;

        private float lightness = 0F;
        private boolean wheelSelected;
        private boolean sliderSelected;

        private Point2i selectedPoint = new Point2i(0, 0); //final

        public SolidColorSetupPage() {
//            if(shaderManager == null) {
                try {
                    shaderManager = new ShaderManager(Minecraft.getMinecraft().getResourceManager(), ProjectNublar.MODID + ":colorwheel");
                } catch (IOException e) {
                    ProjectNublar.getLogger().error("Unable to load color wheel shader :/", e);
//                }
            }
        }

        @Override
        public int getWidth() {
            return WHEEL_DIAMETER + 100;
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

            int centerX = x + WHEEL_DIAMETER/2;
            int centerY = y + WHEEL_DIAMETER/2;
            RenderUtils.renderBorderExclusive(centerX + this.selectedPoint.x - 2, centerY + this.selectedPoint.y - 2, centerX + this.selectedPoint.x + 2, centerY + this.selectedPoint.y + 2, 2, -1);

            GlStateManager.color(1F, 1F, 1F, 1F);
        }

        private int calculateColor() {
            double theta = -Math.atan2(this.selectedPoint.y, this.selectedPoint.x) - Math.PI/2D;
            double brightness = Math.min(1F, 2*Math.sqrt(this.selectedPoint.x*this.selectedPoint.x + this.selectedPoint.y*this.selectedPoint.y) / WHEEL_DIAMETER);
            return Color.HSBtoRGB((float) (theta / (2*Math.PI)), (float) brightness, 1F - this.lightness);
        }

        @Override
        public void mouseClicked(int x, int y, int mouseX, int mouseY, int mouseButton) {
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
        }

        @Override
        public void mouseReleased(int x, int y, int mouseX, int mouseY, int mouseButton) {
            this.sliderSelected = this.wheelSelected = false;
        }

        @Override
        public SolidColorBackground create() {
            SolidColorBackground background = new SolidColorBackground();
            background.setColor(this.calculateColor());
            return background;
        }
    }
}
